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
	
	public MultiValue asVoid() {
		this.dataType = DataType.VOID;
		this.structure = Structure.VOID;
		return this;
	}
	
	/**
	 * Appends an element to this ResultValue array
	 * @param parser Calling parser
	 * @param value ResultValue to append to this array
	 */
	public void add(Parser parser, Value value) {
		
		this.values.add(value);
		size++;
		numItems++;
		
	}
	
	/**
	 * Stores the given ResultValue into the index of this array ResultValue
	 * @param parser Calling parser object
	 * @param index index to set in this array
	 * @param value value to set at the given index
	 */
	public void set(Parser parser, int index, Value value) {
		
		if (this.structure == Structure.PRIMITIVE) {
			throw new IndexError("Cannot refer to an index of a primitive value");
		}
		
		// TODO: add value to array
		
	}
	
	/**
	 * Fetches the ResultValue at the given array index if this ResultValue
	 * is an array value.
	 * @param parser Havabol parser that called this method
	 * @param index Index to access in array value
	 * @return a primitive ResultValue corresponding to the value at the given index
	 */
	public Value fetch(Parser parser, int index) {
		
		if (this.structure == Structure.PRIMITIVE) {
			throw new IndexError("Cannot refer to an index of a primitive value");
		}
		
		if (index >= this.size) {
			throw new IndexError("Internal array index is out of bounds");
		}
		
		if (index >= this.numItems) {
			throw new IndexError("Array index is out of bounds");
		}
		
		return this.values.get(index);
		
	}

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
