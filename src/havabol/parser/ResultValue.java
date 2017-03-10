package havabol.parser;

import havabol.storage.*;
import havabol.lexer.*;
import havabol.runtime.Value;

public class ResultValue {
	
	public DataType dataType;
	public String strValue;
	public int intValue;
	public double floatValue;
	public boolean booleanValue;
	public Structure structure;
	public String terminatingStr;
	
	public static ResultValue tokenStrToResult(Parser parser, DataType dataType, String tokenStr) {
		ResultValue res = new ResultValue();
		
		
		return res;
	}

}
