The Grails remote-control plugin allows you to execute code inside a remote Grails application. The typical use case for this is for functional testing where you are testing an application inside a separate JVM and therefore do not have easy access to the application runtime. If you can access the application runtime environment then you can do things like change service parameter values, create and delete domain data and so forth.

**This plugin requires Grails 1.3.5 and will not work on earlier versions**

**There is currently no way to turn this plugin off for certain environments once installed (there will be in future versions) so DO NOT USE IN A PRODUCTION ENVIRONMENT, it is inherently a security hole**

## An Example

### The Test

We have written an application and now want to write some functional tests. In these tests we need to create some test data. This might look something like…

    class MyFunctionalTest extends GroovyTestCase {
        
        def testIt() {
            def person = new Person(name: "Me")
            person.save(flush: true)
            
            // Somehow make some HTTP request and test that person is in the DB
            
            person.delete(flush: true)
        }
        
    }

That will work if we are running our tests in the same JVM as the running application, which is the default behaviour…

    grails test-app functional:

However, it won't work if your tests ARE NOT in the same JVM, as is the case with testing against a WAR deployment…

    grails test-app functional: -war
    
This is going to fail because in the JVM that is running the tests there is no Grails application (the WAR is run in a forked JVM to be closer to a production like environment).


### Existing Solutions

The most common existing workaround for this problem is to write a special controller that you call via HTTP in your functional tests to do the setup/teardown. This will work, but requires effort and is inherently fragile.

### Using a remote control

The remote control plugin solves this problem by allowing you to define closures to be executed in the application you are testing. This is best illustrated by rewriting the above test…

    import grails.plugin.remotecontrol.RemoteControl
    
    class MyFunctionalTest extends GroovyTestCase {
        
        def remote = new RemoteControl()
        
        def testIt() {
            remote {
                def person = new Person(name: "Me")
                person.save(flush: true)
            }
            
            // Somehow make some HTTP request and test that person is in the DB
            
            remote {
                person.delete(flush: true)
            }
        }
    }

This test will now working when testing agains a WAR or a local version. The closures passed to `remote` are sent over HTTP to the running application and executed there, so it doesn't matter where the application is.
