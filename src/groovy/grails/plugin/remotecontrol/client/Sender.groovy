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
package grails.plugin.remotecontrol.client

import grails.plugin.remotecontrol.*
import grails.plugin.remotecontrol.util.*

/**
 * Responsible for sending a given command to a receiver over HTTP and 
 * assembling up the returned Result.
 */
class Sender {

	final receiverAddress
	final classLoader
	
	/**
	 * @param receiverAddress the full address to the remote receiver
	 * @param classLoader the class loader to use when unserialising the result
	 */
	Sender(String receiverAddress, ClassLoader classLoader) {
		this.receiverAddress = receiverAddress
		this.classLoader = classLoader
	}

	/**
	 * Serialises the Command and sends it over HTTP, returning the Result.
	 * 
	 * @throws UnableToCommunicateWithReceiverException if there is any issue with the receiver.
	 */
	Result send(CommandChain commandChain) throws UnableToCommunicateWithReceiverException {
		openConnection().with {
			setRequestProperty("Content-Type", ContentType.COMMAND.value)
			setRequestProperty("accept", ContentType.RESULT.value)
			instanceFollowRedirects = true
			doOutput = true
			
			writeCommandChain(commandChain, outputStream)

			try {
				readResult(inputStream)
			} catch (IOException e) {
				def status = responseCode
				if (status == -1) {
					throw e
				} else {
					throw new UnableToCommunicateWithReceiverException("A non OK response was returned ($status): ${errorStream.text}", e)
				}
			}
		}
	}
	
	def openConnection() {
		new URL(receiverAddress).openConnection()
	}
	
	protected writeCommandChain(CommandChain commandChain, OutputStream output) {
		def oos = new ObjectOutputStream(output)
		oos << commandChain
		oos.flush()
		oos.close()
	}
	
	protected Result readResult(InputStream input) {
		new ClassLoaderConfigurableObjectInputStream(classLoader, input).readObject()
	}

	static class UnableToCommunicateWithReceiverException extends IOException {
		UnableToCommunicateWithReceiverException(message, Throwable cause) {
			super(message as String, cause)
		}
	}
}