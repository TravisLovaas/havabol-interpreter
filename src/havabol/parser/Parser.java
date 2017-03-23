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
		while (!scanner.getNext().isEmpty()) {
			parseStatement();
		}
	}
	
	/**
	 * parseIf will call parseExpression and Operators or skip the statements
	 * in the if-then-else statement using statements function
	 * @param bExec
	 */
	
	private void parseIf() {
		scanner.getNext();
		ResultValue resCond = parseExpression();
		if (!scanner.currentToken.tokenStr.equals(":")){
			throw new SyntaxError("Expected ':' after conditional expression in if", scanner.currentToken);
		}
		scanner.getNext();
		if (resCond.asBoolean(this).booleanValue){
			// parse the true statements
			while (!scanner.currentToken.tokenStr.equals("else") || !scanner.currentToken.tokenStr.equals("endif")) {
				parseStatement();
			}
			scanner.getNext();
			while (!scanner.currentToken.tokenStr.equals("endif")) {
				scanner.getNext();
			}
		}
		else if(resCond.asBoolean(this).booleanValue == false){
			while (!scanner.currentToken.tokenStr.equals("else") || !scanner.currentToken.tokenStr.equals("endif")) {
				scanner.getNext();
			}
			while (!scanner.currentToken.tokenStr.equals("endif")) {
				parseStatement();
			}
		}
	}
	
	private void parseWhile() {
		
	}
	
	private void parseFor() {
		
	}
	
	private void parseStatement() {
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
			} else {
				throw new UnsupportedOperationError("Unknown CONTROL token found.");
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
			return;
		} else {
			throw new UnsupportedOperationError("Unexpected token '" + scanner.currentToken.tokenStr + "' found while parsing statements.");
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
		
		// currentToken should be our identifier after this call
		scanner.getNext();
		
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
				res02 = parseExpression();
				//System.out.println("res02 = " + res02.toString());
				// Ensure type of rhsExpr matches declared type, or can be 	cast to such.
				//System.out.println("token = " + token);
				rhsExpr = res02.asType(this, variable.declaredType); // Parse expression on right-hand side of assignment
				//System.out.println(rhsExpr.toString());
				variable.setValue(rhsExpr);
				break;
			case "+=":
				scanner.getNext();
				res02 = parseExpression();
				res01 = symbolTable.getSymbol(identifier).getValue();
				//run the subtract, Operators should figure out if it is valid
				rhsExpr = Operators.subtract(this, res01, res02);
				variable.setValue(rhsExpr);
				break;
			case "-=":
				scanner.getNext();
				res02 = parseExpression();
				res01 = symbolTable.getSymbol(identifier).getValue();
				//run the subtract, Operators should figure out if it is valid
				rhsExpr = Operators.add(this, res01, res02);
				variable.setValue(rhsExpr);
				break;
			case "*=":
				scanner.getNext();
				res02 = parseExpression();
				res01 = symbolTable.getSymbol(identifier).getValue();
				//run the subtract, Operators should figure out if it is valid

				rhsExpr = Operators.multiply(this, res01, res02);
				variable.setValue(rhsExpr);
				break;
			case "/=":
				scanner.getNext();
				res02 = parseExpression();
				res01 = symbolTable.getSymbol(identifier).getValue();
				//run the subtract, Operators should figure out if it is valid
				rhsExpr = Operators.divide(this, res01, res02);
				variable.setValue(rhsExpr);
				break;
			default:
				throw new SyntaxError("Expected assignment operator as part of assignment", scanner.nextToken);
		}
		
		// Next token should be an expression
		//scanner.getNext();
		
		//ResultValue rhsExpr = parseExpression(); // Parse expression on right-hand side of assignment
		
		// Ensure type of rhsExpr matches declared type, or can be cast to such.
		
		//variable.setValue(rhsExpr);
		
		return rhsExpr;
		
