package havabol.storage;

import havabol.parser.Parser;
import havabol.parser.ResultValue;
import havabol.storage.*;

/*
 * STIdentifier class for the Identifier symbol table entries.
 */
public class STIdentifier extends STEntry
{
	/*
	 * Constructor for STIdentifier subclass
	 */
	public STIdentifier(String tokenStr, DataType declaredType, Structure structure, String parm, int nonLocal) {
		super(tokenStr, 0);
		this.declaredType = declaredType;
		this.structure = structure;
		this.parm = parm;
		this.nonLocal = nonLocal;
	}
	
	public DataType declaredType;
	public Structure structure;
	public int declaredSize = 1;
	public String parm;
	public int nonLocal;
	
	public String toString() {
		return String.format("IDENTIFIER: %s: %s %s %s %d", symbol, declaredType, structure, parm, nonLocal);
	}
	
}