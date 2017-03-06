package it.cfalchi.alfrescorest.controller;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import it.cfalchi.alfrescorest.model.RequestMessage;
import it.cfalchi.alfrescorest.model.ResponseMessage;

public interface AlfrescoController {
	
	public ResponseMessage createDocumentService (@RequestPart("message") RequestMessage message, 
			@RequestPart("file") MultipartFile file) throws IOException;
	
	public ResponseMessage createFolderService (@RequestBody RequestMessage message);
	
	public ResponseEntity<InputStreamResource> getDocumentService (@RequestBody RequestMessage message) throws IOException ;
	
	public ResponseMessage getDocumentsFolderService (@RequestBody RequestMessage message);
	
	public ResponseMessage removeFolderService (@RequestBody RequestMessage message);
	
	public ResponseMessage removeDocumentsService (@RequestBody RequestMessage message);

}
