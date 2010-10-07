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
class RemoteControlGrailsPlugin {

	def version = "1.0-alpha-1"
	def grailsVersion = "1.3.5 > *"
	def dependsOn = [:]
	def pluginExcludes = ["grails-app/views/error.gsp"]
	
	def author = "Luke Daley"
	def authorEmail = "ld@ldaley.com"
	def title = "Remote Control"
	def description = "Remotely control a Grails application (for functional testing)"
	def documentation = "http://grails.org/plugin/remote-control"

	def doWithWebDescriptor = { webXml ->
		def servlets = webXml.servlet 
		def lastServlet = servlets[servlets.size() - 1] 
		lastServlet + { 
			servlet { 
				'servlet-name'('grails-remote-control') 
				'servlet-class'('grails.plugin.remotecontrol.server.RemoteControlServlet') 
				'load-on-startup'(1) 
			} 
		} 
		def mappings = webXml.'servlet-mapping' 
		def lastMapping = mappings[mappings.size() - 1] 
		lastMapping + { 
			'servlet-mapping' { 
				'servlet-name'('grails-remote-control') 
				'url-pattern'("/grails-remote-control") 
			} 
		} 
	}
}