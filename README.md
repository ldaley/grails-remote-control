[![Build Status](https://travis-ci.org/puneetbehl/grails-remote-control.svg?branch=master)](https://travis-ci.org/puneetbehl/grails-remote-control)

The Grails remote-control plugin allows you to execute code inside a remote Grails application. The typical use case for this is for functional testing where you are testing an application inside a separate JVM and therefore do not have easy access to the application runtime. If you can access the application runtime environment then you can do things like change service parameter values, create and delete domain data and so forth.

The plugin uses the [Groovy Remote Control](https://github.com/alkemist/remote-control "Groovy Remote Control") library.

**This plugin requires Grails 3.x but will not work on Grails 2.x and earlier versions**

## An Example

### The Test

We have written an application and now want to write some functional tests. In these tests we need to create some test data. This might look something like…
```groovy
@Integration
@Transactional
class PersonSpec extends Specification {

    def "store new Person" () {
        given:
        def person = new Person (name: 'Me')
        person.save (flush: true)

        expect:
        // Somehow make some HTTP request and test that person is in the DB

        cleanup:
        person.delete (flush: true)
    }
}
```
That will work if we are running our tests in the same JVM as the running application, which is the default behaviour…
```sh
grails test-app -integration
```
However, it won't work if your tests ARE NOT in the same JVM, as is the case with testing against a WAR deployment (which could be useful to be closer to a production environment) …

```sh
grails test run-app -port=8090
grails test-app -integration -DbaseUrl=http://localhost:8090
```
or...

```sh
grails war
java -Dgrails.env=test -jar build/libs/remote-control-3.0.0.war
grails test-app -integration -DbaseUrl=http://localhost:8090
```

This is going to fail because in the JVM that is running the tests there is no Grails application (the WAR is run in a forked JVM to be closer to a production like environment).

> Important Note: you have to explicitly tell gradle to pass the system property `-DbaseUrl=...` to the integration test run. Add the following setting to  `build.gradle`:
>
>     [integrationTest].each { runTask ->
>         configure(runTask) {
>             systemProperties System.properties
>         }
>     }


### Existing Solutions

The most common existing workaround for this problem is to write a special controller that you call via HTTP in your functional tests to do the setup/teardown. This will work, but requires effort and is inherently fragile.

### Using a remote control

The remote control plugin solves this problem by allowing you to define closures to be executed in the application you are testing. This is best illustrated by rewriting the above test…
```groovy
import grails.plugins.remotecontrol.RemoteControl

@Integration
@Transactional
class PersonSpec extends Specification {
    RemoteControl remote

    void setup () {
        remote = new RemoteControl()
    }

    void testSave () {
        setup:
        def id = remote.exec {
            def person = new Person (name: "Me")
            person.save()
            person.id
        }

        expect:
        // Somehow make some HTTP request and test that person is in the DB

        cleanup:
        remote.exec { Person.get(id).delete() }
    }
}
```
This test will now working when testing against a WAR or a local version. The closures passed to `remote` are sent over HTTP to the running application and executed there, so it doesn't matter where the application is.

#### Chaining

Closures can be *chained*, with the return value of the previous closure being passed as an argument to the next closure in the chain. This is done on the server side, so it's ok for a closure to return a non serialisable value to be given to the next one. An example use for this would be reusing a closure to fetch some value, and then using another closure to process it.
```groovy
import grails.plugins.remotecontrol.RemoteControl

@Integration
@Transactional
class PersonSpec extends Specification {
    RemoteControl remote

    void setup () {
        remote = new RemoteControl ()
    }

    def getPerson = { id -> Person.get(id) }

    def modifyPerson(id, Closure modifications) {
        // pass the result of the getPerson command to the
        // given modifications command
        remote.exec(getPerson.curry(id), modifications)
    }

    def "testModifyPerson" () {
        setup:
        Long id = remote {
            Person person = new Person(name: "Me")
            person.save()
            person.id
        }
        // Somehow make some HTTP request and test that person is in the DB

        when:
        // Change the name
        modifyPerson(id) {
            it.setName("New Name")
            it.save(flush: true)
            null // return must be serializable
        }

        then:
        // Somehow make some HTTP request and test that the person's name has changed

        cleanup:
        modifyPerson(id) {
            it.delete()
        }
    }
}
```
A more concise example of how values are passed to the next command in the chain would be…
```groovy
assert remote.exec({ 1 }, { it + 1 }, { it + 1 }) == 3
```

#### Context

The Groovy Remote Control library establishes a [command context](http://groovy.codehaus.org/modules/remote/manual/latest/contexts.html "Groovy Remote Control - Command Context") that is shared for all commands in a given chain.

This plugin prepoluates the context with two variables:

* `ctx` - The main application context
* `app` - The grails application object

This allows you to access beans (such as services) from commands…
```groovy
remote.exec { ctx.someService.doSomeServiceStuff() }
```

##### Populating The Context

You can also set values in the context. This is sometimes useful when using a command chain where an initial command sets up some context.
```groovy
def getPersonWithId = { person = Person.get(it) }
def doubleAge = { person.age *= 2 }
def savePerson = { person.save(flush: true) }

def doubleAgeOfPersonWithId(id) {
    remote.exec(getPersonWithId.curry(id), doubleAge, savePerson)
}

doubleAgeOfPersonWithId(10)
```

#### More Examples

To see some more usage examples of a remote control, see the [demonstration test case](http://github.com/alkemist/grails-remote-control/blob/master/src/integration-test/groovy/remotecotrol/RemoteComtrol.groovy) in the project.

### Testing Remote Apps

Let's say that we want to functionally test our application on different flavours of application server and we have our app deployed on three different app servers at the following URLs:

* http://appsrv1.test.my.org/myapp
* http://appsrv2.test.my.org/myapp
* http://appsrv3.test.my.org/myapp

If we have the remote-control plugin installed and have written our tests to use it, we could simply run:
```sh
grails test-app -integration -DbaseUrl=http://appsrv1.test.my.org/myapp
grails test-app -integration -DbaseUrl=http://appsrv2.test.my.org/myapp
grails test-app -integration -DbaseUrl=http://appsrv3.test.my.org/myapp
```

Which will execute the tests against that remote instance.

### Security

By default, the servlet that accepts remote commands is only configured when the application is started in the **test** environment. This means that it is not possible to use a remote with a production application out of the box.

However, if you do want to enable the remote control servlet that accepts commands in an environment other than production you can set `remoteControl.enabled` to `true` in the application config for that environment.

### Testing applications with catch-all URL Mappings

Your remote control will not work if you have a catch-all URL mapping, e.g.

	class UrlMappings {
		static mappings = {
			"/**"(controller:'boss')
		}
	}

You will see `groovyx.remote.RemoteControlException: Error sending command chain`.  The fix is to add the following line to URL Mappings:

	static excludes = ['/grails-remote-control']

Your URL mappings should now look like:

	class UrlMappings {
		static excludes = ['/grails-remote-control']
		static mappings = {
			"/**"(controller:'boss')
		}
	}
