package havabol.error;

import havabol.lexer.Token;

public class ArithmeticError extends Error {

	public ArithmeticError(String message) {
		super(message);
	}
	
	/**	 
	 * Function: ArithmeticError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	
	 */
	public ArithmeticError(String message, int lineNumber) {
		super("\n" + message + "\n(at line " + lineNumber + ")");
	}

}
