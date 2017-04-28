package havabol.parser;

import havabol.storage.*;

import java.util.ArrayList;
import java.util.List;

import havabol.error.IndexError;
import havabol.error.TypeError;
import havabol.lexer.*;

/**
 * Represents a primitive value in Havabol
 */
public class Value {
	
	public DataType dataType;
	public Structure structure;
	public String terminatingStr;
	
	public String strValue;
	public int intValue;
	public double floatValue;
	public boolean booleanValue;
	public int year, month, day;

	public List<Value> arrayValue = new ArrayList<>();
	
	public int numItems = 0;

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
	
	/**
	 * Function:	toToken
	 * @param parser information about  values being parsed
	 * @return		 Value containing Token representation of token
	 */
	public Token toToken(Parser parser) {
		
		Token newToken = new Token();
		newToken.tempValue = this;
		newToken.isValueContainer = true;
		return newToken;
		
//		if (this.structure != Structure.PRIMITIVE) {
//			throw new TypeError("Cannot convert non primitive to token");
//		}
//		
//		Token patch = new Token(); 
//		patch.tokenStr = this.asString(parser).strValue;
//		patch.primClassif = Token.OPERAND;
//		switch(this.dataType){
//		case INTEGER:
//			patch.subClassif = Token.INTEGER;
//			break;
//		case STRING:
//			patch.subClassif = Token.STRING;
//			break;
//		case BOOLEAN:
//			patch.subClassif = Token.BOOLEAN;
//			break;
//		case FLOAT:
//			patch.subClassif = Token.FLOAT;
//			break;
//		default:
//			break;
//		}
//		
//		return patch;
	}
	
	/**
	 * Function: add
	 * Purpose:  Appends an element to this ResultValue array
	 * @param parser information about  values being parsed
	 * @param value ResultValue to append to this array
	 */
	public void add(Parser parser, Value value) {
		
		this.arrayValue.add(value);
		numItems++;
		
	}
	
	/**
	 * Splices the given string, replacing chars [beginIndex => endIndex] in the original
	 * string.
	 * @param parser Parser that called this method
	 * @param beginIndex Inclusive begin replacement index
	 * @param endIndex Exclusive end replacement index
	 * @param isEnded true if an end index was included in slice
	 * @param splice String to insert into the location
	 */
	public void spliceString(Parser parser, int beginIndex, int endIndex, boolean isEnded, String splice) {
		
		if (this.dataType != DataType.STRING) {
			throw new TypeError("Cannot slice character for non-string type");
		}
		
		if (!isEnded) {
			endIndex = this.strValue.length();
		}
		
		//System.out.println("slicing: " + this.strValue + " from " + beginIndex + " to " + endIndex);
		//System.out.println("inserting: " + splice);
		
		// Remove sliced value from string
		String sliced = this.strValue.substring(0, beginIndex) + splice + this.strValue.substring(endIndex, this.strValue.length());

		this.strValue = sliced;
		
	}
	
	/**
	 * Splices the given string, replacing the char at index in the original
	 * string.
	 * @param parser Parser that called this method
	 * @param index Index to replace
	 * @param splice String to insert into the location
	 */
	public void spliceString(Parser parser, int index, String splice) {
		
		if (this.dataType != DataType.STRING) {
			throw new TypeError("Cannot set character for non-string type");
		}
		
		StringBuilder newString = new StringBuilder(this.strValue.substring(0, index));
		
		newString.append(splice);
		
		if (index < this.strValue.length() - 1) {
			newString.append(this.strValue.substring(index + 1, this.strValue.length()));
		}
		
		this.strValue = newString.toString();
		
	}
	
	/**
	 * Function: 	asInteger
	 * @param parser information about  values being parsed
	 * @return		 Value containing integer representation of token
	 */
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
			try {
				res.intValue = Integer.parseInt(this.strValue);
			} catch (NumberFormatException e) {
				throw new TypeError("Invalid format of String to Int coercion", parser.scanner.currentToken);
			}		
		} else if (this.dataType == DataType.BOOLEAN) {
			// incompatible types
			throw new TypeError("Cannot coerce BOOLEAN type into INTEGER.");
		}
		
		return res;
		
	}
	
	/**
	 * Function:	asFloat
	 * @param parser information about  values being parsed
	 * @return		 Value containing float representation of token
	 */
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
			try {
				res.floatValue = Double.parseDouble(this.strValue);
			} catch (NumberFormatException e) {
				throw new TypeError("Invalid format of string to double coercion", parser.scanner.currentToken);
			}
		} else if (this.dataType == DataType.BOOLEAN) {
			// incompatible types
			throw new TypeError("Cannot coerce BOOLEAN type into FLOAT.");
		}
		
		return res;
		
	}
	
	/**
	 * Function: 	asString
	 * @param parser information about  values being parsed
	 * @return		 Value containing string representation of token
	 */
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
	
	/**
	 * Function:	asBoolean
	 * @param parser information about  values being parsed
	 * @return		 Value contain boolean values based on datatype
	 */
	public Value asBoolean(Parser parser) {
		
		if (this.dataType == DataType.BOOLEAN) {
			// nothing to do
			return this;
		}
		
		Value res = new Value();
		res.structure = Structure.PRIMITIVE;
		res.dataType = DataType.STRING;
		
		if (this.dataType == DataType.STRING) {
			if (this.strValue.equals("T")) {
				res.booleanValue = true;
			} else if (this.strValue.equals("F")) {
				res.booleanValue = false;
			} else {
				throw new TypeError("Invalid string value for coercion to Bool", parser.scanner.currentToken);
			}
		} else {
			throw new TypeError("Cannot coerce " + this.dataType + " to Bool", parser.scanner.currentToken);
		}
		
		return res;
		
	}
	
	public Value asDate(Parser parser){
		return new Value(null);
	}
	
	/**
	 * Function:	asType 	
	 * @param parser information about  values being parsed
	 * @param dataType
	 * @return
	 */
	public Value asType(Parser parser, DataType dataType) {
		switch (dataType) {
		case INTEGER:
			return this.asInteger(parser);
		case FLOAT:
			return this.asFloat(parser);
		case STRING:
		case DATE:
			return this.asString(parser);
		case BOOLEAN:
			return this.asBoolean(parser);
		default:
			throw new TypeError("Invalid type cast specified");
		}
	}
	
	/**
	 * Function:	toString
	 * Purpose:		returns string representation of dataType
	 */
	public String toString() {
		
		if (this.structure == Structure.PRIMITIVE) {
		
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
		
		} else if (this.structure == Structure.MULTIVALUE) {
			
			StringBuilder str = new StringBuilder("[");
			
			for (Value v : this.arrayValue) {
				str.append(v.toString());
				str.append(", ");
			}
			
			str.deleteCharAt(str.length() - 1);
			str.deleteCharAt(str.length() - 1);
			str.append("]");
			
			return str.toString();
			
		} else {
			
			return "[Empty value]";
			
		}
		
	}
	
	public Value clone() {
		Value val = new Value();
		val.dataType = this.dataType;
		val.structure = this.structure;
		val.intValue = this.intValue;
		val.strValue = this.strValue;
		val.floatValue = this.floatValue;
		val.booleanValue = this.booleanValue;
		val.terminatingStr = this.terminatingStr;
		return val;
	}

}
