import grails.plugin.remotecontrol.RemoteControl

class TheTests extends GroovyTestCase {

	void testIt() {
		def name = RemoteControl.exec {
			grailsApplication.metadata['app.name']
		}
		
		assert name == "grails-remote-control"
	}
	
	void testWithInnerClosures() {
		assert [2,3,4] == RemoteControl.exec {
			[1,2,3].collect { it + 1 }
		}
	}
}
