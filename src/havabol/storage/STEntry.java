package havabol.storage;

import havabol.parser.Parser;
import havabol.parser.Value;

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

	public String symbol;
	public int primClassif;
	private Value value;
	
	public String toString() {
		return String.format("GENERIC ENTRY: %s: %d", symbol, primClassif);
	}
	
	public void setValue(Value value) {
		
		// TODO: check size and move to STIdentifier
		
		this.value = value;
		//System.out.println("----------------->Value of symbol: " + symbol + " is: " + this.value);
	}
	
	public Value getValue() {
		return this.value;
	}
	
}