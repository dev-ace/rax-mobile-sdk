package com.rackspace.mobile.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ContainerSaxHandler extends DefaultHandler {
	
	private List<ContainerSaxHandler.Container>containerList;
	
	private ContainerSaxHandler.Container theContainer;
	private boolean hasContainerName;	
	private boolean hasNumberOfFilesInContainer;
	private boolean hasSizeOfContainer;
	private boolean endOfContainer;
	
	public void startElement(String uri, String localName,String qName, 
			Attributes attributes) throws SAXException{
		if(qName.equals("account")){
			this.containerList=new ArrayList<ContainerSaxHandler.Container>();
		}
		if(qName.equalsIgnoreCase("name")){
			this.hasContainerName=true;
		}
		if(qName.equalsIgnoreCase("container")){
			 this.theContainer=new ContainerSaxHandler.Container();
		}
		if(qName.equalsIgnoreCase("name")){
			this.hasContainerName=true;
		}
		if(qName.equalsIgnoreCase("count")){
			this.hasNumberOfFilesInContainer=true;
		}
		if(qName.equalsIgnoreCase("bytes")){
			this.hasSizeOfContainer=true;
		}			
	}
	
	public void characters(char ch[], int start, int length)
			throws SAXException {
		if(this.hasContainerName){
			this.theContainer.setName(new String(ch,start,length));
			this.hasContainerName=false;
		}
		if(this.hasNumberOfFilesInContainer){
			this.theContainer.setCount(new Long(new String(ch,start,length)));
			this.hasNumberOfFilesInContainer=false;
		}
		if(this.hasSizeOfContainer){
			this.theContainer.setBytes(new Long(new String(ch,start,length)));
			this.hasSizeOfContainer=false;
		}
		if(this.endOfContainer){
			this.containerList.add(this.theContainer);
		}
	}
	
	public List<ContainerSaxHandler.Container>getContainersList(){
	   	return this.containerList;
	}
	
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		if(qName.equalsIgnoreCase("container")){
			this.endOfContainer=true;
		}
	}
	
	
	public class Container{
		private String name;
		private Long count;
		private Long bytes;
		
		public Container(){
			this.name=null;
			this.count=null;
			this.bytes=null;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}

		public Long getBytes() {
			return bytes;
		}

		public void setBytes(Long bytes) {
			this.bytes = bytes;
		}
		
		public String toString(){
			StringBuffer retVal=new StringBuffer("");
			retVal.append("{");
			if(null!=this){
				retVal.append("name=");
				if(null!=this.name){				
					retVal.append(this.name);				
				}
				else{
					retVal.append("null");
				}
				retVal.append(", count=");
				if(null!=this.count){
					retVal.append(this.count);
				}
				else{
					retVal.append("null");
				}
				retVal.append(", size=");
				if(null!=this.bytes){
					retVal.append(this.bytes);
				}
				else{
					retVal.append("null");
				}
			}
			retVal.append("}");
			return retVal.toString();
		}				
	}
	
}
