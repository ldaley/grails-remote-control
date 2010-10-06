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