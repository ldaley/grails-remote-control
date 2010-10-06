import grails.plugin.remotecontrol.RemoteControl

class TheTests extends GroovyTestCase {

	void testIt() {
		def name = RemoteControl.exec {
			grailsApplication.metadata['app.name']
		}
		
		assert name == "grails-remote-control"
	}
}
