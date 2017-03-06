package it.cfalchi.alfrescorest.service;

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
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.cfalchi.alfrescorest.model.DocumentRetrived;
import it.cfalchi.alfrescorest.model.RequestMessage;
import it.cfalchi.alfrescorest.model.ResponseMessage;
import it.cfalchi.alfrescorest.utils.CmisClient;
import it.cfalchi.alfrescorest.utils.RequestConstants;

@Service("cmisService")
public class CmisService implements AlfrescoService{
	
	private static Logger logger = LoggerFactory.getLogger(CmisService.class);
	
	/**
	 * Crea un nuovo documento in un path specificato. Se esiste già un oggetto nel path
	 * effettua solo l'upload dei byte del contenuto, altrimenti crea una nuova istanza Document
	 * con relativi metadati.
	 * Restituisce l'istanza del nuovo documento/documento aggiornato.
	 * 
	 * @return newDocument
	 */
	@Override
	public ResponseMessage createDocument(RequestMessage message) throws FileNotFoundException{
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		String user = cmisClient.getUser();
		Map<String, Object> request = message.getRequest();
		
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
			
			//TODO versioning!!
			//controllo se esiste già un file con lo stesso nome
			newDocument = (Document) cmisClient.getObject(parentFolder, (String) request.get(RequestConstants.DOC_NAME));
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
	@Override
	public ResponseMessage createFolder(RequestMessage message) {
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		String user = cmisClient.getUser();
		Map<String, Object> request = message.getRequest();
		
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
			newFolder = (Folder) cmisClient.getObject(parentFolder, folderName);
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
	@Override
	public Document getDocumentByUUIDPath(RequestMessage message){
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		Map<String, Object> request = message.getRequest();
		String path = (String) request.get(RequestConstants.DOC_PATH);
		String uuid = (String) request.get(RequestConstants.DOC_UUID);
		
		CmisObject object = null;
		try {
			if(path!=null && !path.equals("")){
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
			logger.error("Document not found! [id: " + uuid + "]" );
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
	@Override
	public ResponseMessage getDocumentsByFolder(RequestMessage message){
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		Map<String, Object> request = message.getRequest();
		String path = (String) request.get(RequestConstants.FOLDER_PATH);
		boolean getContent = (boolean) request.get(RequestConstants.GET_CONTENT);
		
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
	@SuppressWarnings("unchecked")
	@Override
	public ResponseMessage removeDocuments(RequestMessage message){
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		String user = cmisClient.getUser();
		Map<String, Object> request = message.getRequest();
		List<String> uuidList = (List<String>) request.get(RequestConstants.UUID_LIST);
		
		ResponseMessage response = new ResponseMessage();
		Map<String,String> notDeleted = new HashMap<>();
		
		for(String uuid : uuidList){
			try{
				Document doc = (Document) session.getObject(uuid);
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
	@Override
	public ResponseMessage removeFolder(RequestMessage message){
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Session session = cmisClient.getSession();
		String user = cmisClient.getUser();
		Map<String, Object> request = message.getRequest();
		String path = (String) request.get(RequestConstants.FOLDER_PATH);
		
		ResponseMessage response = new ResponseMessage();
		Map<String,String> notDeleted = new HashMap<>();
		
		try{
			Folder folder = (Folder) session.getObjectByPath(path);
			//controllo permessi
			if (folder.getAllowableActions().getAllowableActions().contains(Action.CAN_DELETE_TREE) == false) {
				logger.error(user + " does not have permission to delete folder tree " + folder.getPath());
				response.setCode("403");
		    	response.setMessage("User does not have permission to to delete folder tree " + folder.getPath());
				return response;
			}
			boolean deleteAllVersions = true;
			boolean continueOnFailure = true;
			List<String> failedObjectIds =folder.deleteTree(deleteAllVersions, UnfileObject.DELETE, continueOnFailure);
			logger.info("Deleted folder and all its content: " + folder.getName());
			if (failedObjectIds != null && failedObjectIds.size() > 1) {
				for (String failedObjectId : failedObjectIds) {
					logger.info("Could not delete Alfresco node with id " + failedObjectId);
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
	
}
