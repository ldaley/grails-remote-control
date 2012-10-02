/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import grails.util.Environment

class RemoteControlGrailsPlugin {

	def version = "1.3"
	def grailsVersion = "1.3.5 > *"
	def dependsOn = [:]
	def pluginExcludes = ["grails-app/**/*", "scripts/**/*"]
	
	def author = "Luke Daley"
	def authorEmail = "ld@ldaley.com"
	def title = "Remote Control"
	def description = "Remotely control a Grails application (for functional testing)"
	def documentation = "http://grails.org/plugin/remote-control"

	def doWithWebDescriptor = { webXml ->
		if (isEnabled(application)) {
			log.info("remote control servlet is enabled")
			def remoteControlClass = getClass().classLoader.loadClass('grails.plugin.remotecontrol.RemoteControl')

			def servlets = webXml.servlet 
			def lastServlet = servlets[servlets.size() - 1] 
			lastServlet + { 
				servlet { 
					'servlet-name'('grails-remote-control') 
					'servlet-class'('grails.plugin.remotecontrol.RemoteControlServlet') 
					'load-on-startup'(1) 
				} 
			} 
			def mappings = webXml.'servlet-mapping' 
			def lastMapping = mappings[mappings.size() - 1] 
			lastMapping + { 
				'servlet-mapping' { 
					'servlet-name'('grails-remote-control') 
					'url-pattern'("/${remoteControlClass.RECEIVER_PATH}") 
				} 
			} 
		} else {
			log.debug("remote control servlet is disabled")
		}
	}
	
	boolean isEnabled(application) {
		def configValue = application.config.remoteControl.enabled
		if (configValue instanceof ConfigObject) {
			getDefaultIsEnabledForEnvironment()
		} else {
			configValue
		}
	}
	
	boolean getDefaultIsEnabledForEnvironment() {
		if (Environment.current == Environment.TEST) {
			true
		} else {
			false
		}
	}
}