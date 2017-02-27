package havabol.storage;

import havabol.storage.*;

/*
 * STIdentifier class for the Identifier symbol table entries.
 */
public class STIdentifier extends STEntry
{
	/*
	 * Constructor for STIdentifier subclass
	 */
	public STIdentifier(String tokenStr, int primClassif, DataType declaredType, Structure structure, String parm, int nonLocal) {
		super(tokenStr, primClassif);
		this.declaredType = declaredType;
		this.structure = structure;
		this.parm = parm;
		this.nonLocal = nonLocal;
	}
	
	DataType declaredType;
	Structure structure;
	String parm;
	int nonLocal;
	
	public String toString() {
		return String.format("IDENTIFIER: %s: %s %s %s %d", symbol, declaredType, structure, parm, nonLocal);
	}
	
}