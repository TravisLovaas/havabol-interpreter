package havabol.storage;

import havabol.lexer.Token;

/*
 * STFunction class for the Function symbol table entries.
 */
public class STFunction extends STEntry
{
	
	public DataType returnType;
	public int subClassif;
	public int numArgs;
	public SymbolTable symbolTable;
	
	/*
	 * Constructor for STFunction subclass
	 */
	public STFunction(String tokenStr, int primClassif, DataType returnType, int subClassif, int numArgs) {
		super(tokenStr, primClassif);
		this.returnType = returnType;
		this.subClassif = subClassif;
		this.numArgs = numArgs;
	}

	/**
	 * Function:	toString
	 * Purpose:		returns string representation of function
	 */
	public String toString() {
		return String.format("FUNCTION: %s: returns: %s classification: %s num args: %d", symbol, returnType, subClassif, numArgs);
	}
	
}
