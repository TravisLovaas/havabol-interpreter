package havabol.error;

import havabol.lexer.Token;

public class IndexError extends Error {

	public IndexError(String message) {
		super(message);
	}
	
	/**
	 * Function: IndexError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param token:		token found that caused error 
	 */
	public IndexError(String message, Token token) {
		super("\n" + message + "\n(found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	/**	 
	 * Function: IndexError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 */
	public IndexError(String message, int lineNumber) {
		super("\n" + message + "\n(at line " + lineNumber + ")");
	}
	

	/**	 
	 * Function: IndexError
	 * Purpose:				Populates error message
	 * @param message: 		message to be displayed
	 * @param lineNumber: 	line number of source code where
	 * 					  	error may have occurred
	 * @param colPos: 		column number of source code where
	 * 						error may have occurred
	 */
	public IndexError(String message, int lineNumber, int colPos) {
		super("\n" + message + "\n(at line " + lineNumber + ", near column " + colPos + ")");
	}

}
