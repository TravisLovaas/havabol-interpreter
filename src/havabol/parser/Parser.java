package havabol.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import havabol.error.*;
import havabol.lexer.*;
import havabol.runtime.*;
import havabol.storage.*;

public class Parser {
	
	private Scanner scanner;
	private SymbolTable symbolTable;
	
	public boolean debugExpr = false;
	public boolean debugAssignment = false;
	
	//precedence values while operator tokens are outside of stack
	private final static HashMap<String, Integer> precedence = new HashMap<String, Integer>(){
		private static final long serialVersionUID = 1L;

	{
		put("and", 3); put("or", 3); put("not", 4); put("in", 5); put("notin", 5);    
		put("<", 5); put(">", 5); put("<=", 5); put(">=", 5); put("==", 5); put("!=", 5);
		put("#", 6);  put("+", 7); put("-", 7); put("*", 8); put("/", 8); put("^", 10); put("u-", 11);
		put("(", 15); put("[", 16);
	}};
	
	//precedence values while tokens are on the stack
	private final static HashMap<String, Integer> stkPrecedence = new HashMap<String, Integer>(){
		private static final long serialVersionUID = 1L;

	{
		put("and", 3); put("or", 3); put("not", 4); put("in", 5); put("notin", 5);    
		put("<", 5); put(">", 5); put("<=", 5); put(">=", 5); put("==", 5); put("!=", 5);
		put("#", 6);  put("+", 7); put("-", 7); put("*", 8); put("/", 8); put("^", 9); put("u-", 11);
		put("(", 2); put("[", 0);
	}};
	
