package havabol;

import java.util.*;

public class SymbolTable
{
	public HashMap<String, STEntry> ST = new HashMap<>();
	int VAR_ARGS;
	public SymbolTable()
	{
		initGlobal();
		
		//Set<STEntry> set = ST.entrySet();
		// Get an iterator
		//Iterator i = set.iterator();
	    // Display elements
		//while(i.hasNext()) {
		//	Map.Entry me = (Map.Entry)i.next();
		//	System.out.printf("%10s: %s\n",me.getKey(),me.getValue());
		//}   
	}
	
	private void initGlobal()
	{
		ST.put("def", new STControl("def", Token.CONTROL, Token.FLOW));
		ST.put("enddef", new STControl("def", Token.CONTROL, Token.END));
		ST.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
		ST.put("endif", new STControl("endif",Token.CONTROL,Token.END));
		ST.put("else", new STControl("else", Token.CONTROL, Token.FLOW));
		ST.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
		ST.put("endfor", new STControl("endfor",Token.CONTROL,Token.END));
		ST.put("while", new STControl("while",Token.CONTROL,Token.FLOW));
		ST.put("endwhile", new STControl("endwhile",Token.CONTROL,Token.END));
		ST.put("print", new STFunction("print",Token.FUNCTION,Token.VOID,Token.BUILTIN, VAR_ARGS));
		ST.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
		ST.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
		ST.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
		ST.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
		ST.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));
		ST.put("LENGTH", new STFunction("LENGTH",Token.FUNCTION,Token.INTEGER,Token.BUILTIN, VAR_ARGS));
		ST.put("MAXLENGTH", new STFunction("MAXLENGTH",Token.FUNCTION,Token.INTEGER,Token.BUILTIN, VAR_ARGS));
		ST.put("SPACES", new STFunction("SPACES",Token.FUNCTION,Token.INTEGER,Token.BUILTIN, VAR_ARGS));
		ST.put("ELEM", new STFunction("ELEM",Token.FUNCTION,Token.INTEGER,Token.BUILTIN, VAR_ARGS));
		ST.put("MAXELEM", new STFunction("MAXELEM",Token.FUNCTION,Token.INTEGER,Token.BUILTIN, VAR_ARGS));
		ST.put("and", new STEntry("and", Token.OPERATOR));
		ST.put("or", new STEntry("or", Token.OPERATOR));
		ST.put("not", new STEntry("not", Token.OPERATOR));
		ST.put("in", new STEntry("in", Token.OPERATOR));
		ST.put("notin", new STEntry("notin", Token.OPERATOR));
		 
	}
	
	//returns the symbol and its corresponding entry in the symbol table.
	STEntry getSymbol(String symbol)
	{
		return (STEntry) ST.get(symbol);
	}
	//stores the symbol and its corresponding entry in the symbol table
	void putSymbol(String symbol, STEntry entry)
	{
		ST.put(symbol, entry);
	}
}

class STEntry
{
	
	public STEntry(String tokenStr, int primClassif) {
		this.symbol = tokenStr;
		this.primClassif = primClassif;
	}
	
	HashMap STEntry = new HashMap();
	String symbol;
	int primClassif;	
}

class STIdentifier extends STEntry
{
	
	public STIdentifier(String tokenStr, int primClassif, String dclType, String structure, String parm, int nonLocal) {
		super(tokenStr, primClassif);
		this.dclType = dclType;
		this.structure = structure;
		this.parm = parm;
		this.nonLocal = nonLocal;
	}
	
	String dclType;
	String structure;
	String parm;
	int nonLocal;
}

class STFunction extends STEntry
{
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
}

class STControl extends STEntry
{
	public STControl(String tokenStr, int primClassif, int subClassif) {
		super(tokenStr, primClassif);
		this.subClassif = subClassif;
	}
	int subClassif;
}
