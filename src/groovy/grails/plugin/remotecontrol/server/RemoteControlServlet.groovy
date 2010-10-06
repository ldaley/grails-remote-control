package grails.plugin.remotecontrol.server

import javax.servlet.*
import javax.servlet.http.*
import grails.plugin.remotecontrol.*
import org.codehaus.groovy.grails.commons.ApplicationHolder

class RemoteControlServlet extends HttpServlet {
	
	void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if (request.contentType != ContentType.COMMAND.value) {
				response.sendError(415, "Only grails remote control commands can be sent")
				return
			}

			response.contentType = ContentType.RESULT.value
			receiver.execute(request.inputStream, response.outputStream)
		} catch (thrown) {
			thrown.printStackTrace()
		}
	}

	def getReceiver() {
		new Receiver(grailsApplication)
	}
	
	def getGrailsApplication() {
		ApplicationHolder.application
	}
}