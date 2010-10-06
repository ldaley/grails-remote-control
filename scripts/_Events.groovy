eventTestSuiteStart = {
	println "functionalTestBaseUrl $grailsSettings.functionalTestBaseUrl"
	if (grailsSettings.functionalTestBaseUrl) {
		def remoteControlClass = classLoader.loadClass("grails.plugin.remotecontrol.RemoteControl")
		remoteControlClass.defaultReceiverAddress = grailsSettings.functionalTestBaseUrl + "grails-remote-control"
	}
}

eventAllTestsStart = {
	if (grailsAppName == "grails-remote-control") {
		functionalTests << "functional"
	}
}