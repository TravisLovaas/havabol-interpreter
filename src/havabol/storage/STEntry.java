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
	
	/**
	 * Function:	toString
	 * Purpose:		return string representation of generic token entered into the manager
	 */
	public String toString() {
		return String.format("GENERIC ENTRY: %s: %d", symbol, primClassif);
	}
	
	/**
	 * Function:	setValue
	 * @param value the value whose parameters are to be set
	 */
	public void setValue(Value value) {
		
		// TODO: check size and move to STIdentifier
		
		this.value = value;
		//System.out.println("----------------->Value of symbol: " + symbol + " is: " + this.value);
	}
	
	/**
	 * Function:	getValue
	 * @return		Value containing populated parameters
	 */
	public Value getValue() {
		return this.value;
	}
}