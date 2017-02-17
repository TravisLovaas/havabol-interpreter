package havabol;

import java.util.*;

public class SymbolTable
{
	public HashMap<String, Object> ST = new HashMap<String, Object>();
	
	public SymbolTable()
	{
		initGlobal();
		
		Set set = ST.entrySet();
		// Get an iterator
		Iterator i = set.iterator();
	    // Display elements
		//while(i.hasNext()) {
		//	Map.Entry me = (Map.Entry)i.next();
		//	System.out.printf("%10s: %s\n",me.getKey(),me.getValue());
		//}   
	}
	
	private void initGlobal()
	{
		ST.put("def", 10);
		ST.put("enddef", 10);
		ST.put("if", new STControl("if",Token.CONTROL,Token.FLOW));
		ST.put("endif", new STControl("endif",Token.CONTROL,Token.END));
		ST.put("else", 10);
		ST.put("for", new STControl("for",Token.CONTROL,Token.FLOW));
		ST.put("endfor", 10);
		ST.put("while", 10);
		ST.put("endwhile", 10);
		ST.put("print", new STFunction("print",Token.FUNCTION,Token.VOID));
		ST.put("Int", new STControl("Int",Token.CONTROL,Token.DECLARE));
		ST.put("Float", new STControl("Float",Token.CONTROL,Token.DECLARE));
		ST.put("String", new STControl("String",Token.CONTROL,Token.DECLARE));
		ST.put("Bool", new STControl("Bool",Token.CONTROL,Token.DECLARE));
		ST.put("Date", new STControl("Date",Token.CONTROL,Token.DECLARE));
		ST.put("LENGTH", 10);
		ST.put("MAXLENGTH", 10);
		ST.put("SPACES", 10);
		ST.put("ELEM", 10);
		ST.put("MAXELEM", 10);
		ST.put("and", new STEntry("and", Token.OPERATOR));
		ST.put("or", new STEntry("or", Token.OPERATOR));
		ST.put("not", new STEntry("not", Token.OPERATOR));
		ST.put("in", new STEntry("in", Token.OPERATOR));
		ST.put("notin", new STEntry("notin", Token.OPERATOR));
		 
	}
	
	
	class STEntry
	{
		public STEntry(String string, int operator){
			
		}
		HashMap STEntry = new HashMap();
		char symbol;
		String primClassif;
		
	}
	class STIdentifier extends STEntry
	{
		public STIdentifier(String string, int operator) {
			super(string, operator);
			// TODO Auto-generated constructor stub
		}
		HashMap STIdentifier = new HashMap();
		String dclType;
		String structure;
		String parm;
		int nonLocal;
	}
	class STFunction extends STEntry
	{
		public STFunction(String string, int operator, int void1) {
			super(string, operator);
			// TODO Auto-generated constructor stub
		}
		HashMap STFunction = new HashMap();
		String returnType;
		String definedBy;
		int numArgs;
		ArrayList parmList;
		SymbolTable symbolTable;
	}
	class STControl extends STEntry
	{
		public STControl(String string, int operator, int declare) {
			super(string, operator);
			// TODO Auto-generated constructor stub
		}
		HashMap STControl = new HashMap();
		String subClassif;
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