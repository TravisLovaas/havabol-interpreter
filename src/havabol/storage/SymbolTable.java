package havabol.storage;

import java.util.*;

import havabol.error.DeclarationError;
import havabol.lexer.Token;
import havabol.parser.*;

public class SymbolTable {
	
	public HashMap<String, STEntry> ST = new HashMap<>();
	public String functionName;
	public List<String> enclosingFunctions = new ArrayList<>();

	/**
	 * main function for Symbol Table class which calls initGlobal
	 */
	public SymbolTable(String functionName){
		this.functionName = functionName;
	}

	/**
	 * initGlobal function for initializing the Global Symbol Table with put
	 * functions.
	 */

	public void initGlobal(){
		ST.put("def", new STControl("def", Token.CONTROL, Token.FLOW));
		ST.put("enddef", new STControl("def", Token.CONTROL, Token.END));
		ST.put("if", new STControl("if", Token.CONTROL, Token.FLOW));
		ST.put("endif", new STControl("endif", Token.CONTROL, Token.END));
		ST.put("else", new STControl("else", Token.CONTROL, Token.FLOW));
		ST.put("for", new STControl("for", Token.CONTROL, Token.FLOW));
		ST.put("endfor", new STControl("endfor", Token.CONTROL, Token.END));
		ST.put("while", new STControl("while", Token.CONTROL, Token.FLOW));
		ST.put("endwhile", new STControl("endwhile", Token.CONTROL, Token.END));
		//ST.put("print", new STFunction("print", Token.FUNCTION, DataType.VOID, Token.BUILTIN, VAR_ARGS));
		ST.put("Int", new STControl("Int", Token.CONTROL, Token.DECLARE));
		ST.put("Float", new STControl("Float", Token.CONTROL, Token.DECLARE));
		ST.put("String", new STControl("String", Token.CONTROL, Token.DECLARE));
		ST.put("Bool", new STControl("Bool", Token.CONTROL, Token.DECLARE));
		ST.put("Date", new STControl("Date", Token.CONTROL, Token.DECLARE));
		//ST.put("LENGTH", new STFunction("LENGTH", Token.FUNCTION, DataType.INTEGER, Token.BUILTIN, VAR_ARGS));
		//ST.put("MAXLENGTH", new STFunction("MAXLENGTH", Token.FUNCTION, DataType.INTEGER, Token.BUILTIN, VAR_ARGS));
		//ST.put("SPACES", new STFunction("SPACES", Token.FUNCTION, DataType.BOOLEAN, Token.BUILTIN, VAR_ARGS));
		//ST.put("ELEM", new STFunction("ELEM", Token.FUNCTION, DataType.INTEGER, Token.BUILTIN, VAR_ARGS));
		//ST.put("MAXELEM", new STFunction("MAXELEM", Token.FUNCTION, DataType.INTEGER, Token.BUILTIN, VAR_ARGS));
		ST.put("and", new STEntry("and", Token.OPERATOR));
		ST.put("or", new STEntry("or", Token.OPERATOR));
		ST.put("not", new STEntry("not", Token.OPERATOR));
		ST.put("in", new STEntry("in", Token.OPERATOR));
		ST.put("notin", new STEntry("notin", Token.OPERATOR));

	}
	
	public boolean isFunction(String symbol) {
		if (ST.containsKey(symbol)) {
			return ST.get(symbol) instanceof STFunction;
		} else {
			return false;
		}
	}

	/**
	 * Function:	getSymbol
	 * Purpose:		returns the symbol and its corresponding entry in the symbol table.
	 * @param symbol:
	 *            the symbol to get to the Symbol Table
	 */
	public STEntry getSymbol(Parser parser, String symbol){
		if (ST.containsKey(symbol)){
			return (STEntry) ST.get(symbol);
		} else {
			throw new DeclarationError("Attempted to access value of undeclared identifier", parser.scanner.currentToken);
		}

	}

	/**
	 * Function:	containsSymbol
	 * @param symbol	symbol to be evaluated
	 * @return			boolean stating the presence or absence of a symbol in the symbolTable
	 */
	public boolean containsSymbol(String symbol) {
		return ST.containsKey(symbol);
	}

	/**
	 * Function:	createSymbol
	 * Purpose:		creates and/or stores the symbol and its corresponding entry in the symbol table
	 * @param symbol the symbol to add to the Symbol Table
	 * @param entry the entry in the symbol table that corresponds to the symbol name
	 */
	public void createSymbol(Parser parser, String symbol, STEntry entry){
		if(this.containsSymbol(symbol)){
			ST.remove(symbol);
			ST.put(symbol, entry);
			//throw new DeclarationError("Attempted to redefine symbol");
		}else{
			ST.put(symbol, entry);
		}
	}
	
	/**
	 * Function:	deleteSymbol
	 * Purpose:		removes the symbol and its corresponding entry in the symbol table
	 * @param symbol the symbol to delete from the Symbol Table
	 */
	public void deleteSymbol(String symbol){
		if(ST.containsKey(symbol)){
			ST.remove(symbol);
		}else{
			// TODO: error: symbol not in table
		}
	}
	
	/**
	 * Function:	printSymbolTable
	 * Purpose:		prints entries in the symbolTable
	 */
	public void printSymbolTable() {
		System.out.println("******************** SymbolTable ********************");
		for (STEntry e : ST.values()) {
			if (e instanceof STIdentifier) {
				System.out.println(((STIdentifier) e).toString());
			} else if (e instanceof STFunction) {
				System.out.println(((STFunction) e).toString());
			} else if (e instanceof STControl) {
				System.out.println(((STControl) e).toString());
			} else {
				System.out.println(e);
			}
		}

		System.out.println("*****************************************************");
	}
}
