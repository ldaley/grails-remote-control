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
package grails.plugin.remotecontrol.server

import grails.plugin.remotecontrol.*
import grails.plugin.remotecontrol.util.*

class CommandInvoker {
	
	final ClassLoader parentLoader
	final Command command
	final Class rootClass
	final List supportClasses
	
	private CommandInvoker(ClassLoader parentLoader, Command command) {
		this.parentLoader = parentLoader
		this.command = command
	}
	
	Result invokeAgainst(delegate, arg = null) {
		try {
			def instance = instantiate()
			instance.resolveStrategy = Closure.DELEGATE_ONLY
			instance.delegate = delegate
			Result.forValue(invoke(instance, arg))
		} catch (Throwable thrown) {
			Result.forThrown(thrown)
		}
	}
	
	def instantiate() {
		def classLoader = new GroovyClassLoader(parentLoader)
		defineClass(classLoader, command.root)
		command.supports.collect { defineClass(classLoader, it) }
		def input = new ByteArrayInputStream(command.instance)
		def ois = new ClassLoaderConfigurableObjectInputStream(classLoader, input)
		ois.readObject()
	}
	
	protected invoke(instance, arg = null) {
		if (instance.maximumNumberOfParameters < 1) {
			instance()
		} else {
			instance(arg)
		}
	}
	
	protected Class defineClass(ClassLoader classLoader, byte[] bytes) {
		classLoader.defineClass(null, bytes, 0, bytes.length)
	}
}