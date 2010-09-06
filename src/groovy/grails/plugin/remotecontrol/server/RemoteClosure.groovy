package grails.plugin.remotecontrol.server

import grails.plugin.remotecontrol.RemoteClosureDefinition

class RemoteClosure {

	static RemoteClosure hydrate(ClassLoader parent, RemoteClosureDefinition container) {
		def loader = new GroovyClassLoader(parent)
		
		def rootClass = defineClass(loader, container.root)
		def supportClasses = container.supports.collect {
			defineClass(loader, it)
		}
		
		new RemoteClosure(loader, rootClass, supportClasses)
	}

	static private Class defineClass(ClassLoader loader, byte[] bytes) {
		loader.defineClass(null, bytes, 0, bytes.length)
	}
	
	final ClassLoader classLoader
	final Class rootClass
	final List supportClasses
	
	private RemoteClosure(ClassLoader loader, Class rootClass, List supportClasses) {
		this.classLoader = loader
		this.rootClass = rootClass
		this.supportClasses = Collections.unmodifiableList(supportClasses)
	}
	
	def invokeAgainst(delegate) {
		def instance = instantiate()
		instance.resolveStrategy = Closure.DELEGATE_ONLY
		instance.call()
	}
	
	def instantiate() {
		rootClass.newInstance(null, null)
	}
}