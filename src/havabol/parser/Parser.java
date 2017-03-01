package havabol.parser;

import java.io.IOException;

import havabol.lexer.*;
import havabol.runtime.Execute;
import havabol.storage.*;

public class Parser {
	
	private Scanner scanner;
	private SymbolTable symbolTable;
	//private DataType exprDataType = null;

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
	
	private void parseToken() {
		if (scanner.currentToken.primClassif == Token.CONTROL) {
			if (scanner.currentToken.subClassif == Token.DECLARE) {
				parseDeclaration();
			}
		}else if(scanner.currentToken.primClassif == Token.OPERATOR){
			
		}
	}
	
	private void parseDeclaration() {
		DataType declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
		Structure structure = Structure.PRIMITIVE;
		String identifier;
		
		if (!scanner.getNext().isEmpty()) {
			if (scanner.currentToken.primClassif == Token.OPERAND && scanner.currentToken.subClassif == Token.IDENTIFIER) {
				identifier = scanner.currentToken.tokenStr;
				
				// Create symbol and checks for errors
				symbolTable.createSymbol(this, identifier, new STIdentifier(identifier, Token.OPERAND, declaredType, structure, "", 0));
				
				// Check for declaration initialization
				if (scanner.nextToken.primClassif == Token.OPERATOR && scanner.nextToken.tokenStr.equals("=")) {
					
					// Initialization assignment found
					scanner.getNext();
					if (!scanner.getNext().isEmpty()) {
						
						// Parse expr into result value
						ResultValue initValue = parseExpression(declaredType);
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
			// TODO: expected identifier, found eof
		}
		
	}
	
	private ResultValue parseAssignment() {
		// assignment := identifier '=' expr
		return null;
	}
	
	private ResultValue parseExpression(DataType dataType) {
		// expression := operand operator expr
		//             | operand
		
		if (!scanner.getNext().isEmpty()) {
			
			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				// There are 2 possibilities:
				// 1. An operator follows this operand
				// 2. This is the final operand
				
				ResultValue op1;
				
				if (scanner.currentToken.primClassif == Token.OPERAND) {
					op1 = ResultValue.tokenStrToResult(this, dataType, scanner.currentToken.tokenStr);
				} else {
					op1 = parseFunctionCall();
				}
				
				if (scanner.nextToken.primClassif == Token.OPERATOR) {
					// Option 1: we found an operator
					scanner.getNext();
					
					ResultValue op2 = parseExpression(dataType);
					
					switch (scanner.currentToken.tokenStr) {
					case "+":
						return Execute.add(dataType, op1, op2);
					case "-":
						return null;
					case "*":
						return null;
					case "/":
						return null;
					case "#":
						return null;
					case "^":
						return null;
					default:
						// TODO: found an unsupported operator
					}
					
				} else {
					// Option 2: that was the final operand
					// TODO: construct new ResultValue and return.
				}
				
			} else {
				// TODO: expected operand or function call, found nothing
			}
			
		} else {
			// TODO: expected operand as part of expression, found eof
		}
		
		return null;
	}
	
	private ResultValue parseFunctionCall() {
		// TODO: recursively parse a function
		return null;
	}

}
