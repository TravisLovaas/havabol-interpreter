package havabol.error;

import havabol.lexer.Token;

public class UnsupportedOperationError extends Error{

	public UnsupportedOperationError(String message) {
		super(message);
	}
	
	/**	 
	 * Function: UnsupportedOperationError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param token:		token found where error is caused
	 */
	public UnsupportedOperationError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	/**	 
	 * Function: UnsupportedOperationError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 */
	public UnsupportedOperationError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	/**	 
	 * Function: UnsupportedOperationError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 * @param colPos: 		column number of source code where
	 * 						error may have occurred
	 */
	public UnsupportedOperationError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}
}
