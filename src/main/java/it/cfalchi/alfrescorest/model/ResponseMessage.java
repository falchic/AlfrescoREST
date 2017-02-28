package it.cfalchi.alfrescorest.model;

import java.util.HashMap;
import java.util.Map;

public class ResponseMessage {
	
	private String code;
	private String message;
	private Map<String,Object> attributes;
	
	public ResponseMessage (){
		this.attributes = new HashMap<>();
		this.code = "000";
		this.message = "";
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(String key) {
		Object value = attributes.get(key);
		return value;
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
