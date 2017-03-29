package havabol.runtime;

import java.util.List;
import havabol.error.*;
import havabol.lexer.Token;
import havabol.parser.*;
import havabol.storage.*;

public class Functions {
	
	public static void print(Parser parser, List<ResultValue> args) {
		if (args.size() == 0) {
			return;
		}

		System.out.print(args.get(0).asString(parser).strValue);
		
		for (int i = 1; i < args.size(); i++) {
			System.out.print(" ");
			System.out.print(args.get(i).asString(parser).strValue);
		}
		
		System.out.println();
	}
	/**
	 * LENGTH gets the length of the string
	 * @param string
	 * @return int
	 */
	public int LENGTH(String string){
		return string.length();
	}
	/**
	 * SPACES returns T if there are any spaces in the string,
	 * and F is there is at least one
	 * @param string
	 * @return boolean
	 */
	public boolean SPACES(String string){
		int i;
		for(i=0; i<string.length(); i++){
			if(string.substring(i).compareTo(" ") == 0)
				return true;
		}
		return false;
	}
	/**
	 * ELEM returns the max populated element in the array. 
	 * @param array
	 * @return String
	 */
	public String ELEM(String[] array){
		int max = 0;
		int i;
		for(i=0; i<array.length; i++){
			if(array[i] != null)
				max = i;
		}
		return array[max+1]; 
	}
	/**
	 * MAXELEM return the length of the array.
	 * @param array
	 * @return
	 */
	public int MAXELEM(String[] array){
		return array.length;
	}

}
