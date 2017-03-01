package havabol.storage;

import havabol.runtime.Value;

/*
 *  STEntry class for handling Symbol Table entries.
 */
public class STEntry
{
	/*
	 * Constructor for the STEntry class
	 */
	public STEntry(String tokenStr, int primClassif) {
		this.symbol = tokenStr;
		this.primClassif = primClassif;
	}

	String symbol;
	int primClassif;
	Value value;
	
	public String toString() {
		return String.format("GENERIC ENTRY: %s: %d", symbol, primClassif);
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	public Value getValue() {
		return this.value;
	}
	
}