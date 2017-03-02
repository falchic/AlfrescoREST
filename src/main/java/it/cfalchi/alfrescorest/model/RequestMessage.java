package it.cfalchi.alfrescorest.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class RequestMessage {
	
	private String user;
	private String password;
	private Map<String, Object> request;
	
	public RequestMessage(){
		
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Map<String, Object> getRequest() {
		return request;
	}
	
	public void setRequest(Map<String, Object> request) {
		this.request = request;
	}
	
	public boolean isValid(boolean isGetDoc){
		if(user==null && password==null){
			return false;
		} else {
			if(isGetDoc){
				if(!(request.get("destination")==null) || !(request.get("uuid")==null)){
					return true;
				} else {
					return false;
				}
			} else {
				for (Map.Entry<String, Object> entry : request.entrySet()){
				    if(entry.getValue()==null)
				    	return false;
				}
			}
		}
		return true;
	}
}