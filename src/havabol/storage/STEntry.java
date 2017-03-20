package havabol.storage;

import havabol.parser.Parser;
import havabol.parser.ResultValue;
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
	ResultValue value;
	
	public String toString() {
		return String.format("GENERIC ENTRY: %s: %d", symbol, primClassif);
	}
	
	public void setValue(ResultValue value) {
		this.value = value;
		System.out.println("----------------->Value of symbol: " + symbol + " is: " + this.value);
	}
	
	public ResultValue getValue() {
		return this.value;
	}
	
}