package grails.plugin.remotecontrol.client

import grails.plugin.remotecontrol.Command
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

class CommandGenerator {

	final ClassLoader classLoader

	CommandGenerator() {
		this(null)
	}
	
	CommandGenerator(ClassLoader classLoader) {
		this.classLoader = classLoader ?: getClass().classLoader
	}
	
	Command generate(Closure closure) {
		def closureClass = closure.class
		def classFile = getClassFile(closureClass)
		
		new Command(
			root: toByteArray(classFile),
			supports: findSupportingClassFiles(closureClass).collect { toByteArray(it) }
		)
	}
	
	protected toByteArray(File classFile) {
		def baos = new ByteArrayOutputStream()
		baos << classFile.newInputStream()
		baos.toByteArray()
	}
	
	protected List<File> findSupportingClassFiles(Class closureClass) {
		def finder = new PathMatchingResourcePatternResolver(classLoader)
		def base = getClassFileResourceBase(closureClass)
		def fixedBase = base.replace('$_$', '$_')
		
		def classFileResources = finder.getResources("classpath*:${fixedBase}?*.class")
		classFileResources*.file
	}
	
	protected File getClassFile(Class closureClass) {
		def classFileResource = classLoader.findResource(getClassFileResourceName(closureClass))
		new File(classFileResource.file)
	}
	
	protected String getClassFileResourceBase(Class closureClass) {
		closureClass.name.replace('.', '/')
	}
	
	protected String getClassFileResourceName(Class closureClass) {
		getClassFileResourceBase(closureClass) + ".class"
	}
	
}