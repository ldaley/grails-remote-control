package grails.plugin.remotecontrol

class Command implements Serializable {
	byte[] instance
	byte[] root
	List supports = []
}
