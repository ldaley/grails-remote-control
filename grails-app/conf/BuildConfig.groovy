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
		mavenRepo "https://nexus.codehaus.org/content/repositories/releases"
		
		
	}
	dependencies {
		compile "org.codehaus.groovy.modules.remote:remote-transport-http:0.2"
	}
	plugins {
		compile (
			":tomcat:$grailsVersion",
			":hibernate:$grailsVersion",
			":spock:0.5-groovy-1.7"
		) {
			export = false
		}
	}
}
