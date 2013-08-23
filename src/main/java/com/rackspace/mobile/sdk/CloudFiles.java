package com.rackspace.mobile.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

import com.rackspace.mobile.exception.CloudFileHttpResponseException;
import com.rackspace.mobile.saxhandler.ContainerSaxHandler;
import com.rackspace.mobile.saxhandler.ContainerSaxHandler.Container;
import com.squareup.okhttp.OkHttpClient;

public class CloudFiles {

	public static final String PUBLIC_ADDRESS_STR="public0_v4";
	public static final String PRIVATE_JSON_IP_ADDRESS="private0_v4";
	public static final String ZONE = "DFW";

	public static final String X_STORAGE_TOKEN="X-Storage-Token";
	public static final String X_STORAGE_URL="X-Storage-Url";
	public static final String X_SERVER_MANAGEMENT_URL="X-Server-Management-Url";
	public static final String X_CDN_MANAGEMENT_URL="X-CDN-Management-Url";
	public static final String X_AUTH_TOKEN="X-Auth-Token";
	public static final String DOWN_LOAD_DIR="Download";

	private String username;
	private String apiKey;
	private String xAuthToken;
	private String xStorageUrl;
	private String xCdnUrl;
	private String xStorageToken;
	private String xServerMangementUrl;
	 

	private static final boolean debug=false;

	public CloudFiles(String username, String apiKey){
		this.username=username;
		this.apiKey=apiKey;
		this.setAuthentication();
	}

	private void setAuthentication(){
		Map<String, String>authMap=this.getCloudFilesAuthentication();
		this.xAuthToken=authMap.get(X_AUTH_TOKEN);
		this.xStorageUrl=authMap.get(X_STORAGE_URL);
		this.xCdnUrl=authMap.get(X_CDN_MANAGEMENT_URL);
		this.xStorageToken=authMap.get(X_STORAGE_TOKEN);
		this.xServerMangementUrl=authMap.get(X_SERVER_MANAGEMENT_URL);
	}

