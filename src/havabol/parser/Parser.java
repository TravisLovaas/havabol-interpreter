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
	public static Boolean debugOn= false;
	private String debugOnOff= null;
	public static String debugArg = null;
	//private DataType exprDataType = null;
	//precedence initialization
	private final static HashMap<String, Integer> precedence = new HashMap<String, Integer>(){
		private static final long serialVersionUID = 1L;

	{
		put("and", 3); put("or", 3); put("not", 4); put("in", 5); put("notin", 5);    
		put("<", 5); put(">", 5); put("<=", 5); put(">=", 5); put("==", 5); put("!=", 5);
		put("#", 6);  put("+", 7); put("-", 7); put("*", 8); put("/", 8); put("^", 10); put("u-", 11);
		put("(", 12);
	}};
	
	private final static HashMap<String, Integer> stkPrecedence = new HashMap<String, Integer>(){
		private static final long serialVersionUID = 1L;

	{
		put("and", 3); put("or", 3); put("not", 4); put("in", 5); put("notin", 5);    
		put("<", 5); put(">", 5); put("<=", 5); put(">=", 5); put("==", 5); put("!=", 5);
		put("#", 6);  put("+", 7); put("-", 7); put("*", 8); put("/", 8); put("^", 9); put("u-", 11);
		put("(", 2);
	}};
	
	public Parser(String sourceFilename, SymbolTable symbolTable) {
		
		this.symbolTable = symbolTable;
		
		try {
			scanner = new Scanner(sourceFilename, symbolTable);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void beginParsing() {
		scanner.getNext();
		while (scanner.currentToken.primClassif != Token.EOF) {
			parseStatement();
		}
	}
	
	/**
	 * parseIf will call parseExpression and to get the conditional path to take
	 * and in the if-then-else it will call parseStatement until else or endif
	 * to correctly execute desired statements.
	 * @param bExec
	 */
	
	private void parseIf() {
		//System.out.println("parse if");
		// currentToken is "if"
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
		
			//System.out.println("skipping else tokens");
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
			//System.out.println("done skipping else");
			// done, semi-colon handled by parseStatement
		} else {
			// Skip everything until else or endif
			//System.out.println("skipping if section");
			int ifCnt = 0;
			for (;;) {
				scanner.getNext();
				if (scanner.currentToken.tokenStr.equals("if")) {
					ifCnt++;
					//System.out.println("increment ifCnt");
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
			//System.out.println("executing else");
		
			// skip everything inside else
			while (!scanner.currentToken.tokenStr.equals("endif")) {
				parseStatement();
			}
			//System.out.println("last: " + scanner.currentToken.tokenStr);
			scanner.getNext(); // pass "endif"
		}
	}
	
	/**
	 * parseWhile will evaluated the expression given to while statement and 
	 * parseStatement will evaluate all the statements within the while loop
	 * until the condition value is no longer true
	 */
	
	private void parseWhile() {
		
		//System.out.println("parsewhile called");
		
		int loopSrcLine = scanner.iSourceLineNr;
		int loopColPos = scanner.iColPos;
		
		// ASSUME currentToken is "while" on call
		scanner.getNext(); // advance past "while"
		
		// Save our position to loop back!!
		
		ResultValue whileCond;
		
		//System.out.println("" + loopSrcLine + " " + loopColPos);
		
		for (;;) {
			
			// Evaluate while condition
			whileCond = parseExpression(":");
			
			// should be on ":"
			if (scanner.currentToken.tokenStr.equals(":")) {
				scanner.getNext(); // advance past ":"
			} else {
				throw new SyntaxError("Expected : after while conditional expression", scanner.currentToken);
			}
			
			assert !scanner.currentToken.tokenStr.equals(":");
			
			if (whileCond.asBoolean(this).booleanValue) {
				// Evaluated to true, execute loop
				//System.out.println("loop execing");
				while (!scanner.currentToken.tokenStr.equals("endwhile")) {
					//System.out.println("while parse statement call");
					parseStatement();
				}
				
				//System.out.println("before while reset: " + scanner.currentToken.tokenStr);
				scanner.getNext(); // pass "endwhile"
				scanner.getNext(); // pass ";"
				
				//System.out.println("after ; " + scanner.currentToken.tokenStr);
				
				// Done executing loop body, let's loop back!
				scanner.setPosition(loopSrcLine, loopColPos);
				
				//System.out.println("after loopback: " + scanner.getNext());
				//System.out.println("" + loopSrcLine + " " + loopColPos);
				
			} else {
				// Evaluated to false, skip loop past endwhile and return
				//System.out.println("loop cond false");
				
				int whileCnt = 0;
				while (!scanner.currentToken.tokenStr.equals("endwhile") || whileCnt > 0) {
					if (scanner.currentToken.tokenStr.equals("while")) {
						whileCnt++;
					} else if (scanner.currentToken.tokenStr.equals("endwhile")) {
						whileCnt--;
					}
					scanner.getNext();
				}
				//System.out.println("last inwhile:" + scanner.currentToken.tokenStr);
				
				scanner.getNext(); // pass "endwhile"
				return; // return to parseStatement, expects ;
			}
			
		}
		
	}
	
	
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
	 * Preconditions:
	 *  - currentToken is beginning of debug statement, e.g.
	 *  	debug token on;
	 *      ^^^^^
	 */
	private void parseDebugStatement() {
		
		debugArg = scanner.getNext();
		switch (debugArg.toLowerCase()){
		case "assign":
		case "Assignment":
		case "expr":
		case "Expression":
		case "token":
			debugOnOff = scanner.getNext();
			if(debugOnOff.equalsIgnoreCase("on"))
				debugOn = true;
			else if (debugOnOff.equalsIgnoreCase("off")){
				debugOn = false;
			}
			String semi = scanner.getNext();
			if(!semi.equals(";"))
				throw new SyntaxError("\"debug statement\" expects a semicolon (\";\")", scanner.currentToken);
			break;
		default:
			throw new SyntaxError("Found unsupported \"debug\" argument", scanner.currentToken);
		}
		
	}
	
	/**
	 * Parses a statement, ending with a semicolon.
	 * Preconditions: 
	 *  - currentToken is the first token in a statement, e.g.
	 *  			if myVar == 0:
	 *  			^^
	 * Postconditions:
	 *  - currentToken is the first token in a statement and directly follows a semicolon, e.g.
	 *  			i = 0;
	 *  			myVar = 1;
	 *              ^^^^^
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
			if(scanner.currentToken.tokenStr.equals("debug")) {
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

				//if(scanner.nextToken.equals("="))
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
	 * Parses a declaration statement.
	 * Preconditions:
	 *    - currentToken is a data type declaration
	 */
	private void parseDeclaration() {
		
		// Parse declared type
		DataType declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
		Structure structure = Structure.PRIMITIVE;
		String identifier;
		
		// Next token is name of variable
		if (scanner.nextToken.subClassif == Token.IDENTIFIER) {
			identifier = scanner.nextToken.tokenStr;
		} else {
			throw new DeclarationError("Expected an identifier", scanner.nextToken);
		}
		
		// Create symbol table entry
		symbolTable.createSymbol(this, identifier, new STIdentifier(identifier, Token.OPERAND, declaredType, structure, "", 0));
		
		scanner.getNext();
		// currentToken should now be an identifier
		
		// Check for an assignment
		if (scanner.nextToken.tokenStr.equals("=")) {
			parseAssignment();
		} else if (scanner.nextToken.tokenStr.equals(";")) {
			scanner.getNext();
		} else {
			throw new SyntaxError("Expected semi-colon", scanner.currentToken);
		}
		
	}
	
	/**
	 * parseAssignment is called for all lines of assignment
	 * within parseExpression
	 * Preconditions:
	 *    - currentToken is an identifier
	 *    
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
		
		if(debugOn){
			switch(debugArg.toLowerCase()){
				case "assign":
				case "assignment":
					System.out.println("\t\t... Assignment variable = " + identifier);
					System.out.println("\t\t... Assignment value = " + rhsExpr);
					break;
				default:
					break;
			}
		}
		// Ensure type of rhsExpr matches declared type, or can be cast to such.
		return rhsExpr;
	}	
	
	/**
	 * Parses a reference to an array value or slice
	 * Preconditions:
	 * 	- currentToken is an array type identifier, e.g.
	 * 		j = tArray[2~4];
	 *          ^^^^^^		
	 * @return
	 */
	private ResultValue parseArrayRef() {
		
		String arrayName = scanner.currentToken.tokenStr;
		
		// assert currentToken is a valid array identifier
		if (scanner.currentToken.primClassif != Token.IDENTIFIER ||
			!symbolTable.containsSymbol(arrayName)) {
			throw new SyntaxError("Expected an identifier for array reference", scanner.currentToken);
		}
		
		ResultValue array = symbolTable.getSymbol(arrayName).getValue();
		
		if (array.structure != Structure.FIXED_ARRAY && array.structure != Structure.UNBOUNDED_ARRAY) {
			throw new TypeError("Expected an array type but found " + array.structure, scanner.currentToken);
		}
		// precondition should be true
		
		// TODO
		
		throw new UnsupportedOperationError("array reference not yet implemented");
		
	}

	/**
	 * Parses a function call
	 * Preconditions:
	 *    - currentToken is the name of a function in a function call, e.g.
	 *           print("hello", "world");
	 *      	 ^^^^^
	 * @return
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
	 * Assumption: parseExpression is called before potential
	 * infix expression
	 * Creates a postFix expression from stack
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
		boolean evaluated = false; //we have popped evaluated result value of expression
		
		while (!(token.equals(";") || token.equals(":") || token.equals(","))) {
			//get token string
			//token = scanner.currentToken.tokenStr;
			//if function or operand place in out
			
			//System.out.println("------------> Current token = " + token + "<--------------");
			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				//add the identifier or function to postfix out
				if (scanner.currentToken.primClassif == Token.OPERAND)
					out.add(scanner.currentToken);
				if (scanner.currentToken.primClassif == Token.FUNCTION){
					parseFunctionCall();
				}

			}
			
			
			//if operator, check precedence
			else if (scanner.currentToken.primClassif == Token.OPERATOR){
				containsOperator = true;
				while(!stackToken.isEmpty()){
					//System.out.println("precedence of token = " + precedence.get(stackToken.peek()));
					if(precedence.get(token) > stkPrecedence.get(stackToken.peek().tokenStr)){
						break;
					}
					//pop from stackToken if precedence is less than or equal to
					out.add(stackToken.pop());
				}
				stackToken.push(scanner.currentToken); 
			}
			
			//if separator, check special cases for parentheses
			//to determine correctness
			else if (scanner.currentToken.primClassif == Token.SEPARATOR){
				boolean lParen = false;
				//token = scanner.nextToken.tokenStr;
				if(token.equals("(")){
					stackToken.push(scanner.currentToken);
				}
				else if(token.equals(")")){
					while(!stackToken.isEmpty()){
						popped = stackToken.pop();
						if(popped.tokenStr.equals("(")){
							//System.out.println("------> I'm in here popping = " + popped.tokenStr + " <----");
							lParen = true;
						}else		
							out.add(popped);
					}
					//did not find matching parenthesis
					if(!lParen){
						break;
					}
				}
				else if (token.equals(",")){
					token = scanner.getNext();
					continue;
				}
				else {
					//invalid separator found, at this point ',' would be invalid
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
		
		//at this point, our postfix expression is already populated
		//Error checks for validity of expression	
		for(Token entry : out){			
			
			//if you find an operand
			ResultValue res, res2 = null;
			token = entry.tokenStr;
			if(entry.primClassif == Token.OPERAND){
				//check if it is an actual value
				//if not, convert to an actual value and push to stack
				switch(entry.subClassif){
					case Token.IDENTIFIER:
						res = symbolTable.getSymbol(token).getValue();
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
						//operand type does not exist
				}
			}
				
			// else you find an operator,
			else if(entry.primClassif == Token.OPERATOR){
				// if stack is not empty
				if(!stackResult.isEmpty()){
					//handle unary operators u- and !
					ResultValue unary;
					switch(token){
						case "u-":
							// push -1 * pop first operand
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
		if(!stackResult.isEmpty() && !evaluated){
			finalValue = stackResult.pop();
			evaluated = true;
			if(debugOn && containsOperator){
				switch(debugArg.toLowerCase()){
					case "expr":
					case "expression":
						System.out.println("\t\t... Expression result = " + finalValue);
						break;
					default:
						break;
				}
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
