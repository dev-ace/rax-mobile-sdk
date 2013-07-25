package com.rackspace.mobile.sdk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ACloudFile {
	String hash;
	String last_modified;
	String bytes;
	String name;
	String content_type;
	long   last_modified_long;
	
	public ACloudFile(String hash, String last_modified, String bytes, String name, String content_type){
		this.hash=hash;
		this.last_modified=last_modified;
		this.bytes=bytes;
		this.name=name;
		this.content_type=content_type;
		if(null!=last_modified){
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		    try {
				Date theDate=sdf.parse(last_modified);
				this.last_modified_long=theDate.getTime();
			} 
		    catch (ParseException e) {
		    	
				this.last_modified_long=-1;
			}
		}
	}
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getLast_modified() {
		return last_modified;
	}
	public void setLast_modified(String last_modified) {
		this.last_modified = last_modified;
	}
	public String getBytes() {
		return bytes;
	}
	public void setBytes(String bytes) {
		this.bytes = bytes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent_type() {
		return content_type;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}
	
	public String toString(){
		StringBuffer aStr=new StringBuffer("");
		aStr.append("{");
		
		aStr.append("hash=");
		aStr.append(hash);
		aStr.append(",");
		
		aStr.append("last_modified=");
		aStr.append(last_modified);
		aStr.append(",");
		
		aStr.append("name=");
		aStr.append(name);
		aStr.append(",");
		
		aStr.append("bytes=");
		aStr.append(bytes);
		aStr.append(",");
		
		aStr.append("content_type=");
		aStr.append(content_type);
		aStr.append(",");
		
		aStr.append("last_modified_long=");
		aStr.append(last_modified_long);		
		
		aStr.append("}");
		return aStr.toString();
	}

}
