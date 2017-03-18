package havabol.runtime;

import havabol.error.*;
import havabol.lexer.Token;
import havabol.parser.*;
import havabol.storage.*;

public class Execute {
	
	/***
	 * Adds to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return
	 */
	public static ResultValue add(Parser parser, ResultValue op1, ResultValue op2) {
		
		ResultValue res = new ResultValue();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				// TODO: coerce op2 to INTEGER type
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue + op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				// TODO: coerce op2 to FLOAT type
			}
			
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue + op2.floatValue;
			
		} else {
			// TODO: handle str as first operand, etc
			throw new UnsupportedOperationError("First operand type cannot be used in addition.");
		}
		
		return res;
	}
	
	/***
	 * Subtracts to evaluate operands
	 * @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return
	 */
	public static ResultValue subtract(Parser parser, ResultValue op1, ResultValue op2) {
		
		ResultValue res = new ResultValue();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				// TODO: coerce op2 to INTEGER type
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue - op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				// TODO: coerce op2 to FLOAT type
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
	
	/***
	 * Multiplies to evaluate operands
	 @param parser - information about  values being parsed
	 * @param op1 	 - first operand to be evaluated with op2
	 * @param op2	 - second operand to be evaluated with op1
	 * @return
	 */
	public static ResultValue multiply(Parser parser, ResultValue op1, ResultValue op2) {
		
		ResultValue res = new ResultValue();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				// TODO: coerce op2 to INTEGER type
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue * op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				// TODO: coerce op2 to FLOAT type
			}
			
			res.dataType = DataType.FLOAT;
			res.structure = Structure.PRIMITIVE;
			res.floatValue = op1.floatValue * op2.floatValue;
			
		} else {
			// TODO: handle str as first operand, etc
			throw new UnsupportedOperationError("First operand type cannot be used in addition.");
		}
		
		return res;
	}
	
	//divide function
	public static ResultValue divide(Parser parser, ResultValue op1, ResultValue op2) {
		
		ResultValue res = new ResultValue();
		
		DataType resultType = op1.dataType;
		
		if (resultType == DataType.INTEGER) {
			
			if (op2.dataType != DataType.INTEGER) {
				// TODO: coerce op2 to INTEGER type
			}
			
			res.dataType = DataType.INTEGER;
			res.structure = Structure.PRIMITIVE;
			res.intValue = op1.intValue / op2.intValue;
			
		} else if (resultType == DataType.FLOAT) {
			
			if (op2.dataType != DataType.FLOAT) {
				// TODO: coerce op2 to FLOAT type
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

	public static ResultValue unaryMinus(Parser parser, ResultValue op1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue unaryNot(Parser parser, ResultValue op1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue less(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue greater(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue lessEqual(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue greaterEqual(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue doubleEqual(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue notEqual(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue logicalAnd(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue logicalOr(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static ResultValue concatenate(Parser parser, ResultValue op1, ResultValue op2)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
