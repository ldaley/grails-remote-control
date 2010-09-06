package grails.plugin.remotecontrol.client

import grails.plugin.remotecontrol.RemoteClosureDefinition
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

class RemoteClosureDefinitionGenerator {

	final Class closureClass
	final ClassLoader classLoader
	
	RemoteClosureDefinitionGenerator(Closure closure) {
		this(closure.class)
	}

	RemoteClosureDefinitionGenerator(Class closureClass) {
		this(closureClass.classLoader, closureClass)
	}
	
	RemoteClosureDefinitionGenerator(ClassLoader classLoader, Closure closure) {
		this(classLoader, closure.class)
	}

	RemoteClosureDefinitionGenerator(ClassLoader classLoader, Class closureClass) {
		this.classLoader = classLoader
		this.closureClass = closureClass
	}
	
	RemoteClosureDefinition generate() {
		def classFile = getClassFile()
		
		def definition = new RemoteClosureDefinition()
		definition.root = toByteArray(classFile)
		definition.supports = findSupportingClassFiles().collect { toByteArray(it) }
		
		definition
	}
	
	/**
	 * Groovy 1.7 add's File.getBytes() but it's not there in 1.6
	 */
	protected toByteArray(File classFile) {
		def baos = new ByteArrayOutputStream()
		baos << classFile.newInputStream()
		baos.toByteArray()
	}
	
	protected List<File> findSupportingClassFiles() {
		def finder = new PathMatchingResourcePatternResolver(classLoader)
		def base = getClassFileResourceBase()
		def fixedBase = base.replace('$_$', '$_')
		
		def classFileResources = finder.getResources("classpath*:${fixedBase}?*.class")
		classFileResources*.file
	}
	
	protected File getClassFile() {
		def classFileResource = classLoader.findResource(getClassFileResourceName())
		new File(classFileResource.file)
	}
	
	protected String getClassFileResourceBase() {
		closureClass.name.replace('.', '/')
	}
	
	protected String getClassFileResourceName() {
		getClassFileResourceBase() + ".class"
	}
	
}