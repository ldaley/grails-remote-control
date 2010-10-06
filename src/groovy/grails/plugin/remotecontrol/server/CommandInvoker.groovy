package grails.plugin.remotecontrol.server

import grails.plugin.remotecontrol.*

class CommandInvoker {
	
	final ClassLoader classLoader
	final Command command
	final Class rootClass
	final List supportClasses
	
	private CommandInvoker(ClassLoader parentLoader, Command command) {
		this.classLoader = new GroovyClassLoader(parentLoader)
		
		this.rootClass = defineClass(command.root)
		this.supportClasses = Collections.unmodifiableList(command.supports.collect { defineClass(it) })
	}
	
	Result invokeAgainst(delegate) {
		def instance = instantiate()
		instance.resolveStrategy = Closure.DELEGATE_ONLY
		instance.delegate = delegate
		
		try {
			Result.forValue(instance.call())
		} catch (thrown) {
			Result.forThrown(thrown)
		}
	}
	
	def instantiate() {
		rootClass.newInstance(null, null)
	}
	
	protected Class defineClass(byte[] bytes) {
		classLoader.defineClass(null, bytes, 0, bytes.length)
	}
}