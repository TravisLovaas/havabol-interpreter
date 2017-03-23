package havabol.parser;

import havabol.storage.*;
import havabol.error.TypeError;
import havabol.lexer.*;

public class ResultValue {
	
	public DataType dataType;
	public String strValue;
	public int intValue;
	public double floatValue;
	public boolean booleanValue;
	public Structure structure;
	public String terminatingStr;
	
	public ResultValue asInteger(Parser parser) {
		
		if (this.dataType == DataType.INTEGER) {
			// nothing to do
			return this;
		}
		
		ResultValue res = new ResultValue();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.INTEGER;
		
		if (this.dataType == DataType.FLOAT) {
			res.intValue = (int) this.floatValue;
		} else if (this.dataType == DataType.STRING) {
			res.intValue = Integer.parseInt(this.strValue);
		} else if (this.dataType == DataType.BOOLEAN) {
			// incompatible types
			throw new TypeError("Cannot coerce BOOLEAN type into INTEGER.");
		}
		
		return res;
		
	}
	
	public ResultValue asFloat(Parser parser) {
		
		if (this.dataType == DataType.FLOAT) {
			// nothing to do
			return this;
		}
		
		ResultValue res = new ResultValue();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.INTEGER;
		
		if (this.dataType == DataType.INTEGER) {
			res.floatValue = (double) this.intValue;
		} else if (this.dataType == DataType.STRING) {
			res.floatValue = Double.parseDouble(this.strValue);
		} else if (this.dataType == DataType.BOOLEAN) {
			// incompatible types
			throw new TypeError("Cannot coerce BOOLEAN type into FLOAT.");
		}
		
		return res;
		
	}
	
	public ResultValue asString(Parser parser) {
		
		if (this.dataType == DataType.STRING) {
			// nothing to do
			return this;
		}
		
		ResultValue res = new ResultValue();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.STRING;
		
		if (this.dataType == DataType.INTEGER) {
			this.strValue = String.valueOf(this.intValue);
		} else if (this.dataType == DataType.FLOAT) {
			this.strValue = String.valueOf(this.floatValue);
		} else if (this.dataType == DataType.BOOLEAN) {
			if (this.booleanValue) {
				this.strValue = "T";
			} else {
				this.strValue = "F";
			}
		}
		
		return res;
		
	}
	
	public ResultValue asBoolean(Parser parser) {
		
		if (this.dataType == DataType.BOOLEAN) {
			// nothing to do
			return this;
		}
		
		ResultValue res = new ResultValue();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.STRING;
		
		if (this.dataType == DataType.INTEGER) {
			if (this.intValue == 0) {
				res.booleanValue = false;
			} else {
				res.booleanValue = true;
			}
		} else if (this.dataType == DataType.FLOAT) {
			if (this.floatValue == 0) {
				res.booleanValue = false;
			} else {
				res.booleanValue = true;
			}
		} else if (this.dataType == DataType.STRING) {
			if (this.strValue.isEmpty() || this.strValue.equals("F")) {
				res.booleanValue = false;
			} else {
				res.booleanValue = true;
			}
		}
		
		return res;
		
	}
	
	public ResultValue asType(Parser parser, DataType dataType) {
		switch (dataType) {
		case INTEGER:
			return this.asInteger(parser);
		case FLOAT:
			return this.asFloat(parser);
		case STRING:
			return this.asString(parser);
		case BOOLEAN:
			return this.asBoolean(parser);
		default:
			throw new TypeError("Invalid type cast specified");
		}
	}

}
