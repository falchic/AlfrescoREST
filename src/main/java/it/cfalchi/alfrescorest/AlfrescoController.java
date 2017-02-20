package it.cfalchi.alfrescorest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
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

import it.cfalchi.alfrescorest.utils.AlfrescoRestURIConstants;
import it.cfalchi.alfrescorest.utils.CmisClient;
import it.cfalchi.alfrescorest.utils.RequestMessage;


@Controller
public class AlfrescoController {
	
	private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);
	
	/**
	 *  Messaggio di richiesta:
	 * {
	 *		"user":"admin",
	 *		"password":"alfresco",
	 *		"request":{
	 *			"destination":"",
	 *			"name": "",
	 *			"title" "",
	 *			"description": "",
	 *			"mime_type" ""
	 *		}
	 *	}
	 */
	//TODO dove lo metto il file?!?!!?
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_CREATE_DOC, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> createDocumentService (@RequestBody RequestMessage message, 
			@RequestParam("file") MultipartFile file){
		logger.info("Start createDocumentService");
		
		ResponseEntity<String> response = null;
		
		boolean isValid = message.isValid(false);
		if(isValid){
			CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
			Map<String,Object> request = message.getRequest();
			try {
				byte[] in = file.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(file.getName())));
				stream.write(in);
		        stream.close();
		        request.put("attachment", stream);
		        request.put("length", file.getSize());
		        cmisClient.createDocument(request);
			} catch (IOException e) {
				logger.error("Upload failed!" + e.getMessage());
			}
			response = new ResponseEntity<String>(HttpStatus.CREATED);
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
	 *			"uuid":"e63f8ee3-6e64-4694-9cde-4998a24f65a8;1.0",
	 *			"path": ""
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
			String uuid = (String) request.get("uuid");
			String path = (String) request.get("path");
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
				logger.error("Document not found!");
			}
		} else {
			response = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		logger.info("End getDocumentService");
		return response;
	}
	
}
