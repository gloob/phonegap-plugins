/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

function fail(error) {
	console.log(error.code);
}

var deviceInfo = function() {
    document.getElementById("platform").innerHTML = device.platform;
    document.getElementById("version").innerHTML = device.version;
    document.getElementById("uuid").innerHTML = device.uuid;
    document.getElementById("name").innerHTML = device.name;
    document.getElementById("width").innerHTML = screen.width;
    document.getElementById("height").innerHTML = screen.height;
    document.getElementById("colorDepth").innerHTML = screen.colorDepth;
};

function successListener (msg) {
	console.log("isFile: " + msg.isFile);
	console.log("isDirectory: " + msg.isDirectory);
	console.log("name: " + msg.name);
	console.log("fullPath: " + msg.fullPath);
	console.log("completed: " + msg.completed);
	console.log("progress: " + msg.progress);
    console.log("entries: " + msg.entries);
}

function info() {
    var targetName = "/test.zip";
    
    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fileSystem) {
        fileSystem.root.getFile(targetName, null, function (fileEntry) {
            window.plugins.Zip.info(fileEntry.fullPath, successListener);
        }, fail);
    }, fail);
}

function uncompressFromSDCard() {
    var targetName = "/test.zip";
    
    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fileSystem) {
        fileSystem.root.getFile(targetName, null, function (fileEntry) {
            window.plugins.Zip.uncompress(fileEntry.fullPath, "", successListener);
        }, fail);
    }, fail);
}

function uncompressFromURL() {

	var url = "http://10.42.0.1/test.zip";
	var targetName = "/test.zip";
    
    
    var uncompressFileEntry = function(fileEntry) {
        
        console.log(fileEntry);
        
        var localPath = fileEntry.fullPath;
        var zip = window.plugins.Zip;
        zip.uncompress(localPath, "/tmp", successListener, function () {
                       console.error('ERROR zip.uncompress()');
                       console.error(arguments);
                       });
    };

	window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fileSystem) {

		fileSystem.root.getFile(targetName, {create: true, exclusive: false}, function (fileEntry) {

			var localPath = fileEntry.fullPath;
			
			/*
			if (device.platform === "Android" && localPath.indexof("file://") === 0) {
				localPath = localPath.substring(7);
			}
			*/
	
			var fileTransfer = new FileTransfer();
	
			fileTransfer.download(
				url,
				localPath,
				function (entry) {
					console.log("download complete: " + entry.fullPath);
					console.log("+ info: " + entry);
                    uncompressFileEntry(fileEntry);
				},
				function (error) {
					console.log("download error source " + error.source);
					console.log("download error target " + error.target);
					console.log("upload error code" + error.code);
				}
			);
		}, fail);
	}, fail);	
}

function compress() {

}

function init() {
    // the next line makes it impossible to see Contacts on the HTC Evo since it
    // doesn't have a scroll button
    // document.addEventListener("touchmove", preventBehavior, false);
    document.addEventListener("deviceready", deviceInfo, true);
}

