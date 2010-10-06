package grails.plugin.remotecontrol

class Result implements Serializable {

	boolean wasNull = false
	boolean wasUnserializable = false
	boolean wasThrown = false
	String stringRepresentation = null
	Serializable value = null
	
	static forNull() {
		new Result(wasNull: true)
	}
	
	static forValue(value) {
		if (value == null) {
			forNull()
		} else if (value instanceof Serializable) {
			forSerializable(value)
		} else {
			forUnserializable(value)
		}
	}
	
	static forThrown(Throwable thrown) {
		new Result(
			wasThrown: true,
			value: thrown
		)
	}
	
	private static forUnserializable(unserialisable) {
		new Result(
			wasUnserializable: true,
			stringRepresentation: unserialisable.toString()
		)
	}
	
	private static forSerializable(Serializable serialisable) {
		new Result(
			value: serialisable,
			stringRepresentation: serialisable.toString()
		)
	}
}

