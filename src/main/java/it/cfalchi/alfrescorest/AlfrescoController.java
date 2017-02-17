package it.cfalchi.alfrescorest;

import java.io.File;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public @ResponseBody File getDocumentService (@RequestBody RequestMessage message) {
		logger.info("Start getDocumentService");
		CmisClient cmisClient = new CmisClient(message.getUser(), message.getPassword());
		Map<String,Object> request = message.getRequest();
		String uuid = (String) request.get("uuid");
		Document doc = cmisClient.getDocumentByUUID(uuid, "");
		File file = null;	
		return file;
	}
	
}
