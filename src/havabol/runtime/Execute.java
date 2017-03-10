package havabol.runtime;

import havabol.error.*;
import havabol.parser.*;
import havabol.storage.*;

public class Execute {
	
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

}
