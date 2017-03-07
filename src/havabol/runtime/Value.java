package havabol.runtime;

import java.util.ArrayList;

import havabol.storage.*;

public class Value<T> {
	
	public DataType dataType;
	public T value;
	public ArrayList<T> arrayValue;
	public Structure structure;
	
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
	
	public T getValue() {
		return value;
	}

}
