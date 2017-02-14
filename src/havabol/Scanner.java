package havabol;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Scanner {
	
	// Source file information
	public String sourceFileNm;
	public ArrayList<String> sourceLineM = new ArrayList<>();
	public SymbolTable symbolTable;
	
	// Holds source lines buffered for printing
	public ArrayList<String> lineBuffer = new ArrayList<>();
	
	// Scanner current line
	public char[] textCharM;
	// Source line number of our current cursor position, zero-indexed
	public int iSourceLineNr = 0;
	// Source character index of the current scanner line, zero-indexed
	public int iColPos = 0;
	public int debugColPos = 0;
	// Current and lookahead tokens
	public Token currentToken;
	public Token nextToken;
	// Done scanning this file
	public boolean done = false;
	
	private final static String DELIMITERS = " \t;:()\'\"=!<>+-*/[]#^,\n"; // terminate a token
	private final static String WHITESPACE = " \t\n";
	private final static String QUOTES = "\"'";
	private final static String OPERATORS = "!=<>+-*/#^";
	private final static String SEPARATORS = ",;:[]()";
	
	/**
	 * Reads a Havabol source file and initializes environment for scanning.
	 * @param sourceFileNm Havabol source file path
	 * @param symbolTable TODO: not yet implemented
	 * @throws IOException Exception encountered while reading file
	 * @throws FileNotFoundException Source file not found or inaccessible
	 */
	public Scanner(String sourceFileNm, SymbolTable symbolTable) throws IOException, FileNotFoundException {
		
		// Read all source lines into an ArrayList
		try (BufferedReader sourceIn = new BufferedReader(new FileReader(sourceFileNm))) {
			String line;
			while ((line = sourceIn.readLine()) != null) {
				sourceLineM.add(line);
			}
		}
		
		// Initialize scanning environment
		currentToken = new Token();
		nextToken = new Token();
		if (sourceLineM.size() > 0) {
			textCharM = sourceLineM.get(0).toCharArray();
			lineBuffer.add(iSourceLineNr + 1 + " " + sourceLineM.get(iSourceLineNr));
		} else {
			done = true;
		}
		
	}
	
	/**
	 * Retrieves and sets all information for the current token scanned.
	 * Returns the next token as a string, functionally.
	 * @return String representation of the next token
	 */
	public String getNext() throws SyntaxError {
		
		/* 
		 * Algorithmic sketch:
		 * 
		 * Advance iColPos and iSourceLineNr until we find a non-whitespace character
		 * If char is a delimiter:
		 * 		If char is quote:
		 * 			Until we find a non-escaped matching quote:
		 * 				Append chars
		 * 			Return a string literal token
		 * 		Otherwise:
		 * 			Append char
		 * 			Return token
		 * Otherwise:
		 * 		Until we find a delimiter:
		 * 			Append chars
		 * 		Return token
		 */
		
		//System.out.println("Scanning for token...");
		StringBuilder tokenStr = new StringBuilder();
		currentToken = new Token();
		boolean isStringLiteral = false;
		
		// Skip until we find something other than whitespace or we finish
		while ((iColPos >= textCharM.length || WHITESPACE.contains(Character.toString(textCharM[iColPos]))) && !done) {
			//System.out.println("SKIP");
			advanceCursor();
		}
		
		// Print any source lines we've read before we scan the next token
		while (!lineBuffer.isEmpty()) {
			System.out.println(lineBuffer.remove(0));
		}
		
		// If the done flag was set, there are no more tokens
		if (done) {
			currentToken.primClassif = Token.EOF;
			return "";
		}
		
		//System.out.println("Found non-whitespace: " + textCharM[iColPos]);
		// Save the start position of this token in case of error
		debugColPos = iColPos;
		char currentChar = textCharM[iColPos];
		
		if (DELIMITERS.contains(Character.toString(currentChar))) {
			if (QUOTES.contains(Character.toString(currentChar))) {
				//System.out.println("Quote found.");
				char openStringChar = currentChar;
				boolean escapeNext = false;
				int openQuoteLineNr = iSourceLineNr;
				
				for (;;) {
					
					advanceCursor();	
					
					if (iSourceLineNr != openQuoteLineNr) {
						// Quote literal must end on opening line
						throw new SyntaxError("String literal must begin and end on same line", openQuoteLineNr + 1);
					}
					
					currentChar = textCharM[iColPos];
					
					if (currentChar == openStringChar && !escapeNext) {
						isStringLiteral = true;
						break;
					}
					
					tokenStr.append(currentChar);
					if (currentChar == '\\' && !escapeNext) {
						escapeNext = true;
					} else {
						escapeNext = false;
					}
				} 
				advanceCursor();
				
			} else {
				//System.out.println("Single delimiter token found.");
				tokenStr.append(currentChar);
				//System.out.println("Appended: " + currentChar);
				advanceCursor();
			}
		} else {
			//System.out.println("Non-delimiter token found.");
			while (!DELIMITERS.contains(Character.toString(currentChar))) {
				tokenStr.append(currentChar);
				//System.out.println("Appended: " + currentChar);
				advanceCursor();
				currentChar = textCharM[iColPos];
			}
		}
		
		classifyToken(currentToken, tokenStr.toString(), isStringLiteral);
		
		return tokenStr.toString();
		
	}
	
	public void setPosition(int iSourceLineNr, int iColPos) {
		this.iSourceLineNr = iSourceLineNr;
		this.iColPos = iColPos;
	}
	
	/**
	 * Classifies a token and sets all necessary token fields.
	 * @param token Token to populate
	 * @param tokenStr Token string
	 * @param isStringLiteral Identifies this token as a string literal
	 */
	public void classifyToken(Token token, String tokenStr, boolean isStringLiteral) throws SyntaxError {
		
		token.tokenStr = tokenStr;
		
		if (OPERATORS.contains(tokenStr)) {
			token.primClassif = Token.OPERATOR;
		} else if (SEPARATORS.contains(tokenStr)) {
			token.primClassif = Token.SEPARATOR;
		} else {
			token.primClassif = Token.OPERAND;
			if (Character.isDigit(tokenStr.charAt(0))) {
				// Numeric literal
				if (tokenStr.contains(".")) {
					if (!tokenStr.matches("(\\d+\\.\\d*|\\d*\\.\\d+)")) {
						throw new SyntaxError("Invalid floating point literal", iSourceLineNr + 1, debugColPos + 1);
					}
					token.subClassif = Token.FLOAT;
				} else {
					if (!tokenStr.matches("\\d+")) {
						throw new SyntaxError("Invalid integer literal", iSourceLineNr + 1, debugColPos + 1);
					}
					token.subClassif = Token.INTEGER;
				}
			} else {
				if (isStringLiteral) {
					token.subClassif = Token.STRING;
				} else {
					token.subClassif = Token.IDENTIFIER;
				}
			}
		}
		
	}
	
	/**
	 * Advances the scanner's cursor location by one, reading in new lines
	 * as necessary.
	 */
	public void advanceCursor() {
		iColPos += 1;
		//System.out.println("Line: " + iSourceLineNr + " Col: " + iColPos);
		if (iColPos >= textCharM.length) {
			iColPos = 0;
			iSourceLineNr++;
			if (iSourceLineNr < sourceLineM.size()) {
				textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
				lineBuffer.add(iSourceLineNr + 1 + " " + sourceLineM.get(iSourceLineNr));
			} else {
				done = true;
			}
			//lineAdvanced = true;
		}
	}

}
