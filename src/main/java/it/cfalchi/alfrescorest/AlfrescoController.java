package it.cfalchi.alfrescorest;

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
import org.springframework.web.bind.annotation.ResponseBody;

import it.cfalchi.alfrescorest.utils.AlfrescoRestURIConstants;
import it.cfalchi.alfrescorest.utils.CmisClient;
import it.cfalchi.alfrescorest.utils.RequestMessage;


@Controller
public class AlfrescoController {
	
	private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);
		
	@RequestMapping(value = AlfrescoRestURIConstants.REQUEST_GET_DOC, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> getDocumentService (@RequestBody RequestMessage message) throws IOException {
		logger.info("Start getDocumentService");
		
		byte[] out = null;
		Document doc = null;
		ResponseEntity<byte[]> response = null;
		
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Map<String,Object> request = message.getRequest();
		String uuid = (String) request.get("uuid");
		String path = (String) request.get("path");
		if(uuid!= null || path!=null){
			doc = cmisClient.getDocumentByUUID(uuid, path);
			if(doc!=null){
				ContentStream contentStream = doc.getContentStream();
				InputStream stream = contentStream.getStream();
				out=IOUtils.toByteArray(stream);
				HttpHeaders responseHeaders = new HttpHeaders();
		        responseHeaders.add("content-disposition", "attachment; filename=" + doc.getName());
		        responseHeaders.add("Content-Type",doc.getContentStreamMimeType());
				response = new ResponseEntity<byte[]>(out, responseHeaders, HttpStatus.OK);
				logger.info("End getDocumentService");
			} else {
				response = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
				logger.error("Document not found!");
			}
		} else {
			response = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
			logger.error("Request not valid!");
		}
		return response;
	}
	
}
