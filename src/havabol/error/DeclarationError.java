package havabol.error;

import havabol.lexer.Token;

public class DeclarationError extends Error{

	public DeclarationError(String message) {
		super(message);
	}
	
	public DeclarationError(String message, Token token) {
		super(message + " (found \"" + token.tokenStr + "\" + at line " + (token.iSourceLineNr + 1) + " near column " + (token.iColPos + 1) + ")");
	}
	
	public DeclarationError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	public DeclarationError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}
}
