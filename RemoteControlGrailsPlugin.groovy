class RemoteControlGrailsPlugin {

	def version = "0.1"
	def grailsVersion = "1.3.5 > *"
	def dependsOn = [:]
	def pluginExcludes = [
			"grails-app/views/error.gsp"
	]
	def author = "Your name"
	def authorEmail = ""
	def title = "Plugin summary/headline"
	def description = 'Brief description of the plugin.'
	def documentation = "http://grails.org/plugin/grails-remote-control"

	def doWithWebDescriptor = { webXml ->
		def servlets = webXml.servlet 
		def lastServlet = servlets[servlets.size() - 1] 
		lastServlet + { 
			servlet { 
				'servlet-name'('grails-remote-control') 
				'servlet-class'('grails.plugin.remotecontrol.server.RemoteControlServlet') 
				'load-on-startup'(1) 
			} 
		} 
		def mappings = webXml.'servlet-mapping' 
		def lastMapping = mappings[mappings.size() - 1] 
		lastMapping + { 
			'servlet-mapping' { 
				'servlet-name'('grails-remote-control') 
				'url-pattern'("/grails-remote-control") 
			} 
		} 
	}
}