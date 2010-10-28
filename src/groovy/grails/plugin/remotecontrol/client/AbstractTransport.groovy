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

/**
 * Provides serialisation of commands and results.
 */
abstract class AbstractTransport implements Transport {

	final classLoader
	
	/**
	 * @param classLoader the class loader to use when unserialising the result
	 */
	AbstractTransport(ClassLoader classLoader) {
		this.classLoader = classLoader
	}

	protected void writeCommandChain(CommandChain commandChain, OutputStream output) {
		def oos = new ObjectOutputStream(output)
		oos << commandChain
		oos.flush()
		oos.close()
	}
	
	protected Result readResult(InputStream input) {
		new ClassLoaderConfigurableObjectInputStream(classLoader, input).readObject()
	}

}