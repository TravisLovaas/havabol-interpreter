package havabol.storage;

import java.util.ArrayList;
import java.util.List;

import havabol.error.DeclarationError;
import havabol.parser.Parser;

/**
 * Encompasses all symbol tables associated with an instance of a
 * Scanner/Parser.
 * 
 * Environment provides an interface to access symbols based on the current
 * state of the parser and the context, automatically managing scope of
 * multiple symbol tables.
 *
 */
public class Environment {
	
	private List<SymbolTable> environmentVectors = new ArrayList<>();
	
	public Environment() {
		
		SymbolTable globalTable = new SymbolTable();
		globalTable.initGlobal();
		
		environmentVectors.add(globalTable);
		
	}
	
	/**
	 * Adds a new environment vector onto the environment.
	 * 
	 * This has the effect of adding a new layer of "scope" where changes
	 * to the symbol table will take effect.
	 * 
	 * @param symbolTable New scoped symbol table
	 */
	public void pushVector(SymbolTable symbolTable) {
		environmentVectors.add(symbolTable);
	}
	
	/**
	 * Destroys the top most environment vector when the scope changes.
	 */
	public void destroyVector() {
		environmentVectors.remove(environmentVectors.size() - 1);
	}
	
	/**
	 * Returns true if the given symbol exists in the environment and is a
	 * function name.
	 * @param parser HavaBol calling parser
	 * @param symbol Symbol to check if a function
	 * @return true if the given symbol is a function
	 */
	public boolean isFunction(Parser parser, String symbol) {
		
		SymbolTable currentFrame;
		for (int i = environmentVectors.size() - 1; i >= 0; i--) {
			currentFrame = environmentVectors.get(i);
			
			if (currentFrame.containsSymbol(symbol)) {
				if (currentFrame.getSymbol(parser, symbol) instanceof STFunction) {
					return true;
				}
			}
			
		}
		
		return false;
	}

	/**
	 * Function:	getSymbol
	 * Purpose:		returns the symbol and its corresponding entry in the symbol table.
	 * @param symbol:
	 *            the symbol to get to the Symbol Table
	 */
	public STEntry getSymbol(Parser parser, String symbol) {
		
		SymbolTable currentFrame;
		for (int i = environmentVectors.size() - 1; i >= 0; i--) {
			currentFrame = environmentVectors.get(i);
			
			if (currentFrame.containsSymbol(symbol)) {
				return currentFrame.getSymbol(parser, symbol);
			}
			
		}
		
		throw new DeclarationError("Attempted to access value of undeclared identifier", parser.scanner.currentToken);

	}

	/**
	 * Function:	containsSymbol
	 * @param symbol	symbol to be evaluated
	 * @return			boolean stating the presence or absence of a symbol in the symbolTable
	 */
	public boolean containsSymbol(String symbol) {
		
		SymbolTable currentFrame;
		for (int i = environmentVectors.size() - 1; i >= 0; i--) {
			currentFrame = environmentVectors.get(i);
			
			if (currentFrame.containsSymbol(symbol)) {
				return true;
			}
			
		}
		
		return false;
	}

	/**
	 * Function:	createSymbol
	 * Purpose:		creates and/or stores the symbol and its corresponding entry in the symbol table
	 * @param symbol the symbol to add to the Symbol Table
	 * @param entry the entry in the symbol table that corresponds to the symbol name
	 */
	public void createSymbol(Parser parser, String symbol, STEntry entry) {
		SymbolTable currentScope = environmentVectors.get(environmentVectors.size() - 1);
		currentScope.createSymbol(parser, symbol, entry);
	}
	
	/**
	 * Function:	deleteSymbol
	 * Purpose:		removes the symbol and its corresponding entry in the symbol table
	 * @param symbol the symbol to delete from the Symbol Table
	 */
	public void deleteSymbol(String symbol){
		SymbolTable currentScope = environmentVectors.get(environmentVectors.size() - 1);
		currentScope.deleteSymbol(symbol);
	}

}