	public String uploadFile(String container, List<File> theFiles)throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.uploadFile()";
		String retVal="";
		if(debug){
			Log.d(METHOD_NAME," START: container="+container);		
		}
		if(null==this.xAuthToken||this.xAuthToken.isEmpty()||this.xStorageUrl==null||this.xStorageUrl.isEmpty()){
			this.setAuthentication();
		}
		if(debug){
			Log.d(METHOD_NAME,"xAuthToken="+this.xAuthToken);
			Log.d(METHOD_NAME,"xStorageUrl="+this.xStorageUrl);	
		}
		if(this.xAuthToken!=null&&!this.xAuthToken.isEmpty()&&this.xStorageUrl!=null&&!this.xStorageUrl.isEmpty()){
			if(debug){
				Log.d(METHOD_NAME,"theFile="+theFiles);
			}
			String theXStorageUrl=this.xStorageUrl;
			if(!theXStorageUrl.endsWith("/")){
				theXStorageUrl+="/";
			}
			theXStorageUrl+=(container+"/");
			if(debug){
				Log.d(METHOD_NAME,"theXStorageUrl="+theXStorageUrl);
			}
			if(null!=theFiles){
				if(debug){
					Log.d(METHOD_NAME,"theFiles.size()="+theFiles.size());
				}
				for(File theFile:theFiles){
					String anXStorageUrl=(theXStorageUrl+theFile.getName());
					if(debug){
						Log.d(METHOD_NAME,"anXStorageUrl="+anXStorageUrl);
					}
					retVal+=putFileIntoStorage(xAuthToken, anXStorageUrl, theFile);
				}
			}
		}
		else{
			if(this.xAuthToken==null||this.xAuthToken.isEmpty()){
				throw new CloudFileHttpResponseException("CloudFileHttpResponseException from CloudFiles.uploadFile(): xAuthToken is null or empty");
			}
			if(this.xStorageUrl==null||this.xStorageUrl.isEmpty()){
				throw new CloudFileHttpResponseException("CloudFileHttpResponseException from CloudFiles.uploadFile(): xStorageUrl is null or empty");
			}
		}
		return retVal;
	}

	private Map<String,String> getCloudFilesAuthentication(){
		String METHOD_NAME="CloudFiles.getCloudFileAuthentication()";	
		if(debug){
			Log.d(METHOD_NAME,"START: ");
		}
		Map<String, String>retVal=new HashMap<String, String>();
		OkHttpClient okHttpClient=new OkHttpClient();

		StringBuffer jsonResponse=null;	
		InputStream inny=null;

		int responseCode;
		try {
			if(null==this.username){
				//Change to actual user name
				this.username="*******";
			}
			if(null==this.apiKey){
				//Change to actual apikey
				this.apiKey="********";
			}

			URL theURL = new URL("https://identity.api.rackspacecloud.com/v1.0");
			HttpURLConnection httpConn =okHttpClient.open(theURL);		
			httpConn.setRequestMethod("GET");

			httpConn.addRequestProperty("X-Auth-User", this.username);
			httpConn.addRequestProperty("X-Auth-Key", this.apiKey);
			if(debug){
				Log.d(METHOD_NAME,": ~~~~~getCloudFilesAuthentication(): this.username="+this.username);
				Log.d(METHOD_NAME,": ~~~~~getCloudFilesAuthentication(): this.apiKey="+this.apiKey);
				Log.d(METHOD_NAME,": ~~~~~getCloudFilesAuthentication(): httpConn.getRequestMethod()="+
						httpConn.getRequestMethod());
			}
			responseCode = httpConn.getResponseCode();

			if(!(responseCode>=200 && responseCode<=299)){
				if(debug){
					Log.d(METHOD_NAME,": responseCode="+responseCode);
				}
				throw new RuntimeException(METHOD_NAME+": Failed: HTTP error code: "+responseCode);
			}

			String xStorageToken=httpConn.getHeaderField(X_STORAGE_TOKEN);
			String xStorageUri=httpConn.getHeaderField(X_STORAGE_URL);
			String xServerManagementUri=httpConn.getHeaderField(X_SERVER_MANAGEMENT_URL);
			String xCdnManagementUri=httpConn.getHeaderField(X_CDN_MANAGEMENT_URL);
			String xAuthToken=httpConn.getHeaderField(X_AUTH_TOKEN);

			retVal.put(X_STORAGE_TOKEN, xStorageToken);
			retVal.put(X_STORAGE_URL, xStorageUri);
			retVal.put(X_SERVER_MANAGEMENT_URL, xServerManagementUri);
			retVal.put(X_CDN_MANAGEMENT_URL, xCdnManagementUri);
			retVal.put(X_AUTH_TOKEN, xAuthToken);

			inny=httpConn.getInputStream();

			int readInt=-1;
			char readChar=' ';
			jsonResponse=new StringBuffer("");
			while(-1!=(readInt=inny.read())){
				readChar=(char)readInt;
				jsonResponse.append(readChar);
			}		
			inny.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			Log.d(METHOD_NAME," IOException exception: "+e.getMessage(),e);			
		}
		catch(Throwable e){
			e.printStackTrace();
			Log.e(METHOD_NAME," Throwable exception: "+e.getMessage(),e);
		}
		if(debug){
			Log.d(METHOD_NAME,"retVal="+retVal);
		}
		return retVal;
	}

	public String putFileIntoStorage(String xAuthToken, String xStorageUrl, File theFile)throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.putFileIntoStorage()";
		if(debug){
			Log.d(METHOD_NAME,": START: xAuthToken="+xAuthToken+" xStorageUrl="+xStorageUrl+" theFile="+theFile);
		}
		String retVal="";
		if(null!=theFile){
			try {
				PutMethod put=new PutMethod(xStorageUrl);
				put.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(theFile)));				
				put.addRequestHeader("X-Auth-Token", xAuthToken);
				put.addRequestHeader("Content-Type", "image/png");


				HttpClient client = new HttpClient();
				client.executeMethod(put);
				retVal=put.getResponseBodyAsString();
				if(debug){
					Log.d(METHOD_NAME,retVal);
				}
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
				Log.e(METHOD_NAME, e.getMessage(), e);
				throw new CloudFileHttpResponseException(e);
			}
			catch(IOException e){
				e.printStackTrace();
				Log.e(METHOD_NAME, e.getMessage(), e);				
				throw new CloudFileHttpResponseException(e);
			}
			catch (Throwable e){
				e.printStackTrace();
				Log.e(METHOD_NAME, e.getMessage(), e);
				throw new CloudFileHttpResponseException(e);
			}
		}

		if(null!=retVal){
			retVal+="\n";
		}
		if(debug){
			Log.d(METHOD_NAME, ": END:");
		}
		return retVal;
	}

	public List<ACloudFile>getStorageFilesInContainer(String container)throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.getFilesInContainer()";
		if(debug){
			Log.d(METHOD_NAME, ": START: container="+container);
		}
		List<ACloudFile>retVal=new ArrayList<ACloudFile>();
		
		JSONArray jsonArr=listStorageFilesForContainer(container);
		for(int i=0;i<jsonArr.length();++i){
			JSONObject aJsonObj;
			try {
				aJsonObj = jsonArr.getJSONObject(i);
				String hash=aJsonObj.getString("hash");
				String lastModified=aJsonObj.getString("last_modified");
				String bytes=aJsonObj.getString("bytes");
				String name=aJsonObj.getString("name");
				String contentType=aJsonObj.getString("content_type");
				if(debug){
					Log.d(METHOD_NAME, " hash="+hash);
					Log.d(METHOD_NAME, " lastModified="+lastModified);
					Log.d(METHOD_NAME, " bytes="+bytes);
					Log.d(METHOD_NAME, " name="+name);
					Log.d(METHOD_NAME, " contentType="+contentType);
				}				
				ACloudFile aFile=new ACloudFile(hash, lastModified, bytes, name, contentType);
				retVal.add(aFile);
			} 
			catch (JSONException e) {				
				e.printStackTrace();
				Log.e(METHOD_NAME, "JSONException caught: "+e.getMessage());
				throw new CloudFileHttpResponseException("JSON Error: "+e.getMessage());
			}
			catch(Throwable e){
				Log.e(METHOD_NAME, "Throwable caught: "+e.getMessage());
				throw new CloudFileHttpResponseException("Throwable JSON Error: "+e.getMessage());				
			}

		}
		
		return retVal;
	}

	public JSONArray listStorageFilesForContainer(String container)throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.listFilesForContainer()";
		if(debug){
			Log.d(METHOD_NAME,": START:");
		}
		JSONArray retVal=new JSONArray();

		if(null==this.xAuthToken||this.xAuthToken.isEmpty()||this.xStorageUrl==null||this.xStorageUrl.isEmpty()){
			this.setAuthentication();
		}
		if(debug){
			Log.d(METHOD_NAME,"xAuthToken="+this.xAuthToken);
			Log.d(METHOD_NAME,"xStorageUrl="+this.xStorageUrl);		
		}
		if(this.xAuthToken!=null&&!this.xAuthToken.isEmpty()&&this.xStorageUrl!=null&&!this.xStorageUrl.isEmpty()){
			String theUrl=this.xStorageUrl;
			if(!theUrl.endsWith("/")){
				theUrl+="/";
			}
			theUrl+=(container+"?format=json");
			if(debug){
				Log.d(METHOD_NAME,"theUrl="+theUrl);
			}
			URL theURL;
			try {
				theURL = new URL(theUrl);
				OkHttpClient okHttpClient=new OkHttpClient();
				HttpURLConnection httpConn =okHttpClient.open(theURL);
				
				httpConn.setRequestMethod("GET");
				httpConn.addRequestProperty("X-Auth-Token", this.xAuthToken);
				httpConn.addRequestProperty("X-Auth-User",this.username);
				httpConn.addRequestProperty("X-Auth-Key", this.apiKey);

				if(debug){
					Log.d(METHOD_NAME,": ~~~~~this.xAuthToken="+this.xAuthToken);
					Log.d(METHOD_NAME,": ~~~~~this.username="+this.username);
					Log.d(METHOD_NAME,": ~~~~~this.apiKey="+this.apiKey);
					Log.d(METHOD_NAME,": ~~~~~httpConn.getRequestMethod()="+				
							httpConn.getRequestMethod());
				}
				int responseCode = httpConn.getResponseCode();

				if(!(responseCode>=200 && responseCode<=299)){
					if(debug){
						Log.d(METHOD_NAME,": responseCode="+responseCode);
					}
					throw new CloudFileHttpResponseException("Failed: HTTP error code: "+responseCode+" ");
				}

				StringBuffer retBuff=new StringBuffer("");
				InputStream inny=httpConn.getInputStream();

				int readInt=-1;
				char readChar=' ';

				while(-1!=(readInt=inny.read())){
					readChar=(char)readInt;
					retBuff.append(readChar);
				}		
				inny.close();
				try {
					retVal=new JSONArray(retBuff.toString());
				} 
				catch (JSONException e) {
					e.printStackTrace();					
					Log.e(METHOD_NAME," JSONException caught: ",e);
					throw new CloudFileHttpResponseException("JSON error: "+e.getMessage());
					
				}				
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
				Log.e(METHOD_NAME," MalformedURLException caught: ",e);
				throw new CloudFileHttpResponseException("MalformedURL error: "+e.getMessage());
			}
			catch (ProtocolException e) {
				e.printStackTrace();
				Log.e(METHOD_NAME," ProtocolException caught: ",e);
				throw new CloudFileHttpResponseException("Protocol error: "+e.getMessage());
			}
			catch(IOException e){
				e.printStackTrace();
				Log.e(METHOD_NAME," IOException caught: ",e);
				throw new CloudFileHttpResponseException("IO error: "+e.getMessage());
			}
			catch(Throwable e){
				e.printStackTrace();
				Log.e(METHOD_NAME," IOException caught: ",e);
				throw new CloudFileHttpResponseException("Throwable error: "+e.getMessage());				
			}

		}
		else{
			if(this.xAuthToken==null||this.xAuthToken.isEmpty()){
				throw new CloudFileHttpResponseException(METHOD_NAME+": xAuthToken is null or empty");
			}
			if(this.xStorageUrl==null||this.xStorageUrl.isEmpty()){
				throw new CloudFileHttpResponseException(METHOD_NAME+": xStorageUrl is null or empty");
			}			
		}
		if(debug){
			Log.d(METHOD_NAME,": END: retVal.length()="+retVal.length());
		}
		return retVal;
	}
	
	
	private HttpURLConnection getGetHttpUrlConnection(String url)throws MalformedURLException, ProtocolException {
		HttpURLConnection retVal=null;
		OkHttpClient okHttp=new OkHttpClient();

		retVal=okHttp.open(new URL(url));
		retVal.setRequestMethod("GET");
		retVal.addRequestProperty("X-Auth-Token", this.xAuthToken);
		retVal.addRequestProperty("X-Auth-User",this.username);
		retVal.addRequestProperty("X-Auth-Key", this.apiKey);

		return retVal;
	}
	
	public List<Container> getContainers()throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.getContainers()";
		if(debug){
			Log.d(METHOD_NAME,": START : this.xStorageUrl="+this.xStorageUrl);
		}		
		List<Container> retVal=new ArrayList<Container>();
		
		if(null!=this.xStorageUrl){


			try {
				String theUrl=(this.xStorageUrl+"?format=json");
				if(debug){
					Log.d(METHOD_NAME,":~~~~~~theUrl="+theUrl);
				}
				HttpURLConnection urlConn=this.getGetHttpUrlConnection(theUrl);

				if(debug){
					Log.d(METHOD_NAME," : ~~~~~this.xAuthToken="+this.xAuthToken);
					Log.d(METHOD_NAME," : ~~~~~this.username="+this.username);
					Log.d(METHOD_NAME," : ~~~~~this.apiKey="+this.apiKey);
					Log.d(METHOD_NAME," : ~~~~~httpConn.getRequestMethod()="+				
							urlConn.getRequestMethod());
				}				
				try {
					int responseCode = urlConn.getResponseCode();

					if(!(responseCode>=200 && responseCode<=299)){
						if(debug){
							Log.d(METHOD_NAME," : responseCode="+responseCode);
						}
						throw new CloudFileHttpResponseException(METHOD_NAME+": Failed: HTTP error code: "+responseCode);
					}					
					InputStream inny=urlConn.getInputStream();
					StringBuffer retBuff=new StringBuffer("");
					int readInt=-1;
					char readChar=' ';

					while(-1!=(readInt=inny.read())){
						readChar=(char)readInt;
						retBuff.append(readChar);
					}		
					inny.close();
					try {
						if(debug){
							Log.d(METHOD_NAME,"~~~~~~~~~~~retBuff.toString()=\n"+retBuff.toString());
						}

						JSONArray containersJson=new JSONArray(retBuff.toString());
						if(debug){
							Log.d(METHOD_NAME,": containersJson.length="+containersJson.length());
						}
						for(int i=0;i<containersJson.length();++i){
							JSONObject aContainerJsonObj=containersJson.getJSONObject(i);
							ContainerSaxHandler.Container container=new ContainerSaxHandler().new Container();
							container.setName(null);
							container.setCount(null);
							container.setBytes(null);
							if(debug){
								Log.d(METHOD_NAME,": aContainerJsonObj.getString(\"name\")="+aContainerJsonObj.getString("name"));
							}
							if(aContainerJsonObj.has("name")){
							    container.setName(aContainerJsonObj.getString("name"));
							}
							if(aContainerJsonObj.has("count")){
								container.setCount(aContainerJsonObj.getLong("count"));
							}
							if(aContainerJsonObj.has("bytes")){
								container.setBytes(aContainerJsonObj.getLong("bytes"));
							}
							retVal.add(container);
						}						
					} 
					catch (JSONException e) {
						e.printStackTrace();
						Log.d(METHOD_NAME,"JSONException caught: ",e);
					}					
				} 
				catch (IOException e) {
					e.printStackTrace();
					Log.e(METHOD_NAME," IOException: ",e);
				}			
			} 
			catch (MalformedURLException e){
				e.printStackTrace();				
				Log.e(METHOD_NAME," MalformedURLException: "+e);
			}
			catch(ProtocolException e){
				e.printStackTrace();
				Log.e(METHOD_NAME," ProtocolException: "+e);
			}
		}
		if(debug){
			Log.d(METHOD_NAME,": END : retVal.size()="+retVal.size());
		}	
		return retVal;
	}
	
	
	public void downloadStorageFileFromContainer(String container, String fileName) throws CloudFileHttpResponseException{
		String METHOD_NAME="CloudFiles.getFileFromContainer()";
		if(debug){
			Log.d(METHOD_NAME,": START : container="+container+" fileName="+fileName);
		}
		if(null!=container && !container.isEmpty() && null!=fileName && !fileName.isEmpty()){
			if(debug){
				Log.d(METHOD_NAME,": this.xStorageUrl="+this.xStorageUrl);
			}
			if(null!=this.xStorageUrl && !this.xStorageUrl.isEmpty()){
				String url=this.xStorageUrl;
				if(!url.endsWith("/")){
					url+="/";
				}
				url+=container;
				if(!url.endsWith("/")){
					url+="/";
				}
				url+=fileName;
				if(debug){
					Log.d(METHOD_NAME," url="+url);
				}

				try {
					HttpURLConnection urlConn=this.getGetHttpUrlConnection(url);
					try{
						int responseCode = urlConn.getResponseCode();

						if(!(responseCode>=200 && responseCode<=299)){
							if(debug){
								Log.d(METHOD_NAME," : responseCode="+responseCode);
							}
							throw new CloudFileHttpResponseException(METHOD_NAME+": Failed: HTTP error code: "+responseCode);
						}
						InputStream inny=urlConn.getInputStream();
						//StringBuffer retBuff=new StringBuffer("");
						int readInt=-1;
						//char readChar=' ';
						File storageDir=new File(Environment.getExternalStorageDirectory(),DOWN_LOAD_DIR);

						if(debug){
							Log.d(METHOD_NAME,": storageDir.exists()="+storageDir.exists());
							Log.d(METHOD_NAME,": storageDir.getAbsolutePath()="+storageDir.getAbsolutePath());
						}
						if(storageDir.exists()){
							String downloadFile=storageDir.getAbsolutePath();
							if(!downloadFile.endsWith("/")){
								downloadFile+="/";
							}
							downloadFile+=fileName;	
							if(debug){
								Log.d(METHOD_NAME,": downloadFile="+downloadFile);							
							}
							File downloadFileFile=new File(downloadFile);
							FileOutputStream downloadFileOutputStream=new FileOutputStream(downloadFileFile);
							while(-1!=(readInt=inny.read())){
								//readChar=(char)readInt;
								//retBuff.append(readChar);
								downloadFileOutputStream.write(readInt);
							}
							downloadFileOutputStream.close();
							inny.close();	
						}						
					}
					catch(IOException e){
						e.printStackTrace();
						Log.e(METHOD_NAME,": IOException: ",e);
					}
					catch(Throwable e){
						e.printStackTrace();
						Log.e(METHOD_NAME,": Throwable: ",e);
					}
				} 
				catch (MalformedURLException e) {
					e.printStackTrace();
					Log.e(METHOD_NAME,": MalformedURLException: ",e);
				}
				catch(ProtocolException e){
					e.printStackTrace();
					Log.e(METHOD_NAME,": ProtocolException: ",e);
				}
				catch(Throwable e){
					e.printStackTrace();
					Log.e(METHOD_NAME,": Throwable: ",e);
				}
			}
		}
		if(debug){
			Log.d(METHOD_NAME,": END ");
		}
	}
	
	
	
}
