package it.cfalchi.alfrescorest.utils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

public class RequestMessage implements Serializable{

	private static final long serialVersionUID = -7788619177798333712L;
	
	private String user;
	private String password;
	private Map<String, Object> request;
	
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
	
}