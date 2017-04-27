package havabol.runtime;

import java.util.List;
import havabol.error.*;
import havabol.lexer.Token;
import havabol.parser.*;
import havabol.storage.*;

public class Operators {
	
	/**
	 * Function: add
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with an addition operator
	 * Purpose: Adds to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value add(Parser parser, Value op1, Value op2) {
		
		//System.out.print("add: " + op1 + " " + op2);
		
		Value res = new Value();
		
		switch (op1.dataType) {
		case INTEGER:
			
			// Coerce second operand into an integer
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue + op2.asInteger(parser).intValue;
			
			break;
		case FLOAT:
			
			// Coerce second operand into an float
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue + op2.asFloat(parser).floatValue;
			
			break;
		case STRING:
			
			// Coerce both operands into floats
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.asFloat(parser).floatValue + op2.asFloat(parser).floatValue;
			
			break;
		default:
			throw new TypeError("Left hand operand is not castable to numeric type for addition");
		}
		
		//System.out.println(" = " + res);
		
		return res;
	}
	
	/**
	 * Function: subtract
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a subtraction operator
	 * Purpose: Subtracts to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value subtract(Parser parser, Value op1, Value op2) {
		
		//System.out.print("subtract: " + op1 + " " + op2);
		
		Value res = new Value();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				op2 = op2.asInteger(parser);
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue - op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				op2 = op2.asFloat(parser);
			}
			
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue - op2.floatValue;
			
		} else {
			// TODO: handle str as first operand, etc
			throw new UnsupportedOperationError("First operand type cannot be used in addition.");
		}
				
		return res;
	}
	
	/**
	 * Function: multiply
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a multiplication operator
	 * Purpose: Multiplies to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value multiply(Parser parser, Value op1, Value op2) {
		
		//System.out.print("multiply: " + op1 + " " + op2);
		
		Value res = new Value();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				op2 = op2.asInteger(parser);
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue * op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				op2 = op2.asFloat(parser);
			}
			
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue * op2.floatValue;
			
		} else {
			// TODO: handle str as first operand, etc
			throw new UnsupportedOperationError("\nFirst operand type cannot be used in multiplication.", parser.scanner.iSourceLineNr);
		}
		
		//System.out.println(" = " + res);
		
		return res;
	}
	
	/**
	 * Function: divide
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a division operator
	 * Purpose: Divides to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value divide(Parser parser, Value op1, Value op2) {
		
		//System.out.print("divide: " + op1 + " " + op2);
		
		Value res = new Value();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				op2 = op2.asInteger(parser);
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue / op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				op2 = op2.asFloat(parser);
			}
			
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue / op2.floatValue;
			
		} else {
			// TODO: handle str as first operand, etc
			throw new UnsupportedOperationError("First operand type cannot be used in addition.");
		}
				
		return res;
	}
	
	/**
	 * Function: exponentiate
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with an exponentiation operator
	 * Purpose: raise to a power to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value exponentiate(Parser parser, Value op1, Value op2) {
		
		//System.out.print("exponentiate: " + op1 + " " + op2);
		
		Value res = new Value();
		
		// op1 dataType determines result of 
		if (op1.dataType == DataType.INTEGER) {
			op2 = op2.asInteger(parser);
			
			res.intValue = (int) Math.pow(op1.intValue, op2.intValue);
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			
		} else {
			op1 = op1.asFloat(parser);
			op2 = op2.asFloat(parser);
			
			res.floatValue = Math.pow(op1.floatValue, op2.floatValue);
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
		}
		
		//System.out.println(" = " + res);
		
		return res;
	}

	
	/**
	 * Function: unaryMinus
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a unaryMinus operator
	 * Purpose: Multiplies by a '-1' to evaluate operand
	 * @param parser - information about  values being parsed
	 * @param op1	 - operand to be evaluated with '-1'
	 * @return final value after some expression has been evaluated
	 */
	public static Value unaryMinus(Parser parser, Value op1)
	{
		
		Value res = new Value();
		res.dataType = op1.dataType;
		res.structure = Structure.PRIMITIVE;
		
		if (op1.dataType == DataType.INTEGER) {
			res.intValue = op1.intValue * -1;
		} else if (op1.dataType == DataType.FLOAT) {
			res.floatValue = op1.floatValue * -1;
		} else {
			throw new UnsupportedOperationError("Unary minus may not be used on non-numeric types.");
		}
		
		return res;
	}

	
	/**
	 * Function: unaryNot
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a negation operator
	 * Purpose: Negates to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1	 - operand to be negated with '!'
	 * @return final value after some expression has been evaluated
	 */
	public static Value unaryNot(Parser parser, Value op1)
	{
		
		Value res = op1.asBoolean(parser);
		res.booleanValue = !res.booleanValue;
		
		return res;
	}

	
	/**
	 * Function: less
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a less than operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value less(Parser parser, Value op1, Value op2)
	{
		
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue < op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue < op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = op1.strValue.compareTo(op2.strValue) < 0;
			
		} else {
			throw new UnsupportedOperationError("");
		}
		
		return res;
	}

	
	/**
	 * Function: greater
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a greater than operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value greater(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue > op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue > op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = op1.strValue.compareTo(op2.strValue) > 0;
			
		} else {
			throw new UnsupportedOperationError("");
		}
		
		return res;
	}

	
	/**
	 * Function: lessEqual
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a less than or equal to
	 * 		operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value lessEqual(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue <= op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue <= op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = op1.strValue.compareTo(op2.strValue) <= 0;
			
		} else {
			throw new UnsupportedOperationError("");
		}
		
		return res;
	}

	
	/**
	 * Function: greaterEqual
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a greater than or equal to
	 * 		operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value greaterEqual(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue >= op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue >= op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = op1.strValue.compareTo(op2.strValue) >= 0;
			
		} else {
			throw new UnsupportedOperationError("");
		}
		
		return res;
	}

	
	/**
	 * Function: doubleEqual
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with an '==' operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value doubleEqual(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue == op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue == op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = op1.strValue.equals(op2.strValue);
			
		} else if (op1.dataType == DataType.BOOLEAN) {
			
			op2 = op2.asBoolean(parser);
			res.booleanValue = op1.booleanValue == op2.booleanValue;
			
		} else {
			throw new UnsupportedOperationError("Unsupported type");
		}
		
		return res;
	}

	/**
	 * Function: notEqual
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a '!=' operator
	 * Purpose: Compares to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value notEqual(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		if (op1.dataType == DataType.INTEGER) {
			
			op2 = op2.asInteger(parser);
			res.booleanValue = op1.intValue != op2.intValue;
			
		} else if (op1.dataType == DataType.FLOAT) {
			
			op2 = op2.asFloat(parser);
			res.booleanValue = op1.floatValue != op2.floatValue;
			
		} else if (op1.dataType == DataType.STRING) {
			
			op2 = op2.asString(parser);
			res.booleanValue = !op1.strValue.equals(op2.strValue);
			
		} else {
			throw new UnsupportedOperationError("");
		}
		
		return res;
	}

	
	/**
	 * Function: logicalAnd
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a logical and '&&'
	 * 		operator
	 * Purpose: Compares one boolean to another to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value logicalAnd(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		op1 = op1.asBoolean(parser);
		op2 = op2.asBoolean(parser);
		
		res.booleanValue = op1.booleanValue && op2.booleanValue;
		
		return res;
	}

	
	/**
	 * Function: logicalOr
	 * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a logical or '||'
	 * 		operator
	 * Purpose: Compares one boolean to another to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value logicalOr(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		op1 = op1.asBoolean(parser);
		op2 = op2.asBoolean(parser);
		
		res.booleanValue = op1.booleanValue || op2.booleanValue;
		
		return res;
	}

	
	/**
	 * Function: concatenate
	 *  * Preconditions:
	 * 		- parseExpression has identified operands that 
	 * 		should be evaluated with a concatenation '#'
	 * 		operator
	 * Purpose: Appends one string to another
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return final value after some expression has been evaluated
	 */
	public static Value concatenate(Parser parser, Value op1, Value op2)
	{
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.STRING;
		
		op1 = op1.asString(parser);
		op2 = op2.asString(parser);
		
		res.strValue = op1.strValue.concat(op2.strValue);
		
		return res;
	}

	
	/**
	 * Function: IN
	 *  * Preconditions:
	 *  	- parseExpression has identified operands that
	 *  	should be evaluated with an IN operator
	 * Purpose: Checks if a string is inside another string
	 * @param parser - information about values being parsed
	 * @param op1	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return boolean true or false if string is found
	 */
	public static Value IN(Parser parser, Value op1, Value op2)
	{
		int i;
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		op1 = op1.asString(parser);
		op2 = op2.asString(parser);
		int first = 0;
		int last = op1.asString(parser).strValue.length();
		
		for(i = 0; last <= op2.strValue.length(); i++){
			if(op2.strValue.substring(first, last).equals(op1.strValue)){
				res.booleanValue = true;
				return res;
			}
			first++;
			last++;
		}
		
		res.booleanValue = false;
		return res;
	}
	
	public static Value NOTIN(Parser parser, Value op1, Value op2)
	{
		int i;
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.BOOLEAN;
		
		op1 = op1.asString(parser);
		op2 = op2.asString(parser);
		int first = 0;
		int last = op1.asString(parser).strValue.length();
		
		for(i = 0; last <= op2.strValue.length(); i++){
			if(op2.strValue.substring(first, last).equals(op1.strValue)){
				res.booleanValue = false;
				return res;
			}
			first++;
			last++;
		}
		
		res.booleanValue = true;
		return res;
	}
	
}
