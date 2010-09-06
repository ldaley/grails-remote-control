package grails.plugin.remotecontrol.client

import grails.plugin.spock.*

class RemoteClosureDefinitionGeneratorSpec extends UnitSpec {

	def "generate with no supports"() {
		when:
		def generator = new RemoteClosureDefinitionGenerator() { "123" }
		def definition = generator.generate()
		then:
		definition.root != null
		definition.supports.size() == 0
	}

	def "generate with supports"() {
		when:
		def generator = new RemoteClosureDefinitionGenerator() { 
			def c = { -> "123" } 
		}
		def definition = generator.generate()
		then:
		definition.root != null
		definition.supports.size() == 1
	}


	def "generate with lots of supports"() {
		when:
		def generator = new RemoteClosureDefinitionGenerator() { 
			def c = { -> 
				def a = { -> 
					def b = { -> }
				}
			} 
			def d = { -> }
		}
		def definition = generator.generate()
		then:
		definition.root != null
		definition.supports.size() == 4
	}

}