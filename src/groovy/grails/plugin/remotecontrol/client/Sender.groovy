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

class Sender {

	final receiverAddress
	final classLoader
	
	Sender(String receiverAddress, ClassLoader classLoader) {
		this.receiverAddress = receiverAddress
		this.classLoader = classLoader
	}

	Result send(Command command) {
		openConnection().with {
			setRequestProperty("Content-Type", ContentType.COMMAND.value)
			setRequestProperty("accept", ContentType.RESULT.value)
			doOutput = true
			
			writeCommand(command, outputStream)
			def result = readResult(inputStream)
			
			disconnect()
			
			result
		}
	}
	
	def openConnection() {
		new URL(receiverAddress).openConnection()
	}
	
	protected writeCommand(Command command, OutputStream output) {
		def oos = new ObjectOutputStream(output)
		oos << command
		oos.flush()
		oos.close()
	}
	
	protected Result readResult(InputStream input) {
		new ClassLoaderConfigurableObjectInputStream(classLoader, input).readObject()
	}

}