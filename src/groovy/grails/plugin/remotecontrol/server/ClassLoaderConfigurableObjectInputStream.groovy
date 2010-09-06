package grails.plugin.remotecontrol.server

/**
 * Allows us to hydrate objects with a custom classloader.
 */
class ClassLoaderConfigurableObjectInputStream extends ObjectInputStream {
	
	final ClassLoader classLoader
	
	ClassLoaderConfigurableObjectInputStream(ClassLoader classLoader, InputStream input) {
		super(input)
		this.classLoader = classLoader
	}

	Class<?> resolveClass(ObjectStreamClass desc) {
		try {
			classLoader.loadClass(desc.name)
		} catch (ClassNotFoundException e) {
			super.resolveClass(desc)
		}
	}

}