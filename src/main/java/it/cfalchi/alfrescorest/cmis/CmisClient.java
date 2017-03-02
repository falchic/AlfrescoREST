package it.cfalchi.alfrescorest.cmis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cfalchi.alfrescorest.model.DocumentRetrived;
import it.cfalchi.alfrescorest.model.ResponseMessage;
import it.cfalchi.alfrescorest.utils.RequestConstants;

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
		logger.info("Trying to connect with user: " + user + " and password: " + password);
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
			logger.info("Found Alfresco repository [id: " + alfrescoRepository.getId() + "]");
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
	 * @return newDocument
	 */
	public ResponseMessage createDocument(Map<String,Object> request) throws FileNotFoundException{
		Folder parentFolder = null;
		Document newDocument = null;
		ResponseMessage response = new ResponseMessage();
		try{
			parentFolder = (Folder) session.getObjectByPath((String) request.get(RequestConstants.DOC_PATH));
			//controllo permessi
			if (parentFolder.getAllowableActions().getAllowableActions().contains(Action.CAN_CREATE_DOCUMENT) == false) {
				logger.error(user + "does not " + "have permission to create a document in " + parentFolder.getPath());
				response.setCode("403");
		    	response.setMessage("User does not have permission to create a document in the folder");
		    	return response;
			}
			
			//controllo se esiste già un file con lo stesso nome
			newDocument = (Document) getObject(parentFolder, (String) request.get(RequestConstants.DOC_NAME));
			if (newDocument == null){
				String name = (String) request.get(RequestConstants.DOC_NAME);
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
				props.put(PropertyIds.NAME, (String) name);
				String mimeType = (String) request.get(RequestConstants.DOC_MIME_TYPE);
				InputStream input = (InputStream) request.get(RequestConstants.REQUEST_ATTACHMENT);
				//gestione aspects per CMIS 1.0 (title, description)
				props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document,P:cm:titled");
				props.put("cm:title", request.get(RequestConstants.DOC_TITLE));
				props.put("cm:description", request.get(RequestConstants.DOC_DESCR));
				ContentStream contentStream = session.getObjectFactory()
						.createContentStream(name, (long) request.get("length"), mimeType, input);
				newDocument = parentFolder.createDocument(props, contentStream, VersioningState.MAJOR);
				logger.info("Document '" + name + "' created in " + (String) request.get(RequestConstants.DOC_PATH));
				response.setMessage("Document created!");
			} else {
				//se file esiste viene eseguito l'update del contenuto
				logger.info("Document already exists, updating content stream...");
				String name = (String) request.get(RequestConstants.DOC_NAME);
				String mimeType = (String) request.get(RequestConstants.DOC_MIME_TYPE);
				InputStream input = (InputStream) request.get(RequestConstants.REQUEST_ATTACHMENT);
				ContentStream contentStream = session.getObjectFactory()
						.createContentStream(name, (long) request.get("length"), mimeType, input);
				newDocument.setContentStream(contentStream, true);
				logger.info("Document "+newDocument.getPaths().get(0)+" updated");
				response.setMessage("Document updated!");
			}
		    //TODO versioning
		    //nel messaggio di risposta è presente l'uuid e la versione del documento appena creato
			String[] uuidVersion = getUuidVersion(newDocument.getId());
			response.setAttribute("docUuid", uuidVersion[0]);
			response.setAttribute("docVersion", uuidVersion[1]);
		} catch (CmisObjectNotFoundException | IllegalArgumentException e){
			response.setCode("400");
	    	response.setMessage("Bad request, path not valid");
			logger.error("Path not valid!");
			return response;
		}
		response.setCode("200");
		return response;
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
	
	private String[] getUuidVersion(String id){
		String[] parts = id.split("/"); 
		String shortId = parts[3];
		String[] uuidVersion = shortId.split(";");
		return uuidVersion;
	}
	
	/**
	 * Crea una nuova cartella nel path indicato, se non esiste già.
	 */
	//TODO creazione in profondità
	//TODO attribuzione permessi
	public ResponseMessage createFolder(Map<String,Object> request) {
		String path = (String) request.get(RequestConstants.FOLDER_PATH);
		String folderName = (String) request.get(RequestConstants.FOLDER_NAME);
		Folder parentFolder = null;
		Folder newFolder = null;
		ResponseMessage response = new ResponseMessage();
		
		try{
			parentFolder = (Folder) session.getObjectByPath(path);
			//controllo sui permessi dell'utente
			if (parentFolder.getAllowableActions().getAllowableActions().contains(Action.CAN_CREATE_FOLDER) == false) {
				logger.error(user + "does not " + "have permission to create a folder in " + parentFolder.getPath());
				response.setCode("403");
		    	response.setMessage("User does not have permission to create a folder in the path");
		    	return response;
			}
			
			//controlla se esiste già una cartella con lo stesso nome
			newFolder = (Folder) getObject(parentFolder, folderName);
			if(newFolder == null){
				Map<String, Object> folderProps = new HashMap<String, Object>();
				folderProps.put(PropertyIds.NAME, folderName);
				folderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
				newFolder = parentFolder.createFolder(folderProps);
				logger.info("Folder '"+folderName+"' created in " + path);
				//nel messagggio di risposta è presente il nome e il path della cartella appena creata
				response.setCode("200");
		    	response.setMessage("Folder created!");
		    	response.setAttribute("folderName", folderName);
		    	response.setAttribute("folderPath", path);
			} else {
				logger.info("Folder "+newFolder.getPath()+" already exists!");
				response.setCode("200");
		    	response.setMessage("Folder already exist!");
			}
		} catch (CmisObjectNotFoundException | IllegalArgumentException e){
			response.setCode("400");
	    	response.setMessage("Bad request, path not valid");
			logger.error("Path not valid!");
			return response;
		}
		return response;
	}
	
	/**
	 * Ottiene un documento a partire dall'uuid o dal path (i parametri sono in alternativa).
	 * 
	 * @param uuid
	 * @param path
	 * @return document
	 */
	public Document getDocumentByUUIDPath(String uuid, String path){
		CmisObject object = null;
		try {
			if(path!=null){
				object = session.getObjectByPath(path);
				logger.info("Document retrived [path: " + path + "]");
				return (Document)object;
			}
			String id = "workspace://SpacesStore/" + uuid;
			object = session.getObject(id);
			if (object == null) return null;
			if(!object.getType().getDisplayName().equals("Document")) return null;
			logger.info("Document retrived [id: " + uuid + "]");
		} catch (CmisObjectNotFoundException | IllegalArgumentException e){
			logger.error("Document not found!");
		}
		return (Document)object;
	}
	
	/**
	 * Restituisce la lista di documenti presenti in una data cartella.
	 * 
	 * @param path
	 * @return documents
	 * @throws IOException 
	 */
	public ResponseMessage getDocumentsByFolder(String path, boolean getContent){
		List<DocumentRetrived> documents = new ArrayList<DocumentRetrived>();
		Folder parentFolder = null;
		ResponseMessage response = new ResponseMessage();
		
		try {
			parentFolder = (Folder) session.getObjectByPath(path);
		} catch (IllegalArgumentException e){
			logger.error("Path not valid!");
			response.setCode("400");
	    	response.setMessage("Bad request, path not valid");
	    	return response;
		}

		if (parentFolder!=null){
			Iterator<CmisObject> it = parentFolder.getChildren().iterator();
			while(it.hasNext()) {
			  CmisObject object = it.next();
			  if(object.getType().getDisplayName().equals("Document")){
				  Document doc = (Document) object;
				  String uuid = getUuidVersion(doc.getId())[0];
				  DocumentRetrived document = new DocumentRetrived(uuid, doc.getName());
				  if(getContent){
					  ContentStream contentStream = doc.getContentStream();
					  InputStream stream = contentStream.getStream();
					  document.setContent(stream);
					  document.setSize(doc.getContentStreamLength());
				  }
				  documents.add(document);
				  logger.info("Document " + doc.getName() + " found in " + path);
			  }
			}
		} else {
			logger.error("Path not valid!");
			response.setCode("400");
	    	response.setMessage("Bad request, path not valid");
	    	return response;
		}
		response.setCode("200");
    	response.setMessage("Documents retrived!");
    	response.setAttribute("documents", documents);
		return response;
	}
	
	/**
	 * Data una lista di uuid, elimina i documenti relativi.
	 * 
	 * @param uuidList
	 */
	public ResponseMessage removeDocuments(List<String> uuidList){
		ResponseMessage response = new ResponseMessage();
		Map<String,String> notDeleted = new HashMap<>();
		
		for(String uuid : uuidList){
			try{
				Document doc = getDocumentByUUIDPath(uuid, "");
				String path = doc.getPaths().get(0);
				//controllo permessi
				if (doc.getAllowableActions().getAllowableActions().
						contains(Action.CAN_DELETE_OBJECT) == false) {
						logger.error(user + " does not have permission to delete document " + doc.getName());
						notDeleted.put(uuid,"User does not have permission");
						}
				boolean deleteAllVersions = true;
				doc.delete(deleteAllVersions);
				logger.info("Deleted document in "+ path);
			} catch(CmisObjectNotFoundException e){
				logger.error("Document not found! [id: " + uuid + "]");
				notDeleted.put(uuid,"Document not found");
			}
		}
		if(notDeleted.isEmpty()){
			response.setCode("200");
	    	response.setMessage("Documents deleted!");
		} else {
			response.setCode("400");
			response.setMessage("Not all documents have been deleted");
			response.setAttribute("errors", notDeleted);
		}
		return response;
	}
	
	/**
	 * Elimina la cartella definita dal path.
	 * 
	 * @param path
	 */
	public ResponseMessage removeFolder(String path){
		ResponseMessage response = new ResponseMessage();
		Map<String,String> notDeleted = new HashMap<>();
		
		try{
			Folder folder = (Folder) session.getObjectByPath(path);
			//controllo permessi
			if (folder.getAllowableActions().getAllowableActions().contains(Action.CAN_DELETE_TREE) == false) {
				logger.error(user + " does not have permission to delete folder tree" + folder.getPath());
				response.setCode("403");
		    	response.setMessage("User does not have permission to to delete folder tree" + folder.getPath());
				return response;
			}
			boolean deleteAllVersions = true;
			boolean continueOnFailure = true;
			List<String> failedObjectIds =folder.deleteTree(deleteAllVersions, UnfileObject.DELETE, continueOnFailure);
			logger.info("Deleted folder and all its content: " + folder.getName());
			if (failedObjectIds != null && failedObjectIds.size() > 1) {
				for (String failedObjectId : failedObjectIds) {
					logger.info("Could not delete Alfresco node with id: " + failedObjectId);
					notDeleted.put(failedObjectId,"Could not delete node");
				}
			}
		} catch (CmisObjectNotFoundException | IllegalArgumentException e){
			logger.error("Folder not found: " + path);
			response.setCode("400");
	    	response.setMessage("Bad request, path not valid");
	    	return response;
		}
		if(notDeleted.isEmpty()){
			response.setCode("200");
	    	response.setMessage("Folder tree deleted!");
		} else {
			response.setCode("400");
			response.setMessage("Some errors occurred");
			response.setAttribute("errors", notDeleted);
		}
		return response;
	}
	
	//TODO versioning!!
	
}
