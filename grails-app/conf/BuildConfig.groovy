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
	}
	dependencies {
		compile "org.codehaus.groovy.modules.remote:remote-transport-http:0.5"
	}
	plugins {
		compile (
			":tomcat:$grailsVersion",
			":hibernate:$grailsVersion",
			":spock:0.6",
		) {
			export = false
		}
		
		build(":release:2.0.4") {
			export = false
		}
	}
}

grails.release.scm.enabled = false