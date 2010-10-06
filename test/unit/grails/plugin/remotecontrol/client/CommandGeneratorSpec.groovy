package grails.plugin.remotecontrol.client

import grails.plugin.spock.*
import spock.lang.*

class CommandGeneratorSpec extends UnitSpec {

	def generator = new CommandGenerator(this.getClass().classLoader)
	
	def "support size"() {
		expect:
		generator.generate(command).supports.size() == size
		where:
		command                                                              | size
		{ -> "123" }                                                         | 0
		{ -> def c = { -> "123" } }                                          | 1
		{ -> def c = { -> def a = { -> def b = { -> } } }; def d = { -> } }  | 4
	}

}