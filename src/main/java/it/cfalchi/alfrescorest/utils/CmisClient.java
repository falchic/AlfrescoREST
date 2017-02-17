package it.cfalchi.alfrescorest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmisClient {
	
	// utente che si connette ad Alfresco
	private String user;
	private Session session;
	
	private static final String ALFRESCO_ATOMPUB_URL = "http://localhost:8080/alfresco/cmisatom";
	
	private static Logger logger = LoggerFactory.getLogger(CmisClient.class);
	
	public CmisClient(String user, String password) {
		this.user = user;
		this.session = createSession(user, password);
	}
	
	public Session getSession(){
		return session;
	}
	
	/**
	 * Crea una sessione per effettuare operazioni sul repository: ottenute le informazioni
	 * sul repository sono necessari username e password dell'utente che vuole connettersi.
	 * 
	 * @param user
	 * @param password
	 * @return session
	 */
	public Session createSession(String user, String password) {
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(SessionParameter.USER, user);
		parameters.put(SessionParameter.PASSWORD, password);
		parameters.put(SessionParameter.ATOMPUB_URL, ALFRESCO_ATOMPUB_URL);
		parameters.put(SessionParameter.BINDING_TYPE,
				BindingType.ATOMPUB.value());
		parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
		//parameters.put(SessionParameter.COMPRESSION, "true");  //compressione HTTP response
		//parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "0");  //cache time-to-live

		List<Repository> repositories = sessionFactory.getRepositories(parameters);
		Repository alfrescoRepository = null;
		if(repositories != null && repositories.size()>0){
			//dovrebbe esserci solo un repository in Alfresco
			alfrescoRepository = repositories.get(0);
			logger.info("Found Alfresco repository, ID: " + alfrescoRepository.getId());
		} else {
			throw new CmisConnectionException(
					"Could not connect to the Alfresco Server, no repository found!");
		}
		Session session = alfrescoRepository.createSession();
		logger.info("Connected to Alfresco repository");
		return session;
	}
	
	/**
	 * Crea un nuovo documento in un path specificato. Se esiste già un oggetto nel path
	 * effettua solo l'upload dei byte del contenuto, altrimenti crea una nuova istanza Document
	 * con relativi metadati.
	 * Restituisce l'istanza del nuovo documento/documento aggiornato.
	 * 
	 * @param path
	 * @param 
	 * @return newDocument
	 */
	public Document createDocument(String path, File file) throws FileNotFoundException{
		Folder parentFolder = null;
		Document newDocument = null;
		try{
			parentFolder = (Folder) session.getObjectByPath(path);
			//controllo permessi
			if (parentFolder.getAllowableActions().getAllowableActions().contains(Action.CAN_CREATE_DOCUMENT) == false) {
				throw new CmisUnauthorizedException(
						user + "does not " + "have permission to create a document in " + parentFolder.getPath());
			}
			
			//controllo se esiste già un file con lo stesso nome
			newDocument = (Document) getObject(parentFolder, file.getName());
			if (newDocument == null){
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
				props.put(PropertyIds.NAME, file.getName());
				String mimeType = new MimetypesFileTypeMap().getContentType(file);
				InputStream input = new FileInputStream(file);
				//gestione aspects per CMIS 1.0 (title, description)
				props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document,P:cm:titled");
				props.put("cm:title", "titolo prova");
				props.put("cm:description", "prova Description");
				ContentStream contentStream = session.getObjectFactory()
						.createContentStream(file.getName(), file.length(), mimeType, input);
				newDocument = parentFolder.createDocument(props, contentStream, VersioningState.MAJOR);
				logger.info("Document '"+file.getName()+"' created in " + path);
			} else {
				//se file esiste viene eseguito l'update del contenuto
				logger.info("Document already exists, updating content stream...");
				String mimeType = new MimetypesFileTypeMap().getContentType(file);
				InputStream input = new FileInputStream(file);
				ContentStream contentStream = session.getObjectFactory()
						.createContentStream(file.getName(), file.length(), mimeType, input);
				newDocument.setContentStream(contentStream, true);
				logger.info("Document "+newDocument.getPaths().get(0)+" updated");
			}
		} catch (CmisObjectNotFoundException e){
			logger.error("Path not valid!");
		}
		return newDocument;
	}
	
	/**
	 * Ottiene un oggetto CMIS, dato il nome della risorsa e la cartella in cui si trova:
	 * se esiste la risorsa, restituisce l'oggetto.
	 * 
	 * @param parentFolder
	 * @param name
	 * @return object
	 */
	private CmisObject getObject(Folder parentFolder, String name){
		CmisObject object = null;
		try {
			String path = parentFolder.getPath();
			if(!path.endsWith("/")){
				path += "/";
			}
			path += name;
			object = session.getObjectByPath(path);
		}catch(CmisObjectNotFoundException e){}
		return object;
	}
	
	/**
	 * Crea una nuova cartella nel path indicato, se non esiste già.
	 * 
	 * @param path
	 * @param folderName
	 */
	//TODO creazione in profondità e documenti?
	//TODO attribuzione permessi
	public void createFolder(String path, String folderName) {
		Folder parentFolder = null;
		try{
			parentFolder = (Folder) session.getObjectByPath(path);
			//controllo sui permessi dell'utente
			if (parentFolder.getAllowableActions().getAllowableActions().contains(Action.CAN_CREATE_FOLDER) == false) {
				throw new CmisUnauthorizedException(
						user + " does not have permission to create a " + "subfolder in " 
				+ parentFolder.getPath());
			}
			
			//controlla se esiste già una cartella con lo stesso nome
			Folder newFolder = (Folder) getObject(parentFolder, folderName);
			if(newFolder == null){
				Map<String, Object> folderProps = new HashMap<String, Object>();
				folderProps.put(PropertyIds.NAME, folderName);
				folderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
				newFolder = parentFolder.createFolder(folderProps);
				logger.info("Folder '"+folderName+"' created in " + path);
			} else {
				logger.info("Folder "+newFolder.getPath()+" already exists!");
			}
			
		} catch (CmisObjectNotFoundException e){
			logger.error("Object not found!");
		}
	}
	
	/**
	 * Ottiene un documento a partire dall'uuid o dal path (i parametri sono in alternativa).
	 * 
	 * @param uuid
	 * @param path
	 * @return document
	 */
	public Document getDocumentByUUID(String uuid, String path){
		CmisObject object = null;
		try {
			if (!"".equals(uuid) && !"".equals(path)) {
				logger.error("Not a valid UUID or path!");
				return null;
			} else{
				if(!"".equals(path)){
					object = session.getObjectByPath(path);
					return (Document)object;
				}
				String id = "workspace://SpacesStore/"+uuid;
				object = session.getObject(id);
				if (object == null) return null;
				if(!object.getType().getDisplayName().equals("Document")) return null;
			}
			if (path.equals("")){
				
			}
			logger.info("Document retrived [id: " + uuid + "]");
		} catch (CmisObjectNotFoundException e){
			logger.error("Object not found!");
		}
		return (Document)object;
	}
	
	public List<Document> getDocumentsByFolder(String path){
		List<Document> documents = new ArrayList<Document>();
		Folder parentFolder = (Folder) session.getObjectByPath(path);
		Iterator<CmisObject> it = parentFolder.getChildren().iterator();
		logger.info("Documents in "+path+":");
		while(it.hasNext()) {
		  CmisObject object = it.next();
		  if(object.getType().getDisplayName().equals("Document")){
			  Document doc = (Document) object;
			  documents.add(doc);
			  logger.info(doc.getName());
		  }
		}
		return documents;
	}
	
	public void removeDocument(List<String> uuidList){
		for(String uuid : uuidList){
			Document doc = getDocumentByUUID(uuid, "");
			String path = doc.getPaths().get(0);
			//controllo permessi
			if (doc.getAllowableActions().getAllowableActions().
					contains(Action.CAN_DELETE_OBJECT) == false) {
					throw new CmisUnauthorizedException(user + " does not have permission to delete document " +
					doc.getName()+" with Object ID "+doc.getId());
					}
			boolean deleteAllVersions = true;
			doc.delete(deleteAllVersions);
			logger.info("Deleted document in "+ path);
		}
	}
	
	public void removeFolder(String path){
		try{
			Folder folder = (Folder) session.getObjectByPath(path);
			//controllo permessi
			if (folder.getAllowableActions().getAllowableActions().
					contains(Action.CAN_DELETE_TREE) == false) {
					throw new CmisUnauthorizedException(user + " does" +
					" not have permission to delete folder tree" + folder.getPath());
					}
			boolean deleteAllVersions = true;
			boolean continueOnFailure = true;
			List<String> failedObjectIds =folder.deleteTree(deleteAllVersions, UnfileObject.DELETE, continueOnFailure);
			logger.info("Deleted folder and all its content: " + folder.getName());
			if (failedObjectIds != null && failedObjectIds.size() > 1) {
				for (String failedObjectId : failedObjectIds) {
					logger.info("Could not delete Alfresco node with id: " + failedObjectId);
				}
			}
		} catch (CmisObjectNotFoundException e){
			logger.error("Object not found: " + path);
		}
	}
	
	//TODO versioning!!
	
}
