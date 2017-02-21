package it.cfalchi.alfrescorest.model;

import java.io.Serializable;

public class DocumentRetrived implements Serializable{
	
	private static final long serialVersionUID = 4577487803583482111L;
	
	private String uuid;
	private String name;
	private byte[] content;
	
	public DocumentRetrived(String uuid, String name){
		this.uuid=uuid;
		this.name=name;
	}
	
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
