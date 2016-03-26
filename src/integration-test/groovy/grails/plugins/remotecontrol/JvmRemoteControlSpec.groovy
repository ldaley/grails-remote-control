package grails.plugins.remotecontrol

import grails.test.mixin.integration.Integration
import io.remotecontrol.client.RemoteException
import spock.lang.Requires
import spock.lang.Specification

@Requires({ System.getProperty("baseUrl") })
@Integration
class JvmRemoteControlSpec extends Specification {

    RemoteControl remote

    void setup() {
        remote = new RemoteControl()
    }

    void "any classes referenced have to be available in the remote app, and any classes defined in the test ARE NOT"() {
        def remoteControlLocal = new RemoteControlLocal()

        when:
        remote.exec { remoteControlLocal }

        then:
        RemoteException e = thrown()
        e.cause.cause.class == ClassNotFoundException
    }

    void "should throw NoClassDefFoundError as class is not in the remote app"() {
        when:
        remote.exec { new RemoteControlLocal() }

        then:
        RemoteException e = thrown()
        e.cause.class == NoClassDefFoundError
    }

    void "should thow MissingPropertyException when trying to access a property that do-not exists in the delegate"() {
        when:
        remote.exec { iDontExist == true }

        then:
        RemoteException e = thrown()
        e.cause.class == MissingPropertyException
    }

}
