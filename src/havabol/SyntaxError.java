package havabol;

public class SyntaxError extends Error {

	public SyntaxError(String message) {
		super(message);
	}
	
	public SyntaxError(String message, int lineNumber) {
		super(message + " (at line " + lineNumber + ")");
	}
	
	public SyntaxError(String message, int lineNumber, int colPos) {
		super(message + " (at line " + lineNumber + ", near column " + colPos + ")");
	}

}
