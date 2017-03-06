package it.cfalchi.alfrescorest.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CmisClient {
	
	private static Logger logger = LoggerFactory.getLogger(CmisClient.class);
	
	private static final String ALFRESCO_ATOMPUB_URL = "http://localhost:8080/alfresco/cmisatom";
	
	// utente che si connette ad Alfresco
	private String user;
	private Session session;
	
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

	public String getUser() {
		return user;
	}
	
	/**
	 * Ottiene un oggetto CMIS, dato il nome della risorsa e la cartella in cui si trova:
	 * se esiste la risorsa, restituisce l'oggetto.
	 * 
	 * @param parentFolder
	 * @param name
	 * @return object
	 */
	public CmisObject getObject(Folder parentFolder, String name){
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

}
