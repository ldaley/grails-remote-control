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
        compile "org.springframework:spring-jdbc:4.1.9.RELEASE"
        compile "org.springframework:spring-context:4.1.9.RELEASE"
        compile "org.springframework:spring-aop:4.1.9.RELEASE"
        compile "org.springframework:spring-expression:4.1.9.RELEASE"

		test 'org.spockframework:spock-core:1.1-groovy-2.4'
		test 'junit:junit:4.12'
		test 'org.hamcrest:hamcrest-core:1.3'
	}
	plugins {
		compile (
			":tomcat:7.0.70",
            ":hibernate4:4.3.10"
		) {
			export = false
		}

		build ':release:3.1.2', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}

grails.release.scm.enabled = false
