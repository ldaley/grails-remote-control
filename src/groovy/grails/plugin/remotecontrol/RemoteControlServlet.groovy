/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.remotecontrol

import javax.servlet.*
import javax.servlet.http.*
import groovyx.remote.server.Receiver
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.util.Environment

class RemoteControlServlet extends groovyx.remote.transport.http.RemoteControlServlet {
	
	void doExecute(InputStream input, OutputStream output) {
		def persistenceInterceptor = grailsApplication?.mainContext?.persistenceInterceptor
		persistenceInterceptor?.init()
		try {
			super.doExecute(input, output)
		} finally {
			persistenceInterceptor?.flush()
			persistenceInterceptor?.destroy()
		}
	}
	
	protected boolean validateRequest(HttpServletRequest request, HttpServletResponse response) {
		if (!isEnabled()) {
			response.sendError(404, "Remote control disabled")
			return false
		}

		return super.validateRequest(request, response)
	}
	
	def getGrailsApplication() {
		ApplicationHolder.application
	}
	
	protected Receiver createReceiver() {
		new Receiver(grailsApplication.classLoader, [app: grailsApplication, ctx: grailsApplication.mainContext])
	}
	
	boolean isEnabled() {
		def configValue = grailsApplication.config.remoteControl.enabled
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