	/***
	 * Function/Constructor: Parser
	 * @param sourceFilename
	 * @param symbolTable
	 */
	public Parser(String sourceFilename, SymbolTable symbolTable) {
		
		this.symbolTable = symbolTable;
		
		try {
			scanner = new Scanner(sourceFilename, symbolTable);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/***
	 * Function: beginParsing()
	 * Purpose: starts the process of parsing tokens based on their
	 * 			classification in parseStatement()
	 */
	public void beginParsing() {
		scanner.getNext();
		while (scanner.currentToken.primClassif != Token.EOF) {
			parseStatement();
		}
	}
	
	/**
	 * Function: parseIf 
	 * Purpose: to call parseExpression and to get the conditional path to take.
	 * 			In the if-then-else it will call parseStatement until else or endif
	 * 			to correctly execute desired statements.
	 * @param bExec
	 */
	
	private void parseIf() {
		scanner.getNext();
		
		// currentToken should be beginning of conditional expression
		Value resCond = parseExpression(":");
		if (!scanner.currentToken.tokenStr.equals(":")){
			throw new SyntaxError("Expected ':' after conditional expression in if", scanner.currentToken);
		}
		
		scanner.getNext(); // advance past :
		
		if (resCond.asBoolean(this).booleanValue){
			// Parse statements if conditional expression is true
			for (;;) {
				parseStatement();
				if (scanner.currentToken.tokenStr.equals("else")) {
					break;
				} else if (scanner.currentToken.tokenStr.equals("endif")) {
					scanner.getNext();
					return; // currentToken should be ;, handled by parseStatement
				}
			}
		
			// skip everything inside else
			int ifCnt = 0;
			
			while (!scanner.currentToken.tokenStr.equals("endif") || ifCnt > 0) {
				if (scanner.currentToken.tokenStr.equals("if")) {
					ifCnt++;
				}
				if (scanner.currentToken.tokenStr.equals("endif")) {
					ifCnt--;
				}
				scanner.getNext();
			}
			scanner.getNext();
			// done, semi-colon handled by parseStatement
		} else {
			// Skip everything until else or endif
			int ifCnt = 0;
			for (;;) {
				scanner.getNext();
				if (scanner.currentToken.tokenStr.equals("if")) {
					ifCnt++;
				}
				if (scanner.currentToken.tokenStr.equals("else")) {
					if (ifCnt == 0) {
						scanner.getNext(); // pass "else"
						scanner.getNext(); // pass ":"
						break;
					}
				} else if (scanner.currentToken.tokenStr.equals("endif")) {
					if (ifCnt == 0) {
						scanner.getNext();
						return; // currentToken should be ;, handled by parseStatement
					} else 
						ifCnt--;
				}
			}		
			// skip everything inside else
			while (!scanner.currentToken.tokenStr.equals("endif")) {
				parseStatement();
			}
			scanner.getNext(); // pass "endif"
		}
	}
	
	/**
	 * Function: parseWhile
	 * Purpose: to evaluate the expression given to while statement and 
	 * parseStatement will evaluate all the statements within the while loop
	 * until the condition value is no longer true
	 */
	
	private void parseWhile() {
		
		int loopSrcLine = scanner.iSourceLineNr;
		int loopColPos = scanner.iColPos;
		
		// ASSUME currentToken is "while" on call
		scanner.getNext(); // advance past "while"
		
		// Save our position to loop back!!
		
		Value whileCond;		
		for (;;) {
			// Evaluate while condition
			whileCond = parseExpression(":");
			
			// should be on ":"
			if (scanner.currentToken.tokenStr.equals(":")) {
				scanner.getNext(); // advance past ":"
			} else {
				throw new SyntaxError("Expected : after while conditional expression", scanner.currentToken);
			}
			
			if (whileCond.asBoolean(this).booleanValue) {
				// Evaluated to true, execute loop
				while (!scanner.currentToken.tokenStr.equals("endwhile")) {
					parseStatement();
				}
				
				scanner.getNext(); // pass "endwhile"
				scanner.getNext(); // pass ";"
								
				// Done executing loop body, let's loop back!
				scanner.setPosition(loopSrcLine, loopColPos);	
			} else {
				// Evaluated to false, skip loop past endwhile and return				
				int whileCnt = 0;
				while (!scanner.currentToken.tokenStr.equals("endwhile") || whileCnt > 0) {
					if (scanner.currentToken.tokenStr.equals("while")) {
						whileCnt++;
					} else if (scanner.currentToken.tokenStr.equals("endwhile")) {
						whileCnt--;
					}
					scanner.getNext();
				}				
				scanner.getNext(); // pass "endwhile"
				return; // return to parseStatement, expects ;
			}	
		}
	}
	
	/***
	 * Function: parseFor
	 * Preconditions:
	 * 		currentToken is on "for"
	 * Purpose:
	 * 		purpose is to loop statements until requirements are
	 * 		no longer met.
	 */
	/**
	 * Possible for loops:
	 * for 1 to 10 :
	 * for i = 1 to 15 by 3 :
	 * for x = "t" in "test" :
	 */
//	private void parseFor() {
//		
//		scanner.getNext(); // get past "for"
//		
//		Value cv = parseAssignment();					// grabs the control variable...
//		
//		
//		if (scanner.currentToken.tokenStr.equals("to")) {		// if token is a to...
//			scanner.getNext();
//			Value limit = parseAssignment();				// grabs the limit variable...
//			
//			if (scanner.currentToken.tokenStr.equals(":")) {		// if end of for statement is reached...
//				int loopSrcLine = scanner.iSourceLineNr;			// remembers start of body statements...
//				int loopColPos = scanner.iColPos;
//				scanner.getNext();
//				
//				while(cv.intValue <= limit.intValue){				// loop until limit is reached...
//					while(!scanner.currentToken.tokenStr.equals("endfor")){
//						parseStatement();
//					}
//					cv.intValue++;
//					if(cv.intValue <= limit.intValue){
//						scanner.setPosition(loopSrcLine, loopColPos);	// reset position...
//					}
//				}
//				scanner.getNext();
//			} 
//			else if (scanner.currentToken.tokenStr.equals("by")) {	// if token is by...
//				scanner.getNext();
//				Value incr = parseAssignment();				// grabs the increment variable...
//				
//				if (scanner.currentToken.tokenStr.equals(":")) {	// if end of for statement is reached...
//					int loopSrcLine = scanner.iSourceLineNr;
//					int loopColPos = scanner.iColPos;
//					scanner.getNext();
//					while(cv.intValue <= limit.intValue){
//						while(!scanner.currentToken.tokenStr.equals("endfor")){
//							parseStatement();
//						}
//						cv.intValue = cv.intValue+incr.intValue;
//						if(cv.intValue <= limit.intValue){
//							scanner.setPosition(loopSrcLine, loopColPos);
//						}
//					}
//				}
//				else{
//					throw new SyntaxError("Expected colon to end statement", scanner.currentToken);
//				}
//				scanner.getNext();
//			} 
//			else{												// else, colon expected here...
//				throw new SyntaxError("Expected colon to end statement", scanner.currentToken);
//			}
//		}
//		else if (scanner.currentToken.tokenStr.equals("in")) {	// if token is an in...
//			scanner.getNext();
//			Value container = parseAssignment();				// grabs the string container...
//			
//			if (scanner.currentToken.tokenStr.equals(":")) {		// if end of for statement is reached...
//				int loopSrcLine = scanner.iSourceLineNr;
//				int loopColPos = scanner.iColPos;
//				scanner.getNext();
//				int i = 1;
//				while(i <= container.strValue.length()){			// loops through string...
//					while(!scanner.currentToken.tokenStr.equals("endfor")){
//						parseStatement();
//					}
//					i++;
//					if(i <= container.strValue.length()){
//						scanner.setPosition(loopSrcLine, loopColPos);
//					}
//				}
//			}
//			else{
//				throw new SyntaxError("Expected colon to end statement", scanner.currentToken);
//			}
//			scanner.getNext();
//		}
//		else{
//			throw new SyntaxError("Expected 'to' or 'in' next", scanner.currentToken);
//		}
//		
//	}
	
	private void parseFor(){
		
		scanner.getNext(); // get past for
		

		assert(scanner.currentToken.subClassif == Token.IDENTIFIER);
		
		String cv = scanner.getNext();
		Value value, limit = null, incr = new Value(1);
		STIdentifier controlVariable = null;
		
		
		scanner.getNext();
		
		
		switch(scanner.currentToken.tokenStr){
		case "=":
			scanner.getNext();
			value = parseExpression("to"); 
			DataType dt = value.dataType;
			controlVariable = new STIdentifier(cv, dt, Structure.PRIMITIVE, null, 0);
			controlVariable.setValue(value);
			symbolTable.createSymbol(this, cv, controlVariable);
			
			assert(scanner.currentToken.tokenStr.equals("to"));
			scanner.getNext(); // get past "to"
			limit = parseExpression("by");
			//scanner.getNext();
			switch(scanner.currentToken.tokenStr){
			case "by":
				scanner.getNext();
				incr = parseExpression(":");
				break;
			case ":":
				break;
			default:
				//TODO: throw exception
			}
			System.out.println("");
			
			break;
		case "in":
			scanner.getNext();
			Token token = scanner.currentToken;
			if(!symbolTable.containsSymbol(token.tokenStr)){
				// TODO: exception
			}
			STIdentifier array = (STIdentifier) symbolTable.getSymbol(token.tokenStr);
			if(!(array.structure == Structure.FIXED_ARRAY || array.structure == Structure.UNBOUNDED_ARRAY)){
				// TODO: exception
			}
			
			//if(controlVariable.setValue(array.fetch(this, 0))){
				
			//}
			
			
			break;
		default:
			// TODO: throw exception
		}
		System.out.println("CV: "+cv+" value: "+controlVariable.getValue()+" limit: "+limit+" incr: "+incr);
	}
	
	
	
	
	
	
	
	/**
	 * Function: parseDebugStatement
	 * Preconditions:
	 *  - currentToken is beginning of debug statement, e.g.
	 *  	debug token on;
	 *      ^^^^^
	 * Purpose: to set up printing dimensions for attributes
	 * 			that need debugging
	 */
	private void parseDebugStatement() {
		
		assert scanner.currentToken.tokenStr.equals("debug");
		
		String debugArg = scanner.getNext();
		String toggleStr = scanner.getNext().toLowerCase(); // on or off
		boolean desiredState = false;
		
		if (toggleStr.equals("on")) {
			desiredState = true;
		} else if (toggleStr.equals("off")) {
			desiredState = false;
		} else {
			throw new SyntaxError("Expected `on` or `off` in debug statement", scanner.currentToken);
		}
		
		switch (debugArg.toLowerCase()){
		case "assign":
		case "assignment":
			
			debugAssignment = desiredState;
			
			break;
		case "expr":
		case "expression":
			
			debugExpr = desiredState;
			
			break;
		case "token":
			
			scanner.debugToken = desiredState;
			
			break;
		default:
			throw new SyntaxError("Found unsupported \"debug\" argument", scanner.currentToken);
		}
		
	}
	
	/**
	 * Function: parseStatement
	 * Preconditions: 
	 *  - currentToken is the first token in a statement, e.g.
	 *  			if myVar == 0:
	 *  			^^
	 * Postconditions:
	 *  - currentToken is the first token in a statement and directly follows a semicolon, e.g.
	 *  			i = 0;
	 *  			myVar = 1;
	 *              ^^^^^
	 * Purpose: Parses a statement, ending with a semicolon.
	 */
	private void parseStatement() {
		/* Possible types of statements:
		 * 
		 * 	Int i = 0;       		declaration
		 *  i = 1;           		assignment
		 *  if ... endif;    		if
		 *  while ... endwhile;		while
		 *  for ... endfor;			for
		 * 	debug type setting;		debug
		 * 	print( ... );			function call
		 * 
		 */
		if (scanner.currentToken.subClassif == Token.IDENTIFIER) {
			if (scanner.currentToken.tokenStr.equals("debug")) {
				parseDebugStatement();
			}
		}
		
		if (scanner.currentToken.primClassif == Token.CONTROL) {
			if (scanner.currentToken.subClassif == Token.DECLARE) {
				parseDeclaration();
			} else if (scanner.currentToken.subClassif == Token.FLOW) {
				switch (scanner.currentToken.tokenStr) {
				case "if":
					parseIf();
					break;
				case "while":
					parseWhile();
					break;
				case "for":
					parseFor();	// Exceptions handled in parseFor function
					return;
				default:
					throw new UnsupportedOperationError("Unsupported FLOW token found.");
				}
			} //else if (scanner.currentToken.tokenStr.equals("endif")) {
				//scanner.getNext();
			 else {
				throw new SyntaxError("Unexpected control token found: " + scanner.currentToken.tokenStr, scanner.currentToken);
			}
		} else if (scanner.currentToken.primClassif == Token.FUNCTION) {
			parseFunctionCall();
		} else if (scanner.currentToken.primClassif == Token.OPERAND) {
			if (scanner.currentToken.subClassif == Token.IDENTIFIER) {
					parseAssignment();
			} else {
				throw new UnsupportedOperationError("Left value must be identifier.");
			}
		} else if (scanner.currentToken.tokenStr.equals(";")) {
			scanner.getNext();
			return;
		} else {
			throw new UnsupportedOperationError("Unexpected token '" + scanner.currentToken.tokenStr + "' found while parsing statements.");
		}
		
		if (scanner.currentToken.tokenStr.isEmpty())
			return;
		
		if (scanner.currentToken.tokenStr.equals("in")) {
			return;
		}
		
		if (scanner.currentToken.tokenStr.equals(";")) {
			scanner.getNext();
		} else {
			throw new SyntaxError("Expected semi-colon to end statement", scanner.currentToken);
		}
	}
	
	/**
	 * Function: parseDeclaration
	 * Parses a declaration statement.
	 * Preconditions:
	 *    - currentToken is a data type control token
	 * Examples of declaration statements:
	 *    Int i = 0;             // primitive = 0
	 *    Int j;                 // primitive with no value
	 *    Int arr[10];           // fixed array size 10 [0 ...]
	 *    Int arr[unbound];      // unbounded array
	 *    Int arr[] = 1, 2, 3;   // fixed array size 3 [1, 2, 3]
	 *    Int arr[10] = 1, 2, 3; // fixed array size 10 [1, 2, 3, 0 ...]
	 * Purpose: to accurately declare tokens based on 
	 * 			their driving data types .i.e for
	 * 			primitives, homogenous/heterogenous arrays
	 */
	private void parseDeclaration() {
		// Parse declared type
		DataType declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
		String identifier;
		
		scanner.getNext(); // currentToken should be identifier
		
		// Current token should be name of variable
		if (scanner.currentToken.subClassif == Token.IDENTIFIER) {
			identifier = scanner.currentToken.tokenStr;
		} else {
			throw new DeclarationError("Expected an identifier", scanner.nextToken);
		}
		
		STIdentifier variable = null; // symbol table entry for this variable
		Value rhsExpr; // right hand side of assignment if it exists
		MultiValue rhsMultiVal;
		
		// Next token is either "=" (primitive) or "[" (array)
		switch (scanner.getNext()) {
			case "=": // expecting a primitive value
				
				variable = new STIdentifier(identifier, declaredType, Structure.PRIMITIVE, "", 0);
				
				// Get value of right hand side
				scanner.getNext();
				rhsExpr = parseExpression(";");
				
				if (rhsExpr.structure != Structure.PRIMITIVE) {
					throw new TypeError("Right hand side of assignment must be a primitive value if variable is primitive", scanner.currentToken);
				}
				
				// Check and cast (if necessary) type and store value
				rhsExpr = rhsExpr.asType(this, variable.declaredType);
				variable.setValue(rhsExpr);
				
				break;
			case "[":
				// Array is being declared
				switch (scanner.getNext()) {
//					case "unbound":
//						variable = new STIdentifier(identifier, declaredType, Structure.UNBOUNDED_ARRAY, "", 0);
//						rhsExpr = parseValueList(";");
//						variable.setValue(rhsExpr);
//						break;
					case "]": // Array size depends on the length of given value list
						variable = new STIdentifier(identifier, declaredType, Structure.FIXED_ARRAY, "", 0);

						scanner.getNext(); // pass "]". Int array[] = 1, 2, 3;
						scanner.getNext(); // pass =
						
						rhsMultiVal = parseValueList(";");
						variable.arrayValue = new Value[rhsMultiVal.numItems];
						for (int i = 0; i < rhsMultiVal.size; i++) {
							variable.arrayValue[i] = rhsMultiVal.values.get(i);
						}
						variable.declaredSize = rhsMultiVal.size;
						variable.structure = Structure.FIXED_ARRAY;
						break;
					default:
						// Array size might be an expression
						Value sizeValue = parseExpression("]");						
						int declaredSize = sizeValue.asInteger(this).intValue;
						
						variable = new STIdentifier(identifier, declaredType, Structure.FIXED_ARRAY, "", 0);
						
						variable.arrayValue = new Value[declaredSize];
						variable.declaredSize = declaredSize;

						if (!scanner.currentToken.tokenStr.equals("]")) {
							throw new SyntaxError("Expected a closing parenthesis ']'");
						}
						
						// currentToken is now ]
						// Int array[10];
						//             ^
						// Int array[10] = 1, 2, 3;
						//             ^
						
						switch (scanner.getNext()) {
						case ";": 
							// Size given with no value list
							// Int array[10];
							// done
							break;
						case "=":
							// Size given with value list
							// Int array[10] = 1, 2, 3;
							scanner.getNext(); // advance past =
							//could be assigned
							//scanner.getNext();
							rhsMultiVal = parseValueList(";");
							
							System.out.println("rhsMultiVal.numItems = " + rhsMultiVal.numItems);
							if (rhsMultiVal.numItems > variable.declaredSize) {
								throw new IndexError("Value list contains too many elements to fit into given array");
							}
							
							for (int i = 0; i < rhsMultiVal.size; i++) {
								variable.arrayValue[i] = rhsMultiVal.values.get(i);
							}
							
							System.out.println("Created array with given size and given value list.");
							
							break;
						default:
							throw new SyntaxError("Expected = or ; after array declaration", scanner.currentToken);
						}
						break;
				}
				break;
				case ";": // no initialization clause
					variable = new STIdentifier(identifier, declaredType, Structure.PRIMITIVE, "", 0);
					break;
				default:
					throw new SyntaxError("Expected an assignment '=', array type specifier, or semicolon in declaration", scanner.currentToken);
		}
		System.out.println("Added symbol: " + identifier);
		symbolTable.createSymbol(this, identifier, variable);
	}
	
	/**
	 * Parses a list of literal values for an array initialization or assignment
	 * Preconditions:
	 * 		currentToken is the beginning of a value list:
	 * 		String fruit[] = "apple", "pear", "orange";
	 *                       ^^^^^^^
	 * @param terminatingStr the token string that says to stop parsing values
	 * @return a ResultValue representing a value list
	 */
	private MultiValue parseValueList(String terminatingStr) {
		
		MultiValue array = new MultiValue();
		array.structure = Structure.MULTIVALUE;
		array.numItems = 0;
		
		// Parse first element and set data type to that of first elem
		Value elem = scanner.currentToken.toResult();
		array.add(this, elem);
		array.dataType = elem.dataType;
		
		// next token must be either ";" or ","
		switch (scanner.getNext()) {
		case ",":
			break;
		case ";":
			return array;
		default:
			throw new SyntaxError("Expected , or ; after element in value list", scanner.currentToken);
		}
		
		assert(scanner.currentToken.tokenStr.equals(","));
		
		scanner.getNext(); // pass ","
		
		while (!scanner.currentToken.tokenStr.equals(terminatingStr)) {			
			elem = scanner.currentToken.toResult();
			elem = elem.asType(this, array.dataType);
			array.add(this, elem);
			
			scanner.getNext(); // currentToken should now be comma or terminatingStr
			
			switch (scanner.currentToken.tokenStr) {
			case ",":
				scanner.getNext();
				continue;
			case ";":
				break;
			default:
				throw new SyntaxError("Expected , or ; after element in value list", scanner.currentToken);
			}			
		}
		
		assert(scanner.currentToken.tokenStr.equals(";"));
		
		System.out.println("Value list: " + array);
		
		return array;
	}
	
	/**
	 * Function: parseAssignment
	 * Preconditions:
	 *    - currentToken is an identifier
	 * Purpose:
	 * 		to parseAssignment's that are called for all lines
	 * 		of an assignment within parseExpression
	 * @return the evaluated value of the assignment
	 */
	private Value parseAssignment() {
		// assignment := identifier '=' expr
		boolean bfor = false;
		boolean bin = false;
		boolean bto = false;
		boolean bby = false;
		
		switch(scanner.previous.tokenStr){
			case "for":
				bfor = true;
				break;
			case "in":
				bin = true;
				break;
			case "to":
				bto = true;
				break;
			case "by":
				bby = true;
				break;
		}
		
		if (scanner.currentToken.subClassif != Token.IDENTIFIER && !bfor && !bto && !bby) {
			throw new SyntaxError("Expected an identifier to begin assignment", scanner.currentToken);
		}
		
		String identifier = scanner.currentToken.tokenStr;
		
		if(bfor || bto || bby){
			// for single integer control variables
			if(scanner.currentToken.subClassif == Token.INTEGER){
				Value rv = scanner.currentToken.toResult();
				scanner.getNext();
				return rv;
			} // for everything with an assignment
			else{
				Token token = scanner.currentToken;
				scanner.getNext();
				if(scanner.currentToken.tokenStr.equals("=")){
					scanner.getNext();
					Value rv = parseExpression(";");
					return rv;
				}
				else{
					scanner.getNext();
					if(scanner.currentToken.tokenStr.equals("to") || scanner.currentToken.tokenStr.equals("in")){
						// check if token is in symbolTable
						if(symbolTable.containsSymbol(token.tokenStr)){
							System.out.println("Found token: "+token.tokenStr);
							return token.toResult();
						}
					}
				}
			}
		}
		
		// Ensure identifier has been declared
		STIdentifier variable = (STIdentifier) symbolTable.getSymbol(identifier);
		
		if (variable == null && !bfor) {
			throw new DeclarationError("Reference to undeclared identifier found", scanner.currentToken);
		}
		
		Value res01;
		Value res02, rhsExpr = null;
		
		String token = scanner.getNext();
		
		// Primitive assignment
		if (token.contains("=")) {

			// Next token should be an assignment operator
			switch (token) {
				case "=":
					scanner.getNext();
					res02 = parseExpression(";");
					// Ensure type of rhsExpr matches declared type, or can be 	cast to such.
					rhsExpr = res02.asType(this, variable.declaredType); // Parse expression on right-hand side of assignment
					variable.setValue(rhsExpr);
					break;
				case "+=":
					scanner.getNext();
					res02 = parseExpression(";");
					res01 = symbolTable.getSymbol(identifier).getValue();
					//run the subtract, Operators should figure out if it is valid
					rhsExpr = Operators.subtract(this, res01, res02);
					variable.setValue(rhsExpr);
					break;
				case "-=":
					scanner.getNext();
					res02 = parseExpression(";");
					res01 = symbolTable.getSymbol(identifier).getValue();
					//run the subtract, Operators should figure out if it is valid
					rhsExpr = Operators.add(this, res01, res02);
					variable.setValue(rhsExpr);
					break;
				case "*=":
					scanner.getNext();
					res02 = parseExpression(";");
					res01 = symbolTable.getSymbol(identifier).getValue();
					//run the subtract, Operators should figure out if it is valid
	
					rhsExpr = Operators.multiply(this, res01, res02);
					variable.setValue(rhsExpr);
					break;
				case "/=":
					scanner.getNext();
					res02 = parseExpression(";");
					res01 = symbolTable.getSymbol(identifier).getValue();
					//run the subtract, Operators should figure out if it is valid
					rhsExpr = Operators.divide(this, res01, res02);
					variable.setValue(rhsExpr);
					break;
				default:
					if(bin){
						rhsExpr = symbolTable.getSymbol(identifier).getValue();
						break;
					}
					throw new SyntaxError("Expected assignment operator as part of assignment", scanner.nextToken);
			}
		
		} else if (token.equals("[")) {
			// Array
			scanner.getNext(); // get array index for assignment
			
			int assignmentIndex = parseExpression("]").asInteger(this).intValue;
			
			// next token should be "]"
//			if (!scanner.getNext().equals("]")) {
//				throw new SyntaxError("Expected closing bracket after index in assignment", scanner.currentToken);
//			}
			
			assert(scanner.currentToken.tokenStr.equals("]"));
			
			// next token should be "="
			if (!scanner.getNext().equals("=")) {
				throw new SyntaxError("Expected assignment operator after array reference in assignment", scanner.currentToken);
			}
			
			// Assign value
			scanner.getNext();
			
			rhsExpr = parseExpression(";");
			
			variable.set(this, assignmentIndex, rhsExpr);
			
			System.out.println("Assigned " + rhsExpr + " to index " + assignmentIndex + " of " + variable.symbol);
			
		} else {
			System.out.println(scanner.currentToken.tokenStr);
			throw new SyntaxError("Expected array reference or assignment operator", scanner.currentToken);
		}
		
		if (debugAssignment) {
			System.out.println("\t\t... Assignment variable = " + identifier);
			System.out.println("\t\t... Assignment value = " + rhsExpr);
		}

		// Ensure type of rhsExpr matches declared type, or can be cast to such.
		return rhsExpr;
	}	
	
	/**
	 * Function: parseArrayRef
	 * Parses a reference to an array value or slice
	 * Preconditions:
	 * 	- currentToken is an array type identifier, e.g.
	 * 		j = tArray[2~4];
	 *          ^^^^^^		
	 * @return a ResultValue with the values of the referenced array
	 */
	private Token parseArrayRef() {
		
		if (scanner.currentToken.subClassif != Token.IDENTIFIER) {
			throw new SyntaxError("Expected identifer at beginning of array reference", scanner.currentToken);
		}
		
		String arrayName = scanner.currentToken.tokenStr;
		
		STIdentifier array = (STIdentifier) symbolTable.getSymbol(arrayName);

		if (array.structure != Structure.FIXED_ARRAY && array.structure != Structure.UNBOUNDED_ARRAY) {
			throw new TypeError("Expected an array type but found " + array.structure, scanner.currentToken);
		}
		
		if (!scanner.getNext().equals("[")) {
			throw new SyntaxError("Expected [ following array reference in expression", scanner.currentToken);
		}
		
		scanner.getNext();
		
		// currentToken may be an integer or slice operator:
		//   tArray[2~4]
		//   tArray[~4]
		//   tArray[2~]
		//   tArray[2]
		//System.out.println("something = " + scanner.currentToken.subClassif);
		
		/*if (!scanner.currentToken.tokenStr.equals("~") && (scanner.currentToken.subClassif != Token.INTEGER)){
			throw new SyntaxError("Expected index or beginning of slice", scanner.currentToken);
		}*/
		
		// index may be an expression
		int beginSliceIndex = parseExpression("]").asInteger(this).intValue;
		Value result = null;
		
		assert(scanner.currentToken.tokenStr.equals("]"));
		
		switch (scanner.currentToken.tokenStr) {
		case "]":
			// Singular array value
			result = array.fetch(this, beginSliceIndex);
			
			System.out.println("Accessing array " + array.symbol + " index " + beginSliceIndex + " value = " + result);
			
			break;
		case "~":
			// Slice
			
			throw new UnsupportedOperationError("Array slicing not supported");
			
//			int endSliceIndex = 0;
//			
//			scanner.getNext();
//			if (scanner.currentToken.tokenStr.equals("]")) {
//				endSliceIndex = array.getValue().numItems;
//			} else if (scanner.currentToken.subClassif == Token.INTEGER) {
//				endSliceIndex = scanner.currentToken.toResult().asInteger(this).intValue;
//			} else {
//				throw new SyntaxError("Expected index to end slice", scanner.currentToken);
//			}
//			
//			// TODO: check and fetch multivalue
//			
//			if (!scanner.getNext().equals("]")) {
//				throw new SyntaxError("Expected ] to end array slice", scanner.currentToken);
//			}
//			
//			break;
		default:
			throw new SyntaxError("Expected ] or slice operator ~", scanner.currentToken);
		}
		
		
		
		return result.toToken(this);
	}

	/**
	 * Function: parseFunctionCall
	 * Preconditions:
	 *    - currentToken is the name of a function in a function call, e.g.
	 *           print("hello", "world");
	 *      	 ^^^^^
	 * Purpose: Parses a function call
	 * @return the result of function call
	 */
	private Token parseFunctionCall() {
		
		if (scanner.currentToken.primClassif != Token.FUNCTION) {
			throw new SyntaxError("Expected name of a function", scanner.currentToken);
		}
		
		String calledFunction = scanner.currentToken.tokenStr;
		String argVar = null;
		System.out.println("Called: " + calledFunction); 
		// currentToken should be open paren "("
		String check = scanner.getNext();
		
		assert(scanner.currentToken.tokenStr.equals("("));
		
		if (!scanner.currentToken.tokenStr.equals("(")) {
			throw new SyntaxError("Expected left parenthesis after function name", scanner.currentToken);
		}
		
		scanner.getNext(); // currentToken is beginning of first arg expression or )
		
		Value retVal = null;
		
		switch (calledFunction) {
		case "print":
			List<Value> args = new ArrayList<>();
			
			if (scanner.currentToken.tokenStr.equals(")")) {
				
			} else {
				
				// Parse all function arguments
				for (;;) {
					
					Value arg = parseExpression(")");
			
					args.add(arg);
					
					if (scanner.currentToken.tokenStr.equals(",")) {
						scanner.getNext();
						continue;
					} else if (scanner.currentToken.tokenStr.equals(")")) {
						break;
					} else {
						throw new SyntaxError("Expected , or ) in function call", scanner.currentToken);
					}
					
				}
			}
			Functions.print(this, args);
			break;
		case "ELEM":
			argVar = scanner.currentToken.tokenStr;
			retVal = Functions.elem(this, (STIdentifier) symbolTable.getSymbol(argVar));
			scanner.getNext();
			break;
		case "MAXELEM":
			argVar = scanner.currentToken.tokenStr;
			retVal = Functions.maxElem(this, (STIdentifier) symbolTable.getSymbol(argVar));
			break;
		case "LENGTH":
			argVar = scanner.currentToken.tokenStr;
			retVal = Functions.length(this, (STIdentifier) symbolTable.getSymbol(argVar));
			break;
		case "SPACES":
			argVar = scanner.currentToken.tokenStr;
			retVal = Functions.spaces(this, (STIdentifier) symbolTable.getSymbol(argVar));
			scanner.getNext();
			break;
		default:
			throw new DeclarationError("Attempted to call undefined function " + calledFunction);
		}
		
		System.out.println("YELL =" + scanner.currentToken.tokenStr);
		assert(scanner.currentToken.tokenStr.equals(")"));
		
		scanner.getNext();
		
		if (retVal == null) {
			return null;
		} else {
			return retVal.toToken(this);
		}
	}
	
	/**
	 * Function: ParseExpression
	 * Preconditions: parseExpression is called on the first
	 * 				  token of a potential infix expression
	 * Purpose: to create a postFix expression from an input 
	 *			infix expression and evaluate the expression
	 *			correctly depending on its operator.
	 * @return the evaluated value of an expression
	 */
	private Value parseExpression(String terminatingStr) throws SyntaxError{
		//System.out.println("In parse");
		ArrayList <Token> out = new ArrayList<Token>();
		Stack<Token> stackToken = new Stack<>();
		Stack<Value> stackResult = new Stack<>();
		Value finalValue = null;
		String token = scanner.currentToken.tokenStr;
		Token popped;
		boolean containsOperator = false;
		boolean evaluated = false; //is true when final evaluated result of expression is obtained
		//if (check.structure != Structure.PRIMITIVE){
		//	System.out.println("We's in here");
			//check = parseArrayRef();
		//}
		while (!(token.equals(";") || token.equals(":") || token.equals(",") || token.equals("]") || token.equals("to") || token.equals("in") ||  token.equals("by"))) {
			

			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				//if function or operand place in postfix out
				boolean function = false;
				if (scanner.currentToken.primClassif == Token.OPERAND){
					if(scanner.currentToken.subClassif == Token.IDENTIFIER && ((STIdentifier) 
							symbolTable.getSymbol(token)).structure == Structure.FIXED_ARRAY){
							Token array = parseArrayRef();
							out.add(array);
					}else
						out.add(scanner.currentToken);
				}
				if (scanner.currentToken.primClassif == Token.FUNCTION){
					function = true;
					Token funcResult = parseFunctionCall();
					if (funcResult != null)
						out.add(funcResult);
					if(scanner.nextToken.tokenStr.equals(";")){
						break;
					}
				}
			}
			else if (scanner.currentToken.primClassif == Token.OPERATOR){
				containsOperator = true;
				while(!stackToken.isEmpty()){
					//if operator, check precedence
					if(precedence.get(token) > stkPrecedence.get(stackToken.peek().tokenStr)){
						break;
					}
					out.add(stackToken.pop());
				}
				stackToken.push(scanner.currentToken); 
			}
			else if (scanner.currentToken.primClassif == Token.SEPARATOR){
				//if separator, check special cases for parentheses
				//to determine correctness
				boolean lParen = false;
				boolean lBrac = false;
				if(token.equals("(")){
					stackToken.push(scanner.currentToken);
				}
				else if(token.equals(")")){
					while(!stackToken.isEmpty()){
						popped = stackToken.pop();
						if(popped.tokenStr.equals("(")){
							lParen = true;
						}else		
							out.add(popped);
					}
					if(!lParen){
						//no matching parenthesis found
						//if parenthesis id for function, leave it to parseFunc
						break;
					}
				}
				else if (token.equals(",")){
					token = scanner.getNext();
					continue;
				}
				
				else if(token.equals("[")){
					//System.out.println("found an array");
					break;					
					//stackToken.push(value);
				}
				else {
						throw new SyntaxError("Invalid separator token '" + token + "' found in expression",
							scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
				}
				
			}
			else{
				throw new SyntaxError("Invalid token '" + token + "' found in expression",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			}
			token = scanner.getNext();
		}
		

		while(!stackToken.isEmpty()){
			popped = stackToken.pop();
			if(popped.tokenStr == "(")
				throw new SyntaxError("Missing right parenthesis for '" + popped + "' found",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			out.add(popped);
		}
		
		//System.out.println("About to start expressing");
		
		//At this point, our postfix expression is already populated
		//check for possible errors
		for(Token entry : out){			
			Value res = null, res2 = null;
			token = entry.tokenStr;
			if(entry.primClassif == Token.OPERAND){
				//Found operand; check if it is an actual value
				//if not, convert to an actual value and push to stack
				switch(entry.subClassif){
					case Token.IDENTIFIER:
						res = symbolTable.getSymbol(token).getValue();
						//Array found?
						stackResult.push(res);
						break;
					case Token.INTEGER:
					case Token.FLOAT:
					case Token.BOOLEAN:
					case Token.STRING:
					case Token.DATE :
						res = entry.toResult();
						stackResult.push(res);
						break;
					default:
						throw new TypeError("Found non-existent datatype", entry);
				}
			}	
			else if(entry.primClassif == Token.OPERATOR){
				//found operator
				if(!stackResult.isEmpty()){
					Value unary; //to handle special unary operations
					switch(token){
						case "u-":
							unary = Operators.unaryMinus(this, stackResult.pop());
							stackResult.push(unary);
							break;
						case "not":
						case "!":
							unary = Operators.unaryNot(this, stackResult.pop());
							stackResult.push(unary);
							break;
						case "+":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.add(this, res2, res));
							break;
						case "-":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.subtract(this, res2, res));
							break;
						case "*":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.multiply(this, res2, res));
							break;
						case "/":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.divide(this, res2, res));
							break;
						case "#":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.concatenate(this, res2, res));
							break;
						case "<":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.less(this, res2, res));
							break;
						case "^":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.exponentiate(this, res2, res));
							break;
						case "<=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.lessEqual(this, res2, res));
							break;
						case ">":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.greater(this, res2, res));
							break;
						case ">=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.greaterEqual(this, res2, res));
							break;
						case "==":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.doubleEqual(this, res2, res));
							break;
						case "!=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.notEqual(this, res2, res));
							break;
						case "and":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.logicalAnd(this, res2, res));
							break;
						case "or":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Operators.logicalOr(this, res2, res));
							break;
						default:
							throw new UnsupportedOperationError("The expression cannot be evaluated because of an invalid operator.", entry);
					}
					
				}else
					throw new UnsupportedOperationError("No operand(s) to be evaluated by the operator.", entry);
			}
			else
				throw new UnsupportedOperationError("Invalid token entry found in expression.", entry);
		}
		
		//if stack is not empty
		//what's left in stack should be the final result
		if (!stackResult.isEmpty() && !evaluated){
			finalValue = stackResult.pop();
			evaluated = true;
			if (debugExpr && containsOperator){
				System.out.println("\t\t... Expression result = " + finalValue);
			}
		}else if(stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too few operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		} if(!stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too many operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		}
		 
		//System.out.println("----------> Exit parse expression <---------" + finalValue);
		return finalValue;
	}


}	
