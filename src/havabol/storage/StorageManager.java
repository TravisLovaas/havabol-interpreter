package havabol.storage;

import havabol.lexer.*;
import havabol.parser.*;

public class StorageManager {
	
	Parser parser;
	Scanner scanner;
	SymbolTable symbolTable;
	
	DataType declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
	Structure structure = Structure.PRIMITIVE;
	
	public StorageManager(){
		
	}
	
	public boolean update(String identifier, String value){
		if(symbolTable.ST.containsKey(identifier)){
			//update symbol with new value
			if(scanner.currentToken.tokenStr != identifier){
				symbolTable.putSymbol(identifier, new STIdentifier(value, Token.OPERAND, declaredType, structure, "", 0));
				return true;
			}else{
				// TODO: error: incorrect identifier
				return false;
			}
		}else{
			// TODO: error: cannot update without creation
			return false;
		}
	}
	
	public boolean create(String identifier, String value){
		//if there's no such key, create it.
		if(!symbolTable.ST.containsKey(identifier)){
			symbolTable.putSymbol(identifier, new STIdentifier(value, Token.OPERAND, declaredType, structure, "", 0));
			return true;
		}else{
			// TODO: error: key already in table
			return false;
		}
	}
	
	public boolean delete(String identifier){
		if(!symbolTable.ST.containsKey(identifier)){
			// TODO: error: key doesn't exist
			return false;
		}else{
			symbolTable.deleteSymbol(identifier);
			return true;
		}
	}
}
