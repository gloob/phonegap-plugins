package org.apache.cordova.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

import org.apache.cordova.json4j.JSONObject;
import org.apache.cordova.json4j.JSONArray;
import org.apache.cordova.json4j.JSONException;

import net.sf.zipme.ZipArchive;
import net.sf.zipme.ZipException;
import net.sf.zipme.ZipInputStream;
import net.sf.zipme.ZipEntry;

public class Zip extends Plugin {

	private static final String LOG_TAG = "Zip";
	private static final String DIRECTORY_SEPARATOR = "/";
	//private static final int BUFFER_SIZE = 2048;

	//private List<String> processedEntities = new ArrayList<String>();

	/**
	* Executes the request and returns PluginResult.
	*
	* @param action		The action to execute.
	* @param args		JSONArry of arguments for the plugin.
	* @param callbackId	The callback id used when calling back into JavaScript.
	* @return		A PluginResult object with a status and message.
	*/
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";

		try {
			// Parse common args
			String source = args.getString(0);

			if (action.equals("info")) {

				return new PluginResult(status, this.info(source));

			} else if (action.equals("compress")) {

				String target = args.getString(1);

				return new PluginResult(status, result);
				/*
				if (this.uncompress(source, target, callbackId)) {
					return new PluginResult(status, result);
				} else {
					return new PluginResult(PluginResult.Status.ERROR, result);
				}
				*/
			} else if (action.equals("uncompress")) {

				String target = args.getString(1);

				JSONObject ret = this.uncompress(source, target, callbackId);

				// Purge action only data structures.
				//this.processedEntities.clear();

				return new PluginResult(PluginResult.Status.OK, ret);
				/*
				if (this.uncompress(source, target, callbackId)) {
					return new PluginResult(status, result);
				} else {
					return new PluginResult(PluginResult.Status.ERROR, result);
				}
				*/
			}
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
		} catch (IOException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());			
		} catch (InterruptedException e) {
			return new PluginResult(PluginResult.Status.IO_EXCEPTION, e.getMessage());
		}
	}

	/**
	* Info.
	*
	* @param source		The action to execute.
	* @return		A ZipFileEntry structure.
	*/
	private JSONObject info(String source) throws JSONException, ZipException, IOException {

        int entries = this.getEntries(source);
        
		JSONObject zipInfo = new JSONObject();
		
		zipInfo.put("entries", entries);
		zipInfo.put("name", source.substring(source.lastIndexOf('/'), source.length()));
		zipInfo.put("fullPath", source);
		
		System.out.println("entries:" +  entries);
		System.out.println("name:" +  source.substring(source.lastIndexOf('/'), source.length()));
		System.out.println("fullPath:" +  source);
		
		return zipInfo;
	}
	
	/**
	* Read the compressed file and returns InputStream obtained.
	*
	* @param zipFile		Zip/Rar filename to read.
	* @return				InputStream containing zip file data.
	 * @throws IOException 
	*/
    public InputStream readRarFile(String zipFile) throws IOException 
    {
    	InputStream is = null;
    	Connection c = null;
    	FileConnection fc = null;
    	
        try 
        {
        	c = Connector.open(zipFile, Connector.READ);
        	fc = (FileConnection) c;
            is = fc.openInputStream();
        } 
        catch (Exception e) 
        {
            System.out.println(LOG_TAG + " - Error reading the File");
        }
        finally
        {
        	c.close();
        	fc.close();
        }
        
        return is;
    }
    
	/**
	* Write file or dir in target path.
	*
	* @param zis		Zip/Rar filename to read.
	* @param path		Path of the current item to write.
	* @param b			Array of bytes to write.
	* @param target		Parent path to write the current item.
	* @return			True or false if file/dir could be written.
	*/
    public boolean writeFile(ZipInputStream zis, String path, byte[] b, String target) 
    {
        String targetToWrite = target + path;
        FileConnection conn = null;
        
        System.out.println(LOG_TAG + " - Processing file: " + targetToWrite);
        
        // Check if a filename we want to process is a DIRECTORY or a FILE
        if (path.charAt(path.length() - 1) == '/')
        {
        	// Process DIRECTORY
            System.out.println(LOG_TAG + " - dirname: " + targetToWrite);
            String dirsMade = "";

            if (!dirsMade.equals(targetToWrite)) 
            {
            	try
            	{
                    conn = (FileConnection) Connector.open(targetToWrite);
                    
                    // If it already exists as a dir, don't do anything
                    if (!(conn.exists() && conn.isDirectory())) 
                    {
                    	System.out.println(LOG_TAG + " - Creating dir: " + targetToWrite);
                    	conn.mkdir();
                    	System.out.println(LOG_TAG + " - Created directory: " + targetToWrite);
                        
                    	dirsMade = targetToWrite;
                    }
            	}
        		catch (IOException e) 
        		{
                    // error
                	System.out.println(LOG_TAG + " - Exception creating dir: " + e.getMessage());
                } 
        		catch (SecurityException e) 
        		{
                    // no permission to create/write
                	System.out.println(LOG_TAG + " - Exception c/w creating dir: " + e.getMessage());
                }
                catch (Exception e) 
                {
                    System.out.println(LOG_TAG + " - Generic exception: " + e.toString());
                    return false;
                }
            	finally
            	{
            		try
            		{
            			System.out.println(LOG_TAG + " - Closing connection");
            			conn.close();
            		}
            		catch (IOException e) 
            		{
            			System.out.println(LOG_TAG + " - Exception closing connection: " + e.toString());
            			return false;
            	    } 
            	}
            }
        }
        else
        {
        	// Process FILES
        	try
        	{
            	conn = (FileConnection) Connector.open(targetToWrite);

        		if (!conn.exists() && !conn.isDirectory()) 
        		{
                    conn.create();
                } 
        		else 
        		{
                    conn.truncate(0);
                }

        		// Create file knowing size of array of bytes
                OutputStream os = conn.openOutputStream();
                
                int count = 0;
        		while ((count = zis.read(b, 0, b.length)) != -1) 
        		{
                    os.write(b, 0, count);
                }
        		
                os.flush();
                os.close();
        	}
    		catch (IOException e) 
    		{
                // error
            	System.out.println(LOG_TAG + " - Exception creating file: " + e.getMessage());
            } 
    		catch (SecurityException e) 
    		{
                // no permission to create/write
            	System.out.println(LOG_TAG + " - Exception c/w creating file: " + e.getMessage());
            }
            catch (Exception e) 
            {
                System.out.println(LOG_TAG + " - Generic exception: " + e.toString());
                return false;
            }
        	finally
        	{
        		try
        		{
        			System.out.println(LOG_TAG + " - Closing connection");
        			conn.close();
        		}
        		catch (IOException e) 
        		{
        			System.out.println(LOG_TAG + " - Exception closing connection: " + e.toString());
        			return false;
        	    } 
        	}
        }

        return true;
    }

	/**
	* Identifies if action to be executed returns a value and should be run synchronously.
	*
	* @param action		The action to execute
	* @return		T=returns value
	*/
	public boolean isSynch(String action) {
		if (action.equals("info")) {
			return false;
		}
		else if (action.equals("compress")) {
			return true;
		}
		else if (action.equals("uncompress")) {
			return false;
		}

		return false;
	}

	/**
	* Compress.
	*
	* @param source		The action to execute.
	* @param elements	An array of element to compress.
	* @return		True if all went fine, false otherwise.
	*/
	private boolean compress(String source, String callbackId) {

		// TODO: Implement it.
		return false;
	}

	private int getEntries(String source) throws IOException
	{
		InputStream is = null;
		int entries = 0;
		try 
        {
			is = readRarFile(source);
			ZipArchive za = new ZipArchive(is);
			entries = za.size();
			System.out.println("Zip file size: " + entries);
			
        }
		catch (Exception e) 
        {
			System.out.println(LOG_TAG + " Error getting number of entries: " + e.getMessage());
        }
		finally
		{
			is.close();
		}
		
		return entries;
	}
	
	/**
	* Uncompress.
	*
	* @param source		Sourcezip file location. (Local or remote)
	* @param destination	Directory destination.
	* @return		True.
	 * @throws IOException 
	*/
	private JSONObject uncompress(String source, String target, String callbackId) throws JSONException, InterruptedException, IOException
	{
		// 'target' would be 'file:///SDCard/test'
        int total_entries = 0;
        int current_entries = 0;
        JSONObject lastMsg = null;

        InputStream is = null;
        ZipInputStream zis = null;
        ZipEntry ze = null;

        try 
        {
        	// get Zip size (in entries)
        	total_entries = getEntries(source);
        	
    		//Rar or Zip File  Extraction
            is = readRarFile(source);
            zis = new ZipInputStream(is);
            
            while ((ze = zis.getNextEntry()) != null) 
            {
                int i = (int) ze.getSize();
                byte[] b = new byte[i];
                
                // Write File or Dir, calling our function
                writeFile(zis, ze.getName(), b, target);
                zis.closeEntry();
                current_entries++;
                
                lastMsg = this.publish(ze.getName(), current_entries, total_entries, callbackId);
            }
        } 
        catch (Exception e) 
        {
          System.out.println(LOG_TAG + " Error uncompressing: " + e.getMessage());
        }
        finally
        {
            zis.close();
        	is.close();
        }
		
		return lastMsg;
	}

	private JSONObject publish(String file, int currentEntities, int totalEntities, String callbackId) throws JSONException, InterruptedException
	{
		JSONObject msg = new JSONObject();
		
		float progress = (float) currentEntities / totalEntities;
		progress = (int) (progress * 100);
		msg.put("progress", progress);
				
		System.out.println("Progreso: " + String.valueOf(progress));
		
		boolean completed = totalEntities == currentEntities;
		
		if (totalEntities == currentEntities)
		{
			msg.put("completed", true);
		}
		else
		{
			msg.put("completed", false);
		}
		
        if (file.charAt(file.length() - 1) == '/')
        {
        	msg.put("isFile", false);
        	msg.put("isDirectory", true);
        }
        else
        {
        	msg.put("isFile", true);
        	msg.put("isDirectory", false);
        }

        msg.put("name", file.substring(file.lastIndexOf('/'), file.length()));
        msg.put("fullPath", file);

		PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
		result.setKeepCallback(true);

        // Avoid to send the message "uncompress completed" twice.
        // This message is sended in the execute method.
        if (!completed) {
		    success(result, callbackId);
        }

		Thread.sleep(100);
		
		return msg;
	}
}