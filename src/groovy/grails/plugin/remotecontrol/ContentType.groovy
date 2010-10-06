package grails.plugin.remotecontrol

enum ContentType {
	
	COMMAND("application/grails-remote-control-command"),
	RESULT("application/grails-remote-control-result")
	
	final value
	
	ContentType(value) {
		this.value = value
	}
	
}