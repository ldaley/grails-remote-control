grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.dependency.resolution = {
	inherits( "global" )
	log "warn"
	repositories {
		grailsCentral()
		grailsHome()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://dl.bintray.com/alkemist/maven"
	}
	dependencies {
		compile "io.remotecontrol:remote-core:0.7"
		compile "io.remotecontrol:remote-transport-http:0.7", {
			excludes "servlet-api"
		}
	}
	plugins {
		compile (
			":tomcat:$grailsVersion",
			":hibernate:$grailsVersion",
			":spock:0.6"
		) {
			export = false
		}
		
		build(":release:2.0.4") {
			export = false
		}
	}
}

grails.release.scm.enabled = false
