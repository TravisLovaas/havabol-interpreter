package havabol.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import havabol.error.SyntaxError;
import havabol.error.UnsupportedOperationError;
import havabol.lexer.*;
import havabol.runtime.Execute;
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
			parseToken();
		}
	}
	
	private void parseIf() {
		// while ":" is not found
		while (scanner.getNext() != ":") {
			// find 
			if (scanner.currentToken.primClassif == Token.OPERAND){
				
			}
		}
	}
	
	private void parseToken() {
		if (scanner.currentToken.primClassif == Token.CONTROL) {
			if (scanner.currentToken.subClassif == Token.DECLARE) {
				parseDeclaration();
			}
			if (scanner.currentToken.subClassif == Token.FLOW) {
				//parseIf();
				//parseWhile();
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
						ResultValue initValue = parseExpression();
						if (initValue.dataType != declaredType) {
							// TODO: type mismatch
						}
						
						//i took out this line because there is no "value" at this point
						//symbolTable.getSymbol(identifier).setValue(initValue.value);
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
	
	/**
	 * parseAssignment is called for all lines of assignment
	 * within parseExpression
	 * @return the evaluated value of the assignment
	 */
	
	
	private ResultValue parseAssignment() {
		// assignment := identifier '=' expr
		
		DataType declaredType = null;
		Structure structure = Structure.PRIMITIVE;
		ResultValue res = null;
		String identifier;
		String variable = null;
		SymbolTable st = this.symbolTable;
		while (scanner.getNext() != ";") {
			// token string
			identifier = scanner.currentToken.tokenStr;
			// if data type is found
			if (scanner.currentToken.primClassif == Token.DECLARE) {
				// takes in the data type
				declaredType = DataType.stringToType(scanner.currentToken.tokenStr);
				// next token is variable
				scanner.getNext();
				variable = scanner.currentToken.toString();
				continue;
			} else if (identifier == "=" || identifier == "-=" || identifier == "+=") {
				continue;
			} else {
				// check if token is in symbol table
				if(symbolTable.containsSymbol(variable)){
					// get value of res
					res = parseExpression();
					// create symbol
					st.createSymbol(this, variable, new STIdentifier(variable, Token.OPERAND, declaredType, structure, "", 0));
				}
			}
		}
		return res;
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

	
	private ResultValue parseFunctionCall() {
		// TODO: recursively parse a function
		return null;
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
		Token popped;
		boolean lParen = false;
		boolean evaluated = false; //we have popped evaluated result value of expression
		
		while (scanner.getNext() != ";" || scanner.getNext() != ":") {
			//get token string
			token = scanner.currentToken.tokenStr;
			//if function or operand place in out
			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				//add the identifier or function to postfix out
				out.add(scanner.currentToken);
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
				if(token == "(")
					stackToken.push(scanner.currentToken);
				else if(token == ")"){
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
				}
				else{
					//invalid separator found, at this point ',' would be invalid
					throw new SyntaxError("Invalid separator token '" + token + "' found in expression",
							scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
				}
			}
			
			else{
				throw new SyntaxError("Invalid token '" + token + "' found in expression",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			}
		}
				
		while(!stackToken.isEmpty()){
			popped = stackToken.pop();
			if(popped.tokenStr == "(")
				throw new SyntaxError("Missing right parenthesis for '" + popped + "' found",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			out.add(popped);
		}
		
		//System.out.println("*************************I'M IN PARSE EXPRESSION, ARE YOU HERE? *************************");
		
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
						break;
					default:
						//operand type does not exist
				}
			}
			
			else if(entry.primClassif == Token.FUNCTION){
				//not positive
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
							unary = Execute.unaryMinus(this, stackResult.pop());
							stackResult.push(unary);
							break;
						case "not":
						case "!":
							unary = Execute.unaryNot(this, stackResult.pop());
							stackResult.push(unary);
							break;
						case "+":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.add(this, res2, res));
							break;
						case "-":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.subtract(this, res2, res));
							break;
						case "*":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.multiply(this, res2, res));
							break;
						case "/":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.divide(this, res2, res));
							break;
						case "#":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.concatenate(this, res2, res));
							break;
						case "<":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.less(this, res2, res));
							break;
						case "<=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.lessEqual(this, res2, res));
							break;
						case ">":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.greater(this, res2, res));
							break;
						case ">=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.greaterEqual(this, res2, res));
							break;
						case "==":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.doubleEqual(this, res2, res));
							break;
						case "!=":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.notEqual(this, res2, res));
							break;
						case "and":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.logicalAnd(this, res2, res));
							break;
						case "or":
							res = stackResult.pop();
							if(!stackResult.isEmpty())
								res2 = stackResult.pop();
							else
								throw new UnsupportedOperationError("Too few operands for operation to be evaluated.", entry.iSourceLineNr, entry.iColPos);
							stackResult.push(Execute.logicalOr(this, res2, res));
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
		}else if(stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too few operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		} if(!stackResult.isEmpty() && evaluated){
			throw new UnsupportedOperationError("Invalid Expression found. There are too many operands for the operators provided"
					, scanner.currentToken.iSourceLineNr);
		}
		
		return finalValue;
	}
	
	
	
	
}	
			