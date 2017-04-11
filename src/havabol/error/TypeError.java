package havabol.error;

import havabol.lexer.Token;

public class TypeError extends Error{

	public TypeError(String message) {
		super(message);
	}
	
	/***	 
	 * Function: TypeError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param token:		token found that caused error
	 */
	public TypeError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	/***	 
	 * Function: TypeError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 */
	public TypeError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	/***	 
	 * Function: TypeError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 * @param colPos: 		column number of source code where
	 * 						error may have occurred
	 */
	public TypeError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}
}
