package grails.plugin.remotecontrol.server

import grails.plugin.remotecontrol.*
import grails.plugin.remotecontrol.util.*

class Receiver {

	final grailsApplication
	
	Receiver(grailsApplication) {
		this.grailsApplication = grailsApplication
	}
	
	void execute(InputStream input, OutputStream output) {
		def command = readCommand(input)
		def result = invokeCommand(command)
		writeResult(result, output)
	}
	
	protected Command readCommand(InputStream input) {
		def baos = new ByteArrayOutputStream()
		baos << input
		
		println "baos.size: ${baos.size()}"
		
		new ClassLoaderConfigurableObjectInputStream(grailsApplication.classLoader, new ByteArrayInputStream(baos.toByteArray())).readObject()
	}
	
	protected Result invokeCommand(Command command) {
		new CommandInvoker(grailsApplication.classLoader, command).invokeAgainst(grailsApplication.mainContext)
	}
	
	protected writeResult(Result result, OutputStream output) {
		def oos = new ObjectOutputStream(output)
		oos << result
		oos.close()
	}
}

