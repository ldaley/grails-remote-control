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
package grails.plugin.remotecontrol.http

import grails.plugin.remotecontrol.*
import grails.plugin.remotecontrol.util.*
import grails.plugin.remotecontrol.client.AbstractTransport

/**
 * Transports commands over http to the given receiver address.
 */
class HttpTransport extends AbstractTransport {

	final receiverAddress
	
	/**
	 * @param receiverAddress the full address to the remote receiver
	 * @param classLoader the class loader to use when unserialising the result
	 */
	HttpTransport(String receiverAddress, ClassLoader classLoader) {
		super(classLoader)
		this.receiverAddress = receiverAddress
	}

	/**
	 * Serialises the Command and sends it over HTTP, returning the Result.
	 * 
	 * @throws IOException if there is any issue with the receiver.
	 */
	Result send(CommandChain commandChain) throws IOException {
		openConnection().with {
			setRequestProperty("Content-Type", ContentType.COMMAND.value)
			setRequestProperty("accept", ContentType.RESULT.value)
			instanceFollowRedirects = true
			doOutput = true
			
			writeCommandChain(commandChain, outputStream)
			readResult(inputStream)
		}
	}
	
	def openConnection() {
		new URL(receiverAddress).openConnection()
	}

}