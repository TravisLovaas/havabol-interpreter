package havabol.storage;

/*
 * STControl class for the Control symbol table entries.
 */
class STControl extends STEntry
{
	/*
	 * Constructor for the STControl subclass
	 */
	public STControl(String tokenStr, int primClassif, int subClassif) {
		super(tokenStr, primClassif);
		this.subClassif = subClassif;
	}
	int subClassif;
	
	/**
	 * Function:	toString
	 * Purpose:		return string representation of token entered into the manager
	 */
	public String toString() {
		return String.format("CONTROL: %s: Control subclass: %s", symbol, subClassif);
	}
}
