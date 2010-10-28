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

class RemoteControl {
	
	final Transport transport
	final ClassLoader classLoader
	
	protected final commandGenerator
	
	RemoteControl(Transport transport, ClassLoader classLoader) {
		this.transport = transport
		this.classLoader = classLoader
		this.commandGenerator = new CommandGenerator(classLoader)
	}
	
	def exec(Closure[] commands) {
		def result = sendCommandChain(generateCommandChain(commands))
		
		if (result.wasNull) {
			null
		} else if (result.wasUnserializable) {
			throw new UnserializableReturnException(result)
		} else if (result.wasThrown) {
			throw new RemoteException(result.value)
		} else {
			result.value
		}
	}
		
	def call(Closure[] commands) {
		exec(commands)
	}
	
	/**
	 * Convenience method
	 */
	static execute(Closure[] commands) {
		new RemoteControl().exec(commands)
	}
	
	protected CommandChain generateCommandChain(Closure[] commands) {
		new CommandChain(commands: commands.collect { commandGenerator.generate(it) })
	}
	
	protected Result sendCommandChain(CommandChain commandChain) {
		transport.send(commandChain)
	}
	
}
