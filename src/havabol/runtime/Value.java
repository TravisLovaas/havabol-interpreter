package havabol.runtime;

import havabol.storage.DataType;

public class Value {
	
	public DataType dataType;
	public String strValue;
	public int intValue;
	public double floatValue;
	public boolean booleanValue;
	
	public Value(DataType dataType, String value) {
		this.dataType = dataType;
		this.strValue = value;
	}
	
	public Value(DataType dataType, int value) {
		this.dataType = dataType;
		this.intValue = value;
	}
	
	public Value(DataType dataType, double value) {
		this.dataType = dataType;
		this.floatValue = value;
	}
	
	public Value(DataType dataType, boolean value) {
		this.dataType = dataType;
		this.booleanValue = value;
	}
	
	public Object getValue() {
		switch (this.dataType) {
		case STRING:
			return strValue;
		case INTEGER:
			return intValue;
		case FLOAT:
			return floatValue;
		case BOOLEAN:
			return booleanValue;
		default:
			return null;
		}
	}

}
