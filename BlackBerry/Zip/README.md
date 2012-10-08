Cordova / Phonegap unzip plugin working on BlackBerry
=====================================================

This plugin for BlackBerry devices allows:

* Download a zip file from external URL
* Unzip a gzip file from SDCard
* Get info of the zip file

Usage:

<pre>cd &lt;dev_path&gt; && ant update -Dproject.path="&lt;dev_path&gt;\&lt;project_name&gt;" && cd &lt;project_name&gt; && copy &lt;dev_path&gt;\build\ext\cordova.1.7.0.jar &lt;dev_path&gt;\&lt;project_path&gt;\www\ext\ /Y && ant blackberry load-simulator</pre>

Example of usage:

<pre>cd \dev\cordova-phonegap-unzip-plugin-for-blackberry && ant update -Dproject.path="C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\example" && cd example && copy C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\build\ext\cordova.1.7.0.jar C:\dev\cordova-phonegap-unzip-plugin-for-blackberry\example\www\ext\ /Y && ant blackberry load-simulator</pre>

Full ant project at https://github.com/jialvarez/cordova-phonegap-unzip-plugin-for-blackberry 