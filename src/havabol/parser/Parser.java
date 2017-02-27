package havabol.parser;

import java.io.IOException;

import havabol.lexer.*;
import havabol.storage.*;

public class Parser {
	
	Scanner scanner;

	public Parser(String sourceFilename) {
		
		SymbolTable symbolTable = new SymbolTable();
		
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
		}
	}
	
	public void parseDeclaration() {
		String declaredType = scanner.currentToken.tokenStr;
		String identifier;
		
		if (!scanner.getNext().isEmpty()) {
			if (scanner.currentToken.primClassif == Token.OPERAND && scanner.currentToken.subClassif == Token.IDENTIFIER) {
				identifier = scanner.currentToken.tokenStr;
				
				// Check for declaration initialization
				if (scanner.nextToken.primClassif == Token.OPERATOR && scanner.nextToken.tokenStr.equals("=")) {
					// Initialization assignment found
					scanner.getNext();
					if (!scanner.getNext().isEmpty()) {
						// Parse expr into result value
						
					} else {
						// expected initialization expr, found nothing
					}
				}
				
			} else {
				// expected identifier, found something else
			}
		} else {
			// expected identifier, found nothing
		}
		
	}
	
	public ResultValue parseExpression() {
		return null;
	}

}
