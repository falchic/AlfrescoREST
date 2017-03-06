package it.cfalchi.alfrescorest.service;

import java.io.FileNotFoundException;

import org.apache.chemistry.opencmis.client.api.Document;

import it.cfalchi.alfrescorest.model.RequestMessage;
import it.cfalchi.alfrescorest.model.ResponseMessage;

public interface AlfrescoService {
	
	public ResponseMessage createDocument(RequestMessage message) throws FileNotFoundException;
	
	public ResponseMessage createFolder(RequestMessage message);
	
	public Document getDocumentByUUIDPath(RequestMessage message);
	
	public ResponseMessage getDocumentsByFolder(RequestMessage message);
	
	public ResponseMessage removeDocuments(RequestMessage message);
	
	public ResponseMessage removeFolder(RequestMessage message);
	
}
