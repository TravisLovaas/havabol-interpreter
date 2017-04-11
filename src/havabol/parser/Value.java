package havabol.parser;

import havabol.storage.*;

import java.util.List;

import havabol.error.IndexError;
import havabol.error.TypeError;
import havabol.lexer.*;

/**
 * Represents a primitive value in Havabol
 */
public class Value {
	
	public DataType dataType;
	public String strValue;
	public int intValue;
	public double floatValue;
	public boolean booleanValue;
	
	public Structure structure;
	public String terminatingStr;
	
	public Value() {
		this.dataType = DataType.VOID;
		this.structure = Structure.VOID;
	}
	
	public Value(int intValue) {
		this.dataType = DataType.INTEGER;
		this.structure = Structure.PRIMITIVE;
		this.intValue = intValue;
	}
	
	public Value(double floatValue) {
		this.dataType = DataType.FLOAT;
		this.structure = Structure.PRIMITIVE;
		this.floatValue = floatValue;
	}
	
	public Value(String strValue) {
		this.dataType = DataType.STRING;
		this.structure = Structure.PRIMITIVE;
		this.strValue = strValue;
	}
	
	public Value(boolean booleanValue) {
		this.dataType = DataType.BOOLEAN;
		this.structure = Structure.PRIMITIVE;
		this.booleanValue = booleanValue;
	}
	
	public Value asVoid() {
		this.dataType = DataType.VOID;
		this.structure = Structure.VOID;
		return this;
	}
	
	public Token toToken(Parser parser) {
		
		if (this.structure != Structure.PRIMITIVE) {
			throw new TypeError("Cannot convert non primitive to token");
		}
		
		Token patch = new Token(); 
		patch.tokenStr = this.asString(parser).strValue;
		patch.primClassif = Token.OPERAND;
		switch(this.dataType){
		case INTEGER:
			patch.subClassif = Token.INTEGER;
			break;
		case STRING:
			patch.subClassif = Token.STRING;
			break;
		case BOOLEAN:
			patch.subClassif = Token.BOOLEAN;
			break;
		case FLOAT:
			patch.subClassif = Token.FLOAT;
			break;
		}
		
		return patch;
	}
	
	public Value asInteger(Parser parser) {
		
		if (this.dataType == DataType.INTEGER) {
			// nothing to do
			return this;
		}
		
		Value res = new Value();
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
	
	public Value asFloat(Parser parser) {
		
		if (this.dataType == DataType.FLOAT) {
			// nothing to do
			return this;
		}
		
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.FLOAT;
		
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
	
	public Value asString(Parser parser) {
		
		if (this.dataType == DataType.STRING) {
			// nothing to do
			return this;
		}
		
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.STRING;
		
		if (this.dataType == DataType.INTEGER) {
			res.strValue = String.valueOf(this.intValue);
		} else if (this.dataType == DataType.FLOAT) {
			res.strValue = String.valueOf(this.floatValue);
		} else if (this.dataType == DataType.BOOLEAN) {
			if (this.booleanValue) {
				res.strValue = "T";
			} else {
				res.strValue = "F";
			}
		}
		
		return res;
		
	}
	
	public Value asBoolean(Parser parser) {
		
		if (this.dataType == DataType.BOOLEAN) {
			// nothing to do
			return this;
		}
		
		Value res = new Value();
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
	
	public Value asType(Parser parser, DataType dataType) {
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
	
	public String toString() {
		
		String valStr = "null";
		
		switch (dataType) {
		case INTEGER:
			valStr = String.valueOf(this.intValue);
			break;
		case FLOAT:
			valStr = String.valueOf(this.floatValue);
			break;
		case STRING:
			valStr = strValue;
			break;
		case BOOLEAN:
			valStr = String.valueOf(booleanValue);
			break;
		default:
			break;
		}
		
		return "[" + this.dataType + ": " + valStr + "]";
		
	}

}
