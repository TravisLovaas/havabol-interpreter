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
	
	public STEntry(String tokenStr) {
		this.symbol = tokenStr;
	}

	public String symbol;
	public int primClassif;
	
	/**
	 * Function:	toString
	 * Purpose:		return string representation of generic token entered into the manager
	 */
	public String toString() {
		return String.format("GENERIC ENTRY: %s: %d", symbol, primClassif);
	}
	
}