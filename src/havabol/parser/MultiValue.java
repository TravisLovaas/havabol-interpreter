package havabol.parser;

import havabol.storage.*;

import java.util.ArrayList;
import java.util.List;

import havabol.error.IndexError;
import havabol.error.TypeError;
import havabol.lexer.*;

public class MultiValue {
	
	public DataType dataType;
	public Structure structure;
	public List<Value> values = new ArrayList<>();
	public int numItems = 0;
	public int size = 0;
	public String terminatingStr;
	
	public MultiValue() {
		this.dataType = DataType.VOID;
		this.structure = Structure.VOID;
	}
	
	/**
	 * Function: asVoid
	 * Purpose:	 initializes array value as void
	 * @return instance of MultiValue
	 */
	public MultiValue asVoid() {
		this.dataType = DataType.VOID;
		this.structure = Structure.VOID;
		return this;
	}
	
	/**
	 * Function: add
	 * Purpose:  Appends an element to this ResultValue array
	 * @param parser information about  values being parsed
	 * @param value ResultValue to append to this array
	 */
	public void add(Parser parser, Value value) {
		
		this.values.add(value);
		size++;
		numItems++;
		
	}

	/**
	 * Function: toString
	 * Purpose: returns values in array as a string
	 */
	public String toString() {
		
		StringBuilder str = new StringBuilder("[");
		
		for (Value v : values) {
			str.append(v.toString());
			str.append(", ");
		}
		
		str.deleteCharAt(str.length() - 1);
		str.deleteCharAt(str.length() - 1);
		str.append("]");
		
		return str.toString();
		
	}

}
