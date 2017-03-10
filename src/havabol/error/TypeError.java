package havabol.error;

import havabol.lexer.Token;

public class TypeError extends Error{

	public TypeError(String message) {
		super(message);
	}
	
	public TypeError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	public TypeError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	public TypeError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}
}
