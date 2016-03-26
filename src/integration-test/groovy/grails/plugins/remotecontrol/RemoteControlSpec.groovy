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
package grails.plugins.remotecontrol

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import io.remotecontrol.UnserializableCommandException
import io.remotecontrol.client.RemoteException
import io.remotecontrol.client.UnserializableReturnException
import spock.lang.Specification

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

@Integration(applicationClass = Application)
class RemoteControlSpec extends Specification {

    RemoteControl remote
    def anIvar = 2

    void setup() {
        remote = new RemoteControl()
    }

    void "the result of the command run on the server is sent back and is returned"() {
        expect:
        remote.exec { 1 + 1 } == 2
    }

    void "the delegate of command contains the app context under 'ctx' so we can access any beans defined there"() {
        expect:
        remote.exec { ctx.grailsApplication.config.info.app.name } == 'remote-control'
    }

    void "a command can create and manipulate domain data in a hibernate session that is flushed at the end"() {
        when:
        def id = remote.exec {
            Person person = new Person(name: "Me")
            person.save()
            person.id
        }

        then:
        remote.exec { Person.countByName("Me") } == 1

        when:
        remote.exec { Person.get(id).delete() }

        then:
        remote.exec { Person.countByName("Me") } == 0
    }

    void "command can create other closures"() {
        expect:
        [2, 3, 4] == remote.exec { [1, 2, 3].collect { it + 1 } }
    }

    void "if the command throwns an exception, we throw a RemoteException client side with the actual exception instance that was thrown server side as the cause"() {
        when:
        remote.exec { throw new Exception("bang!") }

        then:
        RemoteException e = thrown()
        e.cause.class == Exception
        e.cause.message == 'bang!'
    }

    void "if the command returns something that is unserialisable, we thrown an UnserializableReturnException"() {
        when:
        remote.exec { System.out }

        then:
        thrown(UnserializableReturnException)
    }

    void "if the command returns an exception but does not throw it, we just return the exception"() {
        expect:
        remote.exec { new Exception() } instanceof Exception
    }

    void "we can access lexical scope (within limits)"() {
        when:
        Integer num = 1

        then:
        remote.exec { num + 1 } == 2
    }

    void "anything in lexical scope we access must be serializable"() {
        when:
        PrintStream printStream = System.out
        remote.exec { printStream }

        then:
        thrown(UnserializableCommandException)
    }

    void "owners ivars can't be accessed because they are not really lexical so get treated as bean names from app context"() {
        when:
        remote.exec { anIvar * 2 }

        then:
        RemoteException e = thrown()
        e.cause instanceof MissingPropertyException
    }

    void "we can pass curried commands"() {
        when:
        def command = { it + 2 }

        then:
        remote.exec(command.curry(2)) == 4
    }

    void "we can curry a command as many times as we need to"() {
        when:
        def command = { a, b -> a + b }
        def curry1 = command.curry(1)
        def curry2 = curry1.curry(2)

        then:
        remote.exec(curry2) == 3
    }

    void "like everything else, currying args must be serialized"() {
        when:
        remote.exec({ it }.curry(System.out))

        then:
        thrown(UnserializableCommandException)
    }

    void "multiple commands can be sent, the return value of the previous command is passed to the next command as it's single argument"() {
        expect:
        remote.exec({ 1 }, { it + 1 }, { it + 1 }) == 3
    }

    void "the delegate command is like a map and can store properties"() {
        expect:
        remote.exec({ num = 1 }, { num = num + 1 }, { num + 1 }) == 3
    }

    void "a command can set properties of a remote bean"() {
        when:
        remote.exec { ctx.theService.value = 1 }

        then:
        remote.exec { ctx.theService.value } == 1

        when:
        remote.exec { ctx.theService.setValue(2) }

        then:
        remote.exec { ctx.theService.value } == 2

        when:
        remote.exec { ctx.theService.setProperty('value', 3) }

        then:
        remote.exec { ctx.theService.value } == 3
    }

    void "a command can call methods dynamically"() {
        when:
        def methodName = "setValue"
        remote.exec { ctx.theService."$methodName"(1) }

        then:
        remote.exec { ctx.theService.value } == 1

        when:
        remote.exec { ctx.theService.invokeMethod(methodName, 2) }

        then:
        remote.exec { ctx.theService.value } == 2
    }


}
