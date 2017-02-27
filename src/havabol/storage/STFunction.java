package havabol.storage;


/*
 * STFunction class for the Function symbol table entries.
 */
class STFunction extends STEntry
{
	/*
	 * Constructor for STFunction subclass
	 */
	public STFunction(String tokenStr, int primClassif, int returnType, int builtin, int numArgs) {
		super(tokenStr, primClassif);
		this.returnType = returnType;
		this.definedBy = builtin;
		this.numArgs = numArgs;
	}
	int returnType;
	int definedBy;
	int numArgs;
	SymbolTable symbolTable;
	
	public String toString() {
		return String.format("FUNCTION: %s: %s %s %d", symbol, returnType, definedBy, numArgs);
	}
	
}
