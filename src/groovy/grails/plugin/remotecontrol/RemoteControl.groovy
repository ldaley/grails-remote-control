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
package grails.plugin.remotecontrol

import grails.util.BuildSettingsHolder
import groovyx.remote.transport.http.HttpTransport

/**
 * Adds grails specific convenient no arg constructor
 */
class RemoteControl extends groovyx.remote.client.RemoteControl {

	static public final RECEIVER_PATH = "grails-remote-control"
	 
	static defaultReceiverAddress
	
	RemoteControl() {
		super(new HttpTransport(getFunctionalTestReceiverAddress(), Thread.currentThread().contextClassLoader), Thread.currentThread().contextClassLoader)
	}
	
	private static getFunctionalTestReceiverAddress() {
		def base = getFunctionalTestBaseUrl()
		if (!base) {
			throw new IllegalStateException("Cannot get receiver address for functional testing as functional test base URL is not set. Are you calling this from a functional test?")
		}
		
		base.endsWith("/") ? base + RECEIVER_PATH : base + "/" + RECEIVER_PATH
	}
	
	private static getFunctionalTestBaseUrl() {
		BuildSettingsHolder.settings?.functionalTestBaseUrl
	}

}
