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
import havabol.storage.Structure;

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
		ResultValue resCond = parseExpression(":");
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
		
		ResultValue whileCond;		
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
	 * TODO:
	 * Preconditions:
	 * 		-
	 * Purpose:
	 */
	private void parseFor() {
		// for expr [in/to] expr:
		// if 3rd token is "in", second expression
		scanner.getNext(); // get past "for"
		
		// 
		while (!(scanner.currentToken.tokenStr.equals("in") || scanner.currentToken.tokenStr.equals("to"))) {
			
		}
		// 
		if (scanner.currentToken.tokenStr.equals("in")){
			
		}
		// 
		else if (scanner.currentToken.tokenStr.equals("to")){
			
		}
		while (!(scanner.currentToken.tokenStr.equals("endfor"))){
			
		}
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
					parseFor();
					break;
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
		ResultValue rhsExpr; // right hand side of assignment if it exists
		
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
					case "unbound":
						variable = new STIdentifier(identifier, declaredType, Structure.UNBOUNDED_ARRAY, "", 0);
						rhsExpr = parseValueList(";");
						variable.setValue(rhsExpr);
						break;
					case "]": // Array size depends on the length of given value list
						variable = new STIdentifier(identifier, declaredType, Structure.FIXED_ARRAY, "", 0);
						rhsExpr = parseValueList(";");
						variable.setValue(rhsExpr);
						variable.declaredSize = rhsExpr.numItems;
						break;
					default:
						//array size might be an expression
						ResultValue value = parseExpression("]");
						//if (scanner.currentToken.subClassif == Token.INTEGER) { // Given fixed array size
						if(value.dataType == DataType.INTEGER){
							variable = new STIdentifier(identifier, declaredType, Structure.FIXED_ARRAY, "", 0);
							variable.declaredSize = value.intValue;	
							//variable.declaredSize = scanner.currentToken.toResult().asInteger(this).intValue;	
						} else {
							throw new SyntaxError("Found invalid size of declared array. Expected an integer, `unbound`, or nothing", scanner.currentToken);
						}
						if(!scanner.currentToken.tokenStr.equals("]")){
							throw new SyntaxError("Expected a closing parenthesis ']'");
						}
						
						//could be assigned
						//scanner.getNext();
						rhsExpr = parseValueList(";");
						variable.setValue(rhsExpr);
						//System.out.println("-----> I do get in here <----" + variable.getValue());
						break;
				}
				break;
				case ";": // no initialization clause
					variable = new STIdentifier(identifier, declaredType, Structure.PRIMITIVE, "", 0);
					break;
				default:
					throw new SyntaxError("Expected an assignment '=', array type specifier, or semicolon in declaration", scanner.currentToken);
		}
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
	private ResultValue parseValueList(String terminatingStr) {
		
		// TODO implement
		ResultValue array = new ResultValue();
		array.structure = Structure.MULTIVALUE;
		array.numItems = 0;
		
		// Parse first element and set data type to that of first elem
		ResultValue elem = scanner.currentToken.toResult();
		array.arrayValue.add(elem);
		array.dataType = elem.dataType;
		array.numItems += 1;
		
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
			//System.out.println("I'm here" + scanner.currentToken.tokenStr);
			
			elem = scanner.currentToken.toResult();
			elem = elem.asType(this, array.dataType);
			array.numItems += 1;
			array.arrayValue.add(elem);
			
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
	private ResultValue parseAssignment() {
		// assignment := identifier '=' expr

		if (scanner.currentToken.subClassif != Token.IDENTIFIER) {
			throw new SyntaxError("Expected an identifier to begin assignment", scanner.currentToken);
		}
		
		String identifier = scanner.currentToken.tokenStr;
		
		// Ensure identifier has been declared
		STIdentifier variable = (STIdentifier) symbolTable.getSymbol(identifier);
		
		if (variable == null) {
			throw new DeclarationError("Reference to undeclared identifier found", scanner.currentToken);
		}
		
		ResultValue res01;
		ResultValue res02, rhsExpr = null;
		
		String token = scanner.getNext();

		// Next token should be an assignment operator
		switch (token) {
			case "=":
				//next token should be an expression
				scanner.getNext();
				//System.out.println("check = " + check);
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
				throw new SyntaxError("Expected assignment operator as part of assignment", scanner.nextToken);
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
	private ResultValue parseArrayRef() {
		
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
		
		if (!scanner.currentToken.tokenStr.equals("~") && scanner.currentToken.subClassif != Token.INTEGER) {
			throw new SyntaxError("Expected index or beginning of slice", scanner.currentToken);
		}
		
		int beginSliceIndex = scanner.currentToken.toResult().asInteger(this).intValue;
		ResultValue result = null;
		
		switch (scanner.getNext()) {
		case "]":
			// Singular array value
			
			// TODO: check and fetch array value
			
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
		
		return result;
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
	private ResultValue parseFunctionCall() {
		
		if (scanner.currentToken.primClassif != Token.FUNCTION) {
			throw new SyntaxError("Expected name of a function", scanner.currentToken);
		}
		
		String calledFunction = scanner.currentToken.tokenStr;
		// currentToken should be open paren "("
		String check = scanner.getNext();
		
		if (!scanner.currentToken.tokenStr.equals("(")) {
			throw new SyntaxError("Expected left parenthesis after function name", scanner.currentToken);
		}
		
		scanner.getNext(); // currentToken is beginning of first arg expression or )
		
		List<ResultValue> args = new ArrayList<>();
		
		if (scanner.currentToken.tokenStr.equals(")")) {
			
		} else {
			
			// Parse all function arguments
			for (;;) {
				
				ResultValue arg = parseExpression(")");
		
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
		
		scanner.getNext();
		
		switch (calledFunction) {
		case "print":
			Functions.print(this, args);
			break;
		default:
			throw new DeclarationError("Attempted to call undefined function " + calledFunction);
		}
		
		// TODO: function call returns proper result
		ResultValue retVal = new ResultValue();
		retVal.dataType = DataType.VOID;
		
		return retVal;
	}
	
	/***
	 * Function: ParseExpression
	 * Preconditions: parseExpression is called on the first
	 * 				  token of a potential infix expression
	 * Purpose: to create a postFix expression from an input 
	 *			infix expression and evaluate the expression
	 *			correctly depending on its operator.
	 * @return the evaluated value of an expression
	 */
	private ResultValue parseExpression(String terminatingStr) throws SyntaxError{
		ArrayList <Token> out = new ArrayList<Token>();
		Stack<Token> stackToken = new Stack<>();
		Stack<ResultValue> stackResult = new Stack<>();
		ResultValue finalValue = null;
		String token = scanner.currentToken.tokenStr;
		Token popped;
		boolean containsOperator = false;
		boolean evaluated = false; //is true when final evaluated result of expression is obtained
		
		while (!(token.equals(";") || token.equals(":") || token.equals(",") || token.equals("]"))) {
			//System.out.println("In parse expr");
			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				//if function or operand place in postfix out
				if (scanner.currentToken.primClassif == Token.OPERAND)
					out.add(scanner.currentToken);
				if (scanner.currentToken.primClassif == Token.FUNCTION){
					parseFunctionCall();
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
		
		//At this point, our postfix expression is already populated
		//check for possible errors
		for(Token entry : out){			
			ResultValue res = null, res2 = null;
			token = entry.tokenStr;
			if(entry.primClassif == Token.OPERAND){
				//Found operand; check if it is an actual value
				//if not, convert to an actual value and push to stack
				switch(entry.subClassif){
					case Token.IDENTIFIER:
						res = symbolTable.getSymbol(token).getValue();
						//Array found?
						if(res.structure != Structure.PRIMITIVE)
							res = parseArrayRef();
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
					ResultValue unary; //to handle special unary operations
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
		 
		//System.out.println("----------> Exit parse expression <---------");
		return finalValue;
	}
}	
