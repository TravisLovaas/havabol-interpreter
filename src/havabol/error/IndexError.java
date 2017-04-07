package havabol.error;

import havabol.lexer.Token;

public class IndexError extends Error {

	public IndexError(String message) {
		super(message);
	}
	
	public IndexError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	public IndexError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	public IndexError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}

}
