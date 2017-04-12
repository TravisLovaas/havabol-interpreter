package havabol.runtime;

import java.util.List;
import havabol.error.*;
import havabol.lexer.Token;
import havabol.parser.*;
import havabol.storage.*;

public class Functions {
	
	/**
	 * Function: print
	 * Purpose: 		Print values in source code print function
	 * @param parser	information about  values being parsed
	 * @param args		arguments passed to be printed
	 * @return			Value type of print function (void)
	 */
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
	 * Function: 		length
	 * Purpose:  		Havabol LENGTH implementation. Returns the length of a given string
	 * @param parser 	information about  values being parsed
	 * @param string 	the STIdentifier containing string to be processed
	 * @return 			Value with the length of a string
	 */
	public static Value length(Parser parser, STIdentifier string){
		return new Value(string.getValue().strValue.length());
	}
	
	/**
	 * Function: 		spaces
	 * Purpose:  		Havabol SPACES implementation.
	 * @param parser 	information about  values being parsed
	 * @param string 	the STIdentifier containing string to be processed
	 * @return 			Value with the boolean T/F if string contains spaces(T),
	 * 		   			is empty (T), or doesn't contain spaces (F) 
	 */
	public static Value spaces(Parser parser, STIdentifier string){
		char[] check = string.getValue().asString(parser).strValue.toCharArray();
		
		if(string.getValue().strValue.isEmpty()){
			return new Value(true);
		}
		for (char c : check) {
			if (c == ' ') {
				return new Value(true);
			}
		}
		
		return new Value(false);
		
	}

	/**
	 * Function:		elem
	 * Purpose:			finds maximum 
	 * @param parser	information about  values being parsed
	 * @param array		the STIdentifier containing array to be parsed
	 * @return			Value containing the number of populated elements
	 * 					in an array
	 */
	public static Value elem(Parser parser, STIdentifier array) {
		
		if (array.structure != Structure.FIXED_ARRAY
			&& array.structure != Structure.UNBOUNDED_ARRAY) {
			
			throw new TypeError("Invalid args to elem(), argument must be array variable");
			
		}
		
		int highestIndex = 0;
		
		for (int i = 0; i < array.declaredSize; i++) {
			if (array.arrayValue[i] != null)
				highestIndex = i;
		}
		
		return new Value(highestIndex);
		
//		if (value.structure == Structure.PRIMITIVE) {
//			throw new TypeError("ELEM may only operate on array-like values.");
//		}
		
	}
	/**
	 * maxElem returns the declared length of an array
	 * @param array
	 * @return
	 */
	/**
	 * Function: maxElem
	 * @param parser: 
	 * @param array
	 * @return
	 */
	public static Value maxElem(Parser parser, STIdentifier array) {
		
		return new Value(array.declaredSize);
		
//		if (value.structure == Structure.PRIMITIVE) {
//			throw new TypeError("MAXELEM may only operate on array-like values.");
//		}
		
	}

}
