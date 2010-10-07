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
import grails.plugin.remotecontrol.RemoteControl
import grails.plugin.remotecontrol.client.*

class SmokeTests extends GroovyTestCase {

	def remote = new RemoteControl()
	
	void testAccessTheAppContext() {
		def name = remote {
			grailsApplication.metadata['app.name']
		}
		
		assert name == "grails-remote-control"
	}

	void testWithInnerClosures() {
		assert [2,3,4] == remote {
			[1,2,3].collect { it + 1 }
		}
	}

	void testThrowingException() {
		def thrown = null
		try {
			remote { throw new Exception("bang!") }
		} catch (RemoteException e) {
			thrown = e.cause
			assert thrown.class == Exception
			assert thrown.message == "bang!"
		}
		
		assert thrown
	}
	
	void testUnserialisableReturn() {
		shouldFail(UnserializableReturnException) {
			remote.exec { System.out }
		}
	}
	
	void testReturningException() {
		assert (remote { new Exception() }) instanceof Exception
	}
	
}