//		DataType declaredType = null;
//		Structure structure = Structure.PRIMITIVE;
//		ResultValue res = null;
//		String identifier;
//		String variable = null;
//		SymbolTable st = this.symbolTable;
//		while (scanner.getNext() != ";") {
//			// token string
//			identifier = scanner.currentToken.tokenStr;
//			// if data type is found
//			if (scanner.currentToken.primClassif == Token.DECLARE) {
//				// takes in the data type
//				declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
//				// next token is variable
//				scanner.getNext();
//				variable = scanner.currentToken.toString();
//				continue;
//			} else if (identifier == "=" || identifier == "-=" || identifier == "+=") {
//				continue;
//			} else {
//				// check if token is in symbol table
//				if(symbolTable.containsSymbol(variable)){
//					// get value of res
//					res = parseExpression();
//					// create symbol
//					st.createSymbol(this, variable, new STIdentifier(variable, Token.OPERAND, declaredType, structure, "", 0));
//				}
//			}
//		}
//		return res;
	}
	
	/*
	private ResultValue parseAssignment() {
	    ResultValue res = null;
	    SymbolTable st = this.symbolTable;
	    if(scanner.currentToken.subClassif != Token.IDENTIFIER){
	    	//error("expected a variable for the target of an assignment");
	    }
	    String variableStr = scanner.currentToken.tokenStr;

	    // get the assignment operator and check it
	    scanner.getNext();
	    if(scanner.currentToken.primClassif != Token.OPERATOR){
	    	//error("expected assignment operator");
	    }

	    String operatorStr = scanner.currentToken.tokenStr;
	    ResultValue resO2;
	    ResultValue resO1;
	    Numeric nOp2;  // numeric value of second operand
	    Numeric nOp1;  // numeric value of first operand
	    switch(operatorStr){
		    case "=":
		    	resO2 = parseExpression();   
//		    	res = assign(variableStr, resO2);  // assign to target
		    	
		    	st.createSymbol(this, variableStr, new STIdentifier(variableStr, Token.OPERAND, ));
		    case "-=":
		    	resO2 = parseExpression();   
		    	// expression must be numeric, raise exception if not
//		    	nOp2 = new Numeric(this, resO2, “-=”, “2nd operand”);
		    	// Since it is numeric, we need value of target variable 
//		    	resO1 = getVariableValue(variableStr);
		    	// target variable must be numeric
//		    	nOp1 = new Numeric(this, resO1, “-=”, “1st operand”);
	
		    	// subtract 2nd operand from first and assign it
//		    	res = assign(variableStr, Utility.subtract(this, nop1, nop2));
		    case "+=":
		    	// fill it in yourself
		    default:
		    	//error("expected assignment operator");
	    }    

	    return res;
	}
	*/

	/**
	 * Parses a function call
	 * Preconditions:
	 *    - currentToken is the name of a function in a function call
	 *      i.e. print("hello", "world");
	 *      	 ^^^^^
	 * @return
	 */
	private ResultValue parseFunctionCall() {
		
		if (scanner.currentToken.primClassif != Token.FUNCTION) {
			throw new SyntaxError("Expected name of a function", scanner.currentToken);
		}
		
		String calledFunction = scanner.currentToken.tokenStr;
		//System.out.println("called func: " + calledFunction);
		
		//scanner.getNext(); // currentToken should be open paren "("
		String check = scanner.getNext();
		//System.out.println("check = " + check);
		
		if (!scanner.currentToken.tokenStr.equals("(")) {
			throw new SyntaxError("Expected left parenthesis after function name", scanner.currentToken);
		}
		
		scanner.getNext(); // currentToken is beginning of first arg expression or )
		
		//System.out.println("begin arg parsing token: " + scanner.currentToken.tokenStr);
		
		List<ResultValue> args = new ArrayList<>();
		
		if (scanner.currentToken.tokenStr.equals(")")) {
			
		} else {
			
			// Parse all function arguments
			for (;;) {
				
				//System.out.println("calling parseExpr");
				ResultValue arg = parseExpression();
				//System.out.println("arg found: " + arg);
				args.add(arg);
				
				if (scanner.currentToken.tokenStr.equals(",")) {
					continue;
				} else if (scanner.currentToken.tokenStr.equals(")")) {
					break;
				} else {
					throw new SyntaxError("Expected , or ) in function call", scanner.currentToken);
				}
				
			}
		}
		
		scanner.getNext();
		
		//System.out.print("dumping args: ");
		//for (ResultValue a : args) {
		//	System.out.print(a);
		//}
		//System.out.println();
		
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
	private ResultValue parseExpression() throws SyntaxError{
		ArrayList <Token> out = new ArrayList<Token>();
		Stack<Token> stackToken = new Stack<>();
		Stack<ResultValue> stackResult = new Stack<>();
		ResultValue finalValue = null;
		String token;
		String next = null;
		Token popped;
		boolean lParen = false;
		boolean evaluated = false; //we have popped evaluated result value of expression
		
		//System.out.println("token = "+ scanner.getNext());

		do {
			//get token string
			token = scanner.currentToken.tokenStr;
			//System.out.println("currenttoken = "+ token);

			//System.out.println("nexttoken = "+ scanner.nextToken.tokenStr);
			//if function or operand place in out
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
				while(!stackToken.isEmpty()){
					if(precedence.get(token) > stkPrecedence.get(stackToken.peek())){
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
				if(token.equals("("))
					stackToken.push(scanner.currentToken);
				else if(token.equals(")")){
					while(!stackToken.isEmpty()){
						popped = stackToken.pop();
						if(popped.tokenStr == "("){
							lParen = true;
							break;
						}
						out.add(popped);
					}
					//did not find matching parenthesis
					if(!lParen){
						//return no matching left parenthesis here
						throw new SyntaxError("No matching left parenthesis for '" + token + "' found in expression",
								scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
					}
				}else if (token.equals(",")){
					next = scanner.getNext();
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
			next = scanner.getNext();
		}while (!(next.equals(";") || next.equals(":") || next.equals(",") || next.equals(")"))); 
				//System.out.println("failed = " + scanner.currentToken.tokenStr);
		while(!stackToken.isEmpty()){
			//System.out.println("$$$$$$$$$$$$$$$$");
			popped = stackToken.pop();
			if(popped.tokenStr == "(")
				throw new SyntaxError("Missing right parenthesis for '" + popped + "' found",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			out.add(popped);
		}
		//nothing
		
		//System.out.println("*************************I'M IN PARSE EXPRESSION, ARE YOU HERE? *************************");
		
		//at this point, our postfix expression is already populated
		//Error checks for validity of expression	
		
		for(Token entry : out){			
			
			//if you find an operand
			ResultValue res, res2 = null;
			token = entry.tokenStr;
			//System.out.println("The entry is = " + entry.tokenStr);
			if(entry.primClassif == Token.OPERAND){
				//check if it is an actual value
				//if not, convert to an actual value and push to stack
				switch(entry.subClassif){
					case Token.IDENTIFIER:
						//getValue should be get variable value, for now this will do
						res = symbolTable.getSymbol(token).getValue();
						stackResult.push(res);
						break;
					case Token.INTEGER:
					case Token.FLOAT:
					case Token.BOOLEAN:
					case Token.STRING:
					case Token.DATE :
						//go back and look at toResult method
						res = entry.toResult();
						stackResult.push(res);
						//System.out.per
						break;
					default:
						//operand type does not exist
				}
			}
			
			//else if(entry.primClassif == Token.FUNCTION){
				//not positive
			//	parseFunction()
			//}
			
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
			//System.out.println("finalvalue = " + finalValue.toString());
			evaluated = true;
		}else if(stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too few operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		} if(!stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too many operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		}
		
		//System.out.println("finalvalue = " + finalValue.toString());
		return finalValue;
	}
	
	
	
	
}	
			