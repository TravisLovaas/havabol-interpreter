package havabol.error;

import havabol.lexer.Token;

public class UnsupportedOperationError extends Error{

	public UnsupportedOperationError(String message) {
		super(message);
	}
	
	public UnsupportedOperationError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	public UnsupportedOperationError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	public UnsupportedOperationError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}
}
