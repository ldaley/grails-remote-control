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

/**
 * This test case shows how to use the remote control and some of it's limitations
 * with regard to serialisation and scope.
 * 
 * The remote control object has an exec(Closure) method, and an alias for that as call(Closure).
 * The call(Closure) variant allows the use of the Groovy language feature where you can essentially
 * treat an object like a method, which is how “remote { … }” works below (i.e. it's really “remote.call { … }).
 * This doesn't always work though as you will see (due to Groovy), so sometimes you need to use .exec().
 * 
 * Where we are passing a closure to the remote control object, that closure gets executed INSIDE the
 * application we are functionally testing, which may be in a different JVM on a completely different machine.
 * This works by sending the closure over HTTP to the application (which must have the remote-control plugin installed).
 * 
 * An example use for this would be creating/deleting domain data inside your remote application for testing purposes.
 */
class SmokeTests extends GroovyTestCase {

	def remote = new RemoteControl()
	
	// Used in a later test
	def anIvar = 2
	
	/**
	 * The result of the command run on the server is sent back and is returned
	 */
	void testReturingValues() {
		assert remote { 1 + 1 } == 2
	}
	
	/**
	 * The command is resolved against the application context,
	 * so we can access any beans defined there
	 */
	void testAccessTheAppContext() {
		def name = remote {
			grailsApplication.metadata['app.name']
		}
		
		assert name == "grails-remote-control"
	}

	/**
	 * Commands can contain other closures
	 */
	void testWithInnerClosures() {
		assert [2,3,4] == remote {
			[1,2,3].collect { it + 1 }
		}
	}

	/**
	 * If the command throwns an exception, we throw a RemoteException
	 * client side with the actual exception instance that was thrown server
	 * side as the cause
	 */
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
	
	/**
	 * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
	 */
	void testUnserialisableReturn() {
		shouldFail(UnserializableReturnException) {
			remote.exec { System.out }
		}
	}
	
	/**
	 * If the command returns an exception but does not throw it, we just return the exception
	 */
	void testReturningException() {
		assert (remote { new Exception() }) instanceof Exception
	}
	
	/**
	 * We can access lexical scope (within limits)
	 */
	void testAccessingLexicalScope() {
		def a = 1
		assert remote { a + 1 } == 2
	}

	/**
	 * Anything in lexical scope we access must be serialisable
	 */
	void testAccessingNonSerializableLexicalScope() {
		def a = System.out
		shouldFail(NotSerializableException) {
			remote.exec { a }
		}
	}
	
	/**
	 * Non existant vars cause NPEs 
	 */
	void testAccessingNonExistantVar() {
		def thrown 
		try {
			remote { iDontExist * 2 }
		} catch (RemoteException e) {
			thrown = e.cause
			assert thrown instanceof NullPointerException
			assert thrown.message == "Cannot invoke method multiply() on null object"
		}
		
		assert thrown
	}
	
	/**
	 * Owner ivars can't be accessed because they aren't really lexical
	 * so get treated as bean names from the app context
	 */
	void testAccessingIvar() {
		def thrown 
		try {
			remote { anIvar * 2 }
		} catch (RemoteException e) {
			thrown = e.cause
			assert thrown instanceof NullPointerException
			assert thrown.message == "Cannot invoke method multiply() on null object"
		}
		
		assert thrown
	}
	
	/**
	 * We can pass curried commands
	 */
	void testCurryingCommands() {
		def command = { it + 2 }
		assert remote.exec(command.curry(2)) == 4
	}
	
	/**
	 * We can curry a command as many times as we need to
	 */
	void testCurryingCommandsMoreThanOnce() {
		def command = { a, b -> a + b }
		def curry1 = command.curry(1)
		def curry2 = curry1.curry(1)
		
		assert remote.exec(curry2) == 2
	}
	
	/**
	 * Like everything else, currying args must be serialisable
	 */
	void testCurryingArgsMustBeSerializable() {
		shouldFail(NotSerializableException) {
			remote.exec({ it }.curry(System.out))
		}
	}
	
	/**
	 * Any classes referenced have to be available in the remote app,
	 * and any classes defined in tests ARE NOT.
	 */
	void testCannotReferToClassesNotInTheApp() {
		def a = new SmokeTestsLocal()
		shouldFailWithCause(ClassNotFoundException) {
			remote.exec { a }
		}
	}
	
	/**
	 * Variation of above, but yields a different error.
	 */
	void testCannotInstantiateClassesNotInTheApp() {
		shouldFailWithCause(NoClassDefFoundError) {
			remote.exec { new SmokeTestsLocal() }
		}
	}
}


class SmokeTestsLocal implements Serializable {}
