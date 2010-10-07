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
package grails.plugin.remotecontrol

import grails.plugin.remotecontrol.client.*
import grails.util.BuildSettingsHolder

class RemoteControl {

	static public final RECEIVER_PATH = "grails-remote-control"
	 
	static defaultReceiverAddress
	
	final classLoader
	final receiverAddress
	private final commandGenerator
	
	/**
	 * Constructs an instance that is suitable for use in functional testing scenarios.
	 */
	RemoteControl() {
		this(getFunctionalTestReceiverAddress(), Thread.currentThread().contextClassLoader)
	}
	
	RemoteControl(String receiverAddress, ClassLoader classLoader) {
		this.receiverAddress = receiverAddress
		this.classLoader = classLoader
		this.commandGenerator = new CommandGenerator(classLoader)
	}
	
	def exec(Closure command) {
		def result = sendCommand(generateCommand(command))
		
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
	
	def call(Closure command) {
		exec(command)
	}
	
	/**
	 * Convenience method
	 */
	static execute(Closure command) {
		new RemoteControl().exec(command)
	}
	
	protected Command generateCommand(Closure command) {
		commandGenerator.generate(command)
	}
	
	protected Result sendCommand(Command command) {
		new Sender(receiverAddress, classLoader).send(command)
	}
	
	private static getFunctionalTestReceiverAddress() {
		def base = getFunctionalTestBaseUrl()
		if (!base) {
			throw new IllegalStateException("Cannot get receiver address for functional testing as functional test base URL is not set. Are you calling this from a functional test?")
		}
		
		base.endsWith("/") ? base + RECEIVER_PATH : base + "/" + RECEIVER_PATH
	}
	
	private static getFunctionalTestBaseUrl() {
		BuildSettingsHolder.settings?.functionalTestBaseUrl
	}

}
