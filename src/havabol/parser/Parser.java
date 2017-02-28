package havabol.parser;

import java.io.IOException;

import havabol.lexer.*;
import havabol.storage.*;

public class Parser {
	
	Scanner scanner;
	SymbolTable symbolTable;

	public Parser(String sourceFilename, SymbolTable symbolTable) {
		
		this.symbolTable = symbolTable;
		
		try {
			scanner = new Scanner(sourceFilename, symbolTable);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void beginParsing() {
		while (!scanner.getNext().isEmpty()) {
			parseToken();
		}
	}
	
	public void parseToken() {
		if (scanner.currentToken.primClassif == Token.CONTROL) {
			if (scanner.currentToken.subClassif == Token.DECLARE) {
				parseDeclaration();
			}
		}else if(scanner.currentToken.primClassif == Token.OPERATOR){
			//parseOperator();
		}
	}
	
	public void parseDeclaration() {
		DataType declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
		Structure structure = Structure.PRIMITIVE;
		String identifier;
		
		if (!scanner.getNext().isEmpty()) {
			if (scanner.currentToken.primClassif == Token.OPERAND && scanner.currentToken.subClassif == Token.IDENTIFIER) {
				identifier = scanner.currentToken.tokenStr;
				
				// TODO: check scope of symbol table entries
				if (symbolTable.containsSymbol(identifier)) {
					// TODO: redeclared already existing identifier
				} else {
					// TODO: handle scope of entries
					symbolTable.createSymbol(identifier, new STIdentifier(identifier, Token.OPERAND, declaredType, structure, "", 0));
				}
				
				// Check for declaration initialization
				if (scanner.nextToken.primClassif == Token.OPERATOR && scanner.nextToken.tokenStr.equals("=")) {
					
					// Initialization assignment found
					scanner.getNext();
					if (!scanner.getNext().isEmpty()) {
						
						// Parse expr into result value
						ResultValue initValue = parseExpression();
						if (initValue.dataType != declaredType) {
							// TODO: type mismatch
						}
						
						symbolTable.getSymbol(identifier).setValue(initValue.value);
					} else {
						// TODO: expected initialization expr, found nothing
					}
				} else {
					// No init, declaration only
					// TODO: set default value of a non-initialized variable (somewhere, not necessarily here?)
				}
				
			} else {
				// TODO: expected identifier, found something else
			}
		} else {
			// TODO: expected identifier, found nothing
		}
		
	}
	
	public ResultValue parseExpression() {
		// TODO: recursively parse an expression
		return null;
	}

}
