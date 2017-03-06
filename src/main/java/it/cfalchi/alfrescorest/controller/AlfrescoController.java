package it.cfalchi.alfrescorest.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import it.cfalchi.alfrescorest.model.RequestMessage;
import it.cfalchi.alfrescorest.model.ResponseMessage;
import it.cfalchi.alfrescorest.service.AlfrescoService;
import it.cfalchi.alfrescorest.utils.AlfrescoRestURIConstants;
import it.cfalchi.alfrescorest.utils.RequestConstants;


@Controller
public class AlfrescoController {
	
	@Autowired
	AlfrescoService alfrescoService;
	
	private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination": "",
	 *			"title": "",
	 *			"description": ""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_CREATE_DOC, method = RequestMethod.POST, consumes = {"multipart/form-data"})
	@ResponseBody
	public ResponseMessage createDocumentService (@RequestPart("message") RequestMessage message, @RequestPart("file") MultipartFile file) throws IOException{
		logger.info("Start createDocumentService");
		
		ResponseMessage response = new ResponseMessage();
		boolean isValid = message.isValid(false);
		if(isValid && !file.isEmpty()){
			Map<String,Object> request = message.getRequest();		
			request.put(RequestConstants.DOC_NAME, file.getOriginalFilename());
			request.put(RequestConstants.DOC_MIME_TYPE, file.getContentType());
			request.put(RequestConstants.FILE_LENGTH, file.getSize());
			InputStream inputStream = new BufferedInputStream(file.getInputStream());
		    request.put("attachment", inputStream);
		    message.setRequest(request);
		    response = alfrescoService.createDocument(message);
		} else {
			response.setCode("400");
	    	response.setMessage("Bad request");
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
	@ResponseBody
	public ResponseMessage createFolderService (@RequestBody RequestMessage message){
		logger.info("Start createFolderService");
		
		ResponseMessage response = new ResponseMessage();
		
		boolean isValid = message.isValid(false);
		if(isValid){
		    response = alfrescoService.createFolder(message);
		} else {
			response.setCode("400");
	    	response.setMessage("Bad request");
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
	@ResponseBody
	public ResponseEntity<InputStreamResource> getDocumentService (@RequestBody RequestMessage message) throws IOException{
		logger.info("Start getDocumentService");
		
		Document doc = null;
		ResponseEntity<InputStreamResource> response = null;
		
		boolean isValid = message.isValid(true);
		if(isValid){
			doc = alfrescoService.getDocumentByUUIDPath(message);
			if(doc!=null){
				ContentStream contentStream = doc.getContentStream();
				InputStreamResource inputStreamResource = new InputStreamResource(contentStream.getStream());
				HttpHeaders responseHeaders = new HttpHeaders();
			    responseHeaders.setContentLength(doc.getContentStreamLength());
		        responseHeaders.add("content-disposition", "attachment; filename=" + doc.getName());
		        responseHeaders.add("Content-Type",doc.getContentStreamMimeType());
				response = new ResponseEntity<InputStreamResource>(inputStreamResource, responseHeaders, HttpStatus.OK);
			} else {
				response = new ResponseEntity<InputStreamResource>(HttpStatus.NOT_FOUND);
			}
		} else {
			response = new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
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
	@ResponseBody
	public ResponseMessage getDocumentsFolderService (@RequestBody RequestMessage message){
		logger.info("Start getDocumentsFolderService");

		ResponseMessage response = new ResponseMessage();
		
		boolean isValid = message.isValid(false);
		if(isValid){
			response = alfrescoService.getDocumentsByFolder(message);
		} else {
			response.setCode("400");
	    	response.setMessage("Bad request");
			logger.error("Request not valid!");
		}
		logger.info("End getDocumentsFolderService");
		return response;
	}
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination":""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_REMOVE_FOLDER, method = RequestMethod.POST)
	@ResponseBody
	public ResponseMessage removeFolderService (@RequestBody RequestMessage message){
		logger.info("Start removeFolderService");
		
		ResponseMessage response = new ResponseMessage();
		
		boolean isValid = message.isValid(false);
		if(isValid){
		    response = alfrescoService.removeFolder(message);
		} else {
			response.setCode("400");
	    	response.setMessage("Bad request");
			logger.error("Request not valid!");
		}
		logger.info("Stop removeFolderService");
		return response;
	}
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"uuid_list":""
	 *		}
	 *	}
	 */
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_REMOVE_DOCS, method = RequestMethod.POST)
	@ResponseBody
	public ResponseMessage removeDocumentsService (@RequestBody RequestMessage message){
		logger.info("Start removeDocumentsService");
		
		ResponseMessage response = new ResponseMessage();
		
		boolean isValid = message.isValid(false);
		if(isValid){
		    response = alfrescoService.removeDocuments(message);
		} else {
			response.setCode("400");
	    	response.setMessage("Bad request");
			logger.error("Request not valid!");
		}
		logger.info("Stop removeDocumentsService");
		return response;
	}
	
}
