package it.cfalchi.alfrescorest.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.cfalchi.alfrescorest.cmis.CmisClient;
import it.cfalchi.alfrescorest.model.DocumentRetrived;
import it.cfalchi.alfrescorest.model.RequestMessage;
import it.cfalchi.alfrescorest.utils.AlfrescoRestURIConstants;
import it.cfalchi.alfrescorest.utils.RequestConstants;


@Controller
public class AlfrescoController {
	
	private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);
	
	@RequestMapping("/")
    public String root() {
        return "home";
    }
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination":"",
	 *			"title" "",
	 *			"description": ""
	 *		}
	 *	}
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_CREATE_DOC, method = RequestMethod.POST, consumes = {"multipart/form-data"})
	public @ResponseBody ResponseEntity<String> createDocumentService (@RequestParam("message") String messageReq, 
			@RequestParam("file") MultipartFile file) throws JsonParseException, JsonMappingException, IOException{
		logger.info("Start createDocumentService");
		
		ResponseEntity<String> response = null;
		ObjectMapper mapper = new ObjectMapper();
		RequestMessage message = mapper.readValue(messageReq, RequestMessage.class);
		
		boolean isValid = message.isValid(false);
		if(isValid && !file.isEmpty()){
			CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
			Map<String,Object> request = message.getRequest();		
			request.put(RequestConstants.DOC_NAME, file.getOriginalFilename());
			request.put(RequestConstants.DOC_MIME_TYPE, file.getContentType());
			request.put(RequestConstants.FILE_LENGTH, file.getSize());
			byte[] in = file.getBytes();
			InputStream inputStream = new ByteArrayInputStream(in);
		    request.put("attachment", inputStream);
		    boolean created = cmisClient.createDocument(request);
		    if(created){
		    	response = new ResponseEntity<String>(HttpStatus.CREATED);
		    } else {
		    	response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		    }
		} else {
			response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		logger.info("Stop createDocumentService");
		return response;
	}
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination":"",
	 *			"name" ""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_CREATE_FOLDER, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> createFolderService (@RequestBody RequestMessage message){
		logger.info("Start createFolderService");
		
		ResponseEntity<String> response = null;
		
		boolean isValid = message.isValid(false);
		if(isValid){
			CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
			Map<String,Object> request = message.getRequest();
		    boolean created = cmisClient.createFolder(request);
		    if(created){
		    	response = new ResponseEntity<String>(HttpStatus.CREATED);
		    } else {
		    	response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		    }
		} else {
			response = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		logger.info("Stop createFolderService");
		return response;
	}
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"uuid":"e63f8ee3-6e64-4694-9cde-4998a24f65a8;1.0",
	 *			"destination": ""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_GET_DOC, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> getDocumentService (@RequestBody RequestMessage message) throws IOException {
		logger.info("Start getDocumentService");
		
		byte[] out = null;
		Document doc = null;
		ResponseEntity<byte[]> response = null;
		
		boolean isValid = message.isValid(true);
		if(isValid){
			CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
			Map<String,Object> request = message.getRequest();
			String uuid = (String) request.get(RequestConstants.DOC_UUID);
			String path = (String) request.get(RequestConstants.DOC_PATH);
			doc = cmisClient.getDocumentByUUIDPath(uuid, path);
			if(doc!=null){
				ContentStream contentStream = doc.getContentStream();
				InputStream stream = contentStream.getStream();
				out=IOUtils.toByteArray(stream);
				HttpHeaders responseHeaders = new HttpHeaders();
		        responseHeaders.add("content-disposition", "attachment; filename=" + doc.getName());
		        responseHeaders.add("Content-Type",doc.getContentStreamMimeType());
				response = new ResponseEntity<byte[]>(out, responseHeaders, HttpStatus.OK);
			} else {
				response = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			}
		} else {
			response = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		logger.info("End getDocumentService");
		return response;
	}
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination":"",
	 *			"get_content": ""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_GET_DOCS, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<DocumentRetrived>> getDocumentsFolderService (@RequestBody RequestMessage message) throws IOException {
		logger.info("Start getDocumentsFolderService");
		
		byte[] out = null;
		ResponseEntity<List<DocumentRetrived>> response = null;
		List<DocumentRetrived> documentList = new ArrayList<DocumentRetrived>();
		
		boolean isValid = message.isValid(false);
		if(isValid){
			CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
			Map<String,Object> request = message.getRequest();
			String path = (String) request.get(RequestConstants.DOC_PATH);
			List<Document> docs = cmisClient.getDocumentsByFolder(path);
			if(docs!=null && !docs.isEmpty()){
				for (Document doc : docs){
					String uuid = getShortUuid(doc.getId());
					DocumentRetrived document = new DocumentRetrived(uuid, doc.getName());
					boolean getContent = (boolean) request.get(RequestConstants.GET_CONTENT);
					if(getContent){
						ContentStream contentStream = doc.getContentStream();
						InputStream stream = contentStream.getStream();
						out=IOUtils.toByteArray(stream);
						document.setContent(out);
					}
					documentList.add(document);
				}
				response = new ResponseEntity<List<DocumentRetrived>>(documentList, HttpStatus.OK);
			} else if(docs.isEmpty()){
				logger.info("No documents in " + path);
				response = new ResponseEntity<List<DocumentRetrived>>(documentList, HttpStatus.OK);
			} else {
				response = new ResponseEntity<List<DocumentRetrived>>(HttpStatus.NOT_FOUND);
			}
		} else {
			response = new ResponseEntity<List<DocumentRetrived>>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		logger.info("End getDocumentsFolderService");
		return response;
	}
	
	private String getShortUuid(String id){
		String[] parts = id.split("/"); 
		String uuid = parts[3];
		return uuid;
	}
	
}
