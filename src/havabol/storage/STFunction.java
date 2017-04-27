package havabol.storage;

import java.util.Map;

import havabol.lexer.Token;

/*
 * STFunction class for the Function symbol table entries.
 */
public class STFunction extends STEntry
{
	
	public DataType returnType;
	public Map<String, DataType> formalParameters;
	public int beginExecSrcLine;
	public int beginExecColPos;
	
	/*
	 * Constructor for STFunction subclass
	 */
	public STFunction(String tokenStr, DataType returnType, Map<String, DataType> formalParameters, int beginExecSrcLine, int beginExecColPos) {
		super(tokenStr);
		this.returnType = returnType;
		this.formalParameters = formalParameters;
		this.beginExecColPos = beginExecColPos;
		this.beginExecSrcLine = beginExecSrcLine;		
	}

	/**
	 * Function:	toString
	 * Purpose:		returns string representation of function
	 */
	public String toString() {
		return String.format("FUNCTION: %s: returns: %s num args: %d", symbol, returnType, formalParameters.size());
	}
	
}
