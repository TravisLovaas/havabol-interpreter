package havabol.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import havabol.error.SyntaxError;
import havabol.error.UnsupportedOperationError;
import havabol.lexer.*;
import havabol.runtime.Execute;
import havabol.runtime.Value;
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
	
	
	/***
	 * Assumption: parseExpression is called before potential
	 * I am not dealing with conditionals at the moment
	 * infix expression
	 * Creates a postFix expression from stack
	 * @return the evaluated value of an expression
	 */
	@SuppressWarnings("rawtypes")
	private ResultValue parseExpression() throws SyntaxError{
		ArrayList <Object> out = new ArrayList<Object>();
		Stack <Object> stack = new Stack <Object>();
		String token;
		Object popped;
		boolean lParen = false;
		boolean evaluated = false; //we have popped evaluated result value of expression
		
		while (scanner.getNext() != ";") {
			//get token string
			token = scanner.currentToken.tokenStr;
			//if function or operand place in out
			if (scanner.currentToken.primClassif == Token.OPERAND || scanner.currentToken.primClassif == Token.FUNCTION) {
				switch(scanner.currentToken.subClassif){
					case Token.IDENTIFIER:
						//function to return actual values isn't complete yet
						ResultValue value = STEntry.getValue(this, token);
						out.add(value);
						break;
					case Token.INTEGER:
						Value value1 = new Value(DataType.INTEGER, token);
						out.add(value1.getValue());
						break;
					case Token.FLOAT:
						Value value2 = new Value(DataType.FLOAT, token);
						out.add(value2.getValue());
						break;
					case Token.DATE:
						Value value3 = new Value(DataType.DATE, token);
						out.add(value3.getValue());
						break;
					case Token.STRING:
						Value value4 = new Value(DataType.STRING, token);
						out.add(value4.getValue());
						break;
					case Token.BOOLEAN:
						Value value5 = new Value(DataType.BOOLEAN, token);
						out.add(value5.getValue());
						break;
					//this is a function for sure.
					default:
						out.add(token);
				}
			}
			
			//if operator, check precedence
			else if (scanner.currentToken.primClassif == Token.OPERATOR){
				while(!stack.isEmpty()){
					if(precedence.get(token) > stkPrecedence.get(stack.peek())){
						break;
					}
					//pop from stack if precedence is less than or equal to
					out.add(stack.pop());
				}
				stack.push(token); 
			}
			
			//if separator, check special cases for parentheses
			//to determine correctness
			else if (scanner.currentToken.primClassif == Token.SEPARATOR){
				if(token == "(")
					stack.push(token);
				else if(token == ")"){
					while(!stack.isEmpty()){
						popped = stack.pop();
						if(popped == "("){
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
				
		while(!stack.isEmpty()){
			popped = (String) stack.pop();
			if(popped == "(")
				throw new SyntaxError("Missing right parenthesis for '" + popped + "' found",
						scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
			out.add(popped);
		}
		
		//System.out.println("*************************I'M IN PARSE EXPRESSION, ARE YOU HERE? *************************");
		
		//at this point, our postfix expression is already populated
		//should contain only operands, operators, and functions
		//still haven't checked for validity of expression
		//the stack is empty.
		
		for(Object entry : out){
			//go through each entry in postfix
			
			//If you find a number
			//push to stack
			if(entry instanceof Number){
				stack.push((Number) entry);
			}
			//we have an operator
			else if(precedence.containsKey(entry)){
				/* if stack is not empty
				   if u-
				   push -1 * pop first operand
				   else pop the first operand from the stack
				   else error invalid expression found 
				   if the stack is not empty
				   pop the second operand from the stack
				   else error invalid expression found*/
				
				if (!stack.isEmpty()){
					if(entry == "u-"){
						popped = stack.pop();
						if(popped instanceof Integer)
							stack.push(-1 * (Integer) popped);
						else
							stack.push(-1 * (Double) popped);
					}
					
					/*else{
						if(!stack.isEmpty())
							ResultValue resOp1 = stack.pop();
						else
							throw new SyntaxError("Parser could not parse invalid Expression: " + out,
									scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);
						if(!stack.isEmpty())
							ResultValue resOp2 = stack.pop();
						else
							throw new SyntaxError("Parser could not parse invalid Expression: " + out,
									scanner.currentToken.iSourceLineNr, scanner.currentToken.iColPos);		
					}*/
				}
				
				// depending on operator, call the appropriate evaluate function
				// then push result to the stack
				switch (entry.toString()){
					case "+":
					case "-":
					case "/":
					case "*":
					case "^":
					case "#":
					case "<":
					case ">":
					case "<=":
					case ">=":
					case "==":
					case "!=":
					case "not":
					case "and":
					case "or":
					default:
						throw new UnsupportedOperationError("The operator is an invalid token for operation", (Token) entry);
				}
			}
									
			// switch (operator)
			
			//default invalid operator found
			//if(out.get(i))
		}
		
		//if stack is not empty
		//f finalRes = pop from the stack
		//evaluated = true;
		//else if !stack.isEmpty && evaluated
		//incorrect expression found
		//return finalRes;
		return null;
	}
	
}	
				/*if (scanner.currentToken.primClassif == Token.OPERAND) {
					op1 = ResultValue.tokenStrToResult(this, dataType, scanner.currentToken.tokenStr);
				} else {
					op1 = parseFunctionCall();
				}
				
				if (scanner.nextToken.primClassif == Token.OPERATOR) {
					// Option 1: we found an operator
					scanner.getNext();
					token = scanner.currentToken.tokenStr;
					//ResultValue op2 = parseExpression(dataType);
					//switch (token){
					
					stack.push(token);
					
					if (){
						scanner.getNext();
						token = scanner.currentToken.tokenStr;				
					}
					
					
				} else if(scanner.nextToken.primClassif == Token.SEPARATOR){
					// Option 2: that was the final operand
					// TODO: construct new ResultValue and return.
				}
				else{
					// TODO: error, was expecting operator or separator
				}
				
			} else {
				// TODO: error expected operand or function call, found nothing
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
*/	
	//unary minus
	//parse expr
	//if statements

