/*
 * Copyright (C) 2012 by Emergya
 *
 * Author: Alejandro Leiva <aleiva@emergya.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/

package org.apache.cordova.plugin;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.FileUtils;

import org.apache.cordova.FileTransfer;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.util.Log;

public class Zip extends Plugin {

	private static final String LOG_TAG = "Zip";
	private static final String DIRECTORY_SEPARATOR = "/";
	private static final int BUFFER_SIZE = 2048;

	private List<String> processedEntities = new ArrayList<String>();

	/**
	* Executes the request and returns PluginResult.
	*
	* @param action		The action to execute.
	* @param args		JSONArry of arguments for the plugin.
	* @param callbackId	The callback id used when calling back into JavaScript.
	* @return		A PluginResult object with a status and message.
	*/
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";

		try {
			// Parse common args
			String source = args.getString(0);
			String target = args.getString(1);

			if (action.equals("info")) {

				JSONObject info = this.info(source);

				return new PluginResult(status, info);

			} else if (action.equals("compress")) {

				return new PluginResult(status, result);
				/*
				if (this.uncompress(source, target, callbackId)) {
					return new PluginResult(status, result);
				} else {
					return new PluginResult(PluginResult.Status.ERROR, result);
				}
				*/
			} else if (action.equals("uncompress")) {
				
				JSONObject ret = this.uncompress(source, target, callbackId);
				ret.put("completed", true);

				// Purge action only data structures.
				this.processedEntities.clear();				

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
			return new PluginResult(PluginResult.Status.IO_EXCEPTION, e.getMessage());
		} catch (InterruptedException e) {
			return new PluginResult(PluginResult.Status.IO_EXCEPTION, e.getMessage());
		}
	}

	/**
	* Identifies if action to be executed returns a value and should be run synchronously.
	*
	* @param action		The action to execute
	* @return		T=returns value
	*/
	public boolean isSynch(String action) {
		if (action.equals("compress")) {
			return true;
		}
		else if (action.equals("uncompress")) {
			return false;
		}

		return false;
	}

	/**
	* Info.
	*
	* @param source		The action to execute.
	* @return			True if all went fine, false otherwise.
	*/
	private JSONObject info(String source) {

		JSONObject info = new JSONObject();

		return info;
	}

	/**
	* Compress.
	*
	* @param source		The action to execute.
	* @param elements	An array of element to compress.
	* @return		True if all went fine, false otherwise.
	*/
	private boolean compress(String source, List<String> entities, String callbackId) {

		// TODO: Implement it.
		return false;
	}

	/**
	* Uncompress.
	*
	* @param source		Sourcezip file location. (Local or remote)
	* @param destination	Directory destination.
	* @return		True.
	*/
	private JSONObject uncompress(String source, String target, String callbackId) throws IOException, JSONException, InterruptedException
	{
		Log.d(LOG_TAG, "uncompress: " + source + " to " + target); 

		List<String> extractedEntities = new ArrayList<String>();

		source = FileUtils.stripFileProtocol(source);
		target = FileUtils.stripFileProtocol(target);
		Log.d(LOG_TAG, "stripped source: " + source);
		Log.d(LOG_TAG, "stripped target: " + target);

		File sourceFile = new File(source);
		File targetFile = new File(target);


		ZipFile zipFile = new ZipFile(sourceFile);
		Enumeration zipEntities = zipFile.entries();
		
		String targetPath = targetFile.getAbsolutePath() + DIRECTORY_SEPARATOR + zipFile.getName().substring(0, zipFile.getName().length() - 4);

		JSONObject lastMsg = new JSONObject();

		// TODO: Handle possible cancelation.
		while (zipEntities.hasMoreElements()) {
			ZipEntry entity = (ZipEntry) zipEntities.nextElement();
			Log.d(LOG_TAG, "Current entity: " + entity.getName());

			File currentTarget = new File(targetPath, entity.getName());
			File currentTargetParent = currentTarget.getParentFile();
			lastMsg = this.publish(currentTargetParent, callbackId);
			currentTargetParent.mkdirs();

			this.processedEntities.add(currentTargetParent.getAbsolutePath());

			if (!entity.isDirectory()) {

				lastMsg = this.publish(currentTarget, callbackId);
				BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entity));
				FileOutputStream fos = new FileOutputStream(currentTarget);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

				int currentByte;
				byte data[] = new byte[BUFFER_SIZE];
				while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();

				this.processedEntities.add(currentTarget.getAbsolutePath());
			}
			
		}

		return lastMsg;
	}

	private JSONObject publish(File file, String callbackId) throws JSONException, InterruptedException
	{
		JSONObject msg = new JSONObject();

		// Using FileUtils::getEntry to create an file info structure.
		FileUtils fu = new FileUtils();
		msg = fu.getEntry(file);
		
		// Add new params for progress calculation.
		msg.put("completed", false);
		msg.put("progress", this.processedEntities.size());

		PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
		result.setKeepCallback(true);
		success(result, callbackId);

		Thread.sleep(100);

		return msg;
	}
}
