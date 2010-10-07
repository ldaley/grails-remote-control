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