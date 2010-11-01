grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits( "global" )
	log "warn"
	repositories {
		grailsCentral()
		grailsHome()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://repository.codehaus.org"
		
		compile "org.codehaus.groovy.modules.remote:remote-transport-http:0.1"
	}
}
