package havabol.runtime;

import java.util.List;
import havabol.error.*;
import havabol.lexer.Token;
import havabol.parser.*;
import havabol.storage.*;

public class Functions {
	
	public static Value print(Parser parser, List<Value> args) {
		if (args.size() == 0) {
			return new Value().asVoid();
		}

		System.out.print(args.get(0).asString(parser).strValue);
		
		for (int i = 1; i < args.size(); i++) {
			System.out.print(" ");
			System.out.print(args.get(i).asString(parser).strValue);
		}
		
		System.out.println();
		
		return new Value().asVoid();
	}
	
	/**
	 * Havabol LENGTH implementation. Returns the length of a given
	 * string value.
	 * @param string
	 * @return int
	 */
	public static Value length(Parser parser, Value value){
		return new Value(value.asString(parser).strValue.length());
	}
	/**
	 * Havabol SPACES returns true if there are any spaces in the string,
	 * or false if there are none
	 * @param string
	 * @return boolean
	 */
	public static Value spaces(Parser parser, Value value){
		char[] check = value.asString(parser).strValue.toCharArray();
		
		for (char c : check) {
			if (c == ' ') {
				return new Value(true);
			}
		}
		
		return new Value(false);
		
	}
	/**
	 * ELEM returns the max populated element in the array. 
	 * @param array
	 * @return String
	 */
	public static Value elem(Parser parser, STIdentifier array) {
		
		if (array.structure != Structure.FIXED_ARRAY
			&& array.structure != Structure.UNBOUNDED_ARRAY) {
			
			throw new TypeError("Invalid args to elem(), argument must be array variable");
			
		}
		
		return new Value(array.arrayValue.numItems);
		
//		if (value.structure == Structure.PRIMITIVE) {
//			throw new TypeError("ELEM may only operate on array-like values.");
//		}
		
	}
	/**
	 * maxElem returns the declared length of an array
	 * @param array
	 * @return
	 */
	public static Value maxElem(Parser parser, Value value) {
		
		throw new UnsupportedOperationError("maxelem is not yet implemented");
		
//		if (value.structure == Structure.PRIMITIVE) {
//			throw new TypeError("MAXELEM may only operate on array-like values.");
//		}
		
	}

}
