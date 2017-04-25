package havabol.storage;

import java.util.Arrays;

import havabol.error.IndexError;
import havabol.error.TypeError;
import havabol.error.UnsupportedOperationError;
import havabol.parser.*;
import havabol.storage.*;

/*
 * STIdentifier class for the Identifier symbol table entries.
 */
public class STIdentifier extends STEntry
{
	/*
	 * Constructor for STIdentifier subclass
	 */
	public DataType declaredType;
	public StorageStructure structure;
	public int declaredSize = 1; // for use in fixed arrays ONLY
	public int maxPopulatedIndex = 0; // for use in unbounded arrays ONLY
	//public String parm;
	//public int nonLocal;
	
	public Value[] arrayValue;
	public Value value;
	public STIdentifier(String tokenStr, DataType declaredType, StorageStructure structure) {
		super(tokenStr, 0);
		this.declaredType = declaredType;
		this.structure = structure;
	}
	
	/**
	 * Fetches the ResultValue at the given array index if this ResultValue
	 * is an array value.
	 * @param parser Havabol parser that called this method
	 * @param index Index to access in array value
	 * @return a primitive ResultValue corresponding to the value at the given index
	 */
	public Value fetch(Parser parser, int index) {
		
		if (this.structure == StorageStructure.PRIMITIVE) {
			throw new IndexError("Cannot refer to an index of a primitive value");
		}
		
		if (this.structure == StorageStructure.FIXED_ARRAY) {
			if (index >= this.declaredSize) {
				throw new IndexError("Array index is out of bounds");
			}
		}
		
		if (index >= this.arrayValue.length || this.arrayValue[index] == null) {
			throw new IndexError("Reference to undefined array index");
		}
		
		return this.arrayValue[index];
		
	}
	
	/**
	 * Stores the given Value into the given index
	 * @param parser Calling parser object
	 * @param index index to set in this array
	 * @param value value to set at the given index
	 */
	public void set(Parser parser, int index, Value value) {
		
		if (this.structure == StorageStructure.PRIMITIVE) {
			
			if (this.declaredType == DataType.STRING) {
				throw new UnsupportedOperationError("slicing not yet implemented");
			} else {
				throw new IndexError("Cannot refer to an index of a primitive value");
			}
		}
		
		if (this.structure == StorageStructure.FIXED_ARRAY) {
		
			if (index >= this.declaredSize) {
				throw new IndexError("Array index is out of bounds");
			}
			
			this.arrayValue[index] = value.clone();
		
		} else {
			
			// Any index in an unbounded array can be set
			
			if (index >= arrayValue.length) {
				// resize array to fit new index
				int newSize = (index + 1) * 2;
				Value[] resizedArray = Arrays.copyOf(this.arrayValue, newSize);
				this.arrayValue = resizedArray;
			}
			
			this.arrayValue[index] = value.clone();
			
		}
		
	}
	
	public void fetchSlice(Parser parser, int beginIndex, int endIndex) {
		
	}
	
	/**
	 * Sets the stored value of this variable.
	 * @param value the value to store in this variable
	 */
	public void setValue(Value value) {
		if (value.structure == Structure.PRIMITIVE) {
			if (this.structure == StorageStructure.PRIMITIVE) {
				// primitive into primitive
				this.value = value;
			} else {
				if (this.structure == StorageStructure.FIXED_ARRAY) {
					// primitive into fixed array
					for (int i = 0; i < this.declaredSize; i++) {
						this.arrayValue[i] = value.clone();
					}
				} else {
					// primitive into unbounded array
					throw new TypeError("Cannot perform primitive assignment on unbounded array");
				}
			}
		} else if (value.structure == Structure.MULTIVALUE) {
			if (this.structure == StorageStructure.FIXED_ARRAY) {
				// multivalue into fixed array
				if (value.numItems > this.declaredSize) {
					throw new IndexError("Array does not have enough space to store array value");
				} else {
					for (int i = 0; i < value.numItems; i++) {
						this.arrayValue[i] = value.arrayValue.get(i).clone();
					}
				}
			} else {
				// multivalue into unbounded array
				this.arrayValue = value.arrayValue.toArray(new Value[0]);
			}
		}
		//System.out.println("----------------->Value of symbol: " + symbol + " is: " + this.value);
	}
	
	/**
	 * Function:	getValue
	 * @return		Value containing populated parameters
	 */
	public Value getValue() {
		return this.value;
	}
	
	/**
	 * Function:	toString
	 * Purpose:		returns string representation of identifier
	 */
	public String toString() {
		return String.format("IDENTIFIER: %s: %s %s", symbol, declaredType, structure);
	}
	
}