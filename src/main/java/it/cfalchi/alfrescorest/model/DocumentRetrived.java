package it.cfalchi.alfrescorest.model;

import java.io.InputStream;

public class DocumentRetrived {
	
	private String uuid;
	private String name;
	private InputStream content;
	private long size;
	
	public DocumentRetrived(String uuid, String name){
		this.uuid=uuid;
		this.name=name;
	}
	
	public InputStream getContent() {
		return content;
	}
	
	public void setContent(InputStream content) {
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
