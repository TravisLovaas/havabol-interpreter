package havabol.lexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import havabol.error.SyntaxError;
import havabol.storage.SymbolTable;

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
	// Comment found
	public boolean commentFound = false;
	public int commentFoundOn = 0;
	// Done scanning this file
	public boolean done = false;
	public boolean unary = false;
	public boolean printBuffer = false;
	
	private final static String DELIMITERS = " \t;:()\'\"=!<>+-*/[]#^,\n"; // terminate a token
	private final static String WHITESPACE = " \t\n";
	private final static String QUOTES = "\"'";
	private final static String OPERATORS = "!=<>+-*/#^";
	private final static String SEPARATORS = ",;:[]()";
	private final static String ESCAPEPRINT ="\\'\"";
	private final static HashMap<Character, Character> escapeMap = new HashMap<Character, Character>(){
		private static final long serialVersionUID = 1L;

	{
		put('t', '\t');
		put('n', '\n');
		put('a', (char) 0x07);    
		}};
		
	private final static String[] WORD_OPERATORS = {"and", "or", "not", "in", "notin"};
	private final static String[] FLOW_OPERATORS = {"if", "endif", "else", "while", "endwhile", "for", "endfor"};
	private final static String[] DATA_TYPES = {"Int", "Float", "String", "Bool"};
	private final static List<String> UNARY = Arrays.asList("=", "+=", "-=", "+", "-", "*", "/", "^", "<", ">",
															"<=", ">=", "!=", "#", "and", "or", "u-", "(", ",",
															"!", "if", "select", "while", "when");
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
			bufferLine(0);
		} else {
			done = true;
		}

	}
	
	/**
	 * Reads and classifies the next token in the source file.
	 * Returns the next token as a string, functionally.
	 * @return String representation of the next token
	 */
	public String getNext() throws SyntaxError {
		
		String previous = currentToken.tokenStr;
		
		currentToken = getNextToken(false);
	
		if (currentToken.primClassif == Token.EOF) {
			return "";
		}
		nextToken = getNextToken(true);
		
		//Take care of unary minus
		if(currentToken.subClassif != Token.STRING && nextToken.subClassif != Token.STRING)
			if(UNARY.contains(previous) && currentToken.tokenStr.equals("-")){
				currentToken.tokenStr = "u-";
			}else
				unary = false;
			
		return currentToken.tokenStr;
		
	}
	
	/**
	 * Returns the next available token from the scanner's current internal
	 * cursor position.
	 * Pseudo:
	 	 * 	
		 * token refers to the next available token we will read
		 * delimiter refers to any character in above DELIMITERS list.
		 * 
		 * Preconditions:
		 * 		- Internal cursor is ON or BEFORE the beginning of our token.
		 * 
		 * Algorithm:
		 * 		If backtrack is true:
		 * 			Save internal cursor state
		 * 		Skip any and all whitespace and comments
		 * 		If our cursor is ON a delimiter:
		 * 			If delimiter is a quote:
		 * 				Read a string literal as our token
		 * 			Otherwise:
		 * 				Read one character into tokenStr
		 * 		Otherwise:
		 * 			Read characters into tokenStr until cursor is ON a delimiter.
		 * 		If backtrack is true:
		 * 			Restore internal cursor state
		 * 
		 * Postconditions:
		 * 		- If backtrack is false: internal cursor is STRICTLY AFTER the end of our token.
		 * 		- If backtrack is true: internal cursor is unchanged from its state before the call to this function.
		 *
	 * @param lookahead If true, save and restore scanner's internal cursor position
	 * @return The next available token
	 */
	private Token getNextToken(boolean lookahead) {
		
		int beforeSourceLineNr = -1;
		int beforeColPos = -1;
		
		if (lookahead) {
			beforeSourceLineNr = iSourceLineNr;
			beforeColPos = iColPos;
		}

		Token token = new Token();
		StringBuilder tokenStr = new StringBuilder();
		boolean isStringLiteral = false;
		commentFound = false;
		
		// Skip until we find something other than whitespace, comments, or we finish
		while ((iColPos >= textCharM.length || textCharM[iColPos] == '/'
				|| WHITESPACE.contains(Character.toString(textCharM[iColPos]))) && !done)
		{
			//System.out.println("SKIP");
			if ((iColPos >= textCharM.length || WHITESPACE.contains(Character.toString(textCharM[iColPos]))) && !done)
				advanceCursor(!lookahead);
			else if (textCharM[iColPos] == '/')
			{
				if(iColPos < (textCharM.length - 1) && textCharM[iColPos + 1] == '/'){
					commentFound = true; 
					commentFoundOn = iSourceLineNr;
					while (iSourceLineNr == commentFoundOn)
						advanceCursor(!lookahead);
				} else {
					throw new SyntaxError("Invalid char \' " + textCharM[iColPos] + " \' found", iSourceLineNr + 1, iColPos);
				}
			}
		}
		
		// If the done flag was set, there are no more tokens
		if (done) {
			token.primClassif = Token.EOF;
			return token;
		}
		
		//System.out.println("Found non-whitespace: " + textCharM[iColPos]);
		// Save the start position of this token in case of error
		token.iColPos = iColPos;
		token.iSourceLineNr = iSourceLineNr;
		debugColPos = iColPos;
		char currentChar = textCharM[iColPos];
		char [] retCharM = new char[textCharM.length];
		int iRet = 0;
		
	
		if (DELIMITERS.contains(Character.toString(currentChar))) {
			if (QUOTES.contains(Character.toString(currentChar))) {
				//System.out.println("Quote found.");
				char openStringChar = currentChar;
				boolean escapeNext = false;
				int openQuoteLineNr = iSourceLineNr;
				for (;;) {
					
					advanceCursor(!lookahead);	
					if (iSourceLineNr != openQuoteLineNr) {
						// Quote literal must end on opening line
						throw new SyntaxError("String literal must begin and end on same line", openQuoteLineNr + 1);
					}
					
					currentChar = textCharM[iColPos];
					if (done || (currentChar == openStringChar && !escapeNext)) {
						isStringLiteral = true;
						break;
					}

					if (currentChar == '\\' && !escapeNext) {
						escapeNext = true;
						if(ESCAPEPRINT.contains(Character.toString(textCharM[iColPos + 1])))
							continue;
						else{
							token.nonPrintable = true;
							if (escapeMap.containsKey(textCharM[iColPos + 1])){
								retCharM[iRet++] = escapeMap.get(textCharM[iColPos + 1]);
								continue;
							}
						}
					} else 
						escapeNext = false;
					if(textCharM[iColPos - 1] != '\\')
						retCharM[iRet++] = textCharM[iColPos];
				}
			
				tokenStr =  tokenStr.insert(0, retCharM, 0, iRet);
				tokenStr.delete(iRet, tokenStr.length() + 1);
				advanceCursor(!lookahead);
			} else {
				if(OPERATORS.contains(Character.toString(textCharM[iColPos])) && 
						textCharM[iColPos + 1] == '=')
				{
					tokenStr.append(textCharM[iColPos]);
					tokenStr.append(textCharM[iColPos + 1]);
					advanceCursor(!lookahead);
				}else
					tokenStr.append(currentChar);
				advanceCursor(!lookahead);
			}
		} else {
			while (!DELIMITERS.contains(Character.toString(currentChar))) {
				tokenStr.append(currentChar);
				advanceCursor(!lookahead);
				currentChar = textCharM[iColPos];
			}
		}

		token.tokenStr = tokenStr.toString();
		classifyToken(token, isStringLiteral);
		if (lookahead) {
			iSourceLineNr = beforeSourceLineNr;
			iColPos = beforeColPos;
			textCharM = sourceLineM.get(beforeSourceLineNr).toCharArray();
		}
		
		return token;
		
	}

	/**
	 * Sets the cursor position of this scanner
	 * @param iSourceLineNr Zero-based line number to set cursor at
	 * @param iColPos Zero-based column to set cursor at
	 */
	public void setPosition(int iSourceLineNr, int iColPos) 
	{
		this.iSourceLineNr = iSourceLineNr;
		this.iColPos = iColPos;
	}
	
	/**
	 * Classifies a token and sets necessary token fields.
	 * @param token Token to populate
	 * @param isStringLiteral Identifies this token as a string literal
	 */
	public void classifyToken(Token token, boolean isStringLiteral) throws SyntaxError {
		
		// Check if tokenStr is a data type
		switch (token.tokenStr) {
			case "Int":
			case "Float":
			case "String":
			case "Bool":
				token.primClassif = Token.CONTROL;
				token.subClassif = Token.DECLARE;
				return;
			case "if":
			case "while":
			case "for":
			case "select":
			case "when":
				token.primClassif = Token.CONTROL;
				token.subClassif = Token.FLOW;
				return;
			case "else":
			case "endif":
			case "endwhile":
			case "endfor":
				token.primClassif = Token.CONTROL;
				token.subClassif = Token.END;
				return;
			case "and":
			case "or":
			case "not":
				token.primClassif = Token.OPERATOR;
				return;
			case "in":
			case "notin":
				token.primClassif = Token.OPERATOR;
				return;
			case "T":
			case "F":
				token.primClassif = Token.OPERAND;
				token.subClassif = Token.BOOLEAN;
				return;
			// Built-in functions
			case "print":
			case "LENGTH":
			case "SPACES":
			case "MAXLENGTH":
			case "ELEM":
			case "MAXELEM":
				token.primClassif = Token.FUNCTION;
				token.subClassif = Token.BUILTIN;
				return;
			// Two-char operators
			case ">=":
			case "<=":
			case "==":
			case "!=":
			case "u-":
				token.primClassif = Token.OPERATOR;
				return;
		}
		
		if (OPERATORS.contains(token.tokenStr)) {
			token.primClassif = Token.OPERATOR;
		} else if (SEPARATORS.contains(token.tokenStr)) {
			token.primClassif = Token.SEPARATOR;
		} else {
			token.primClassif = Token.OPERAND;
			if (Character.isDigit(token.tokenStr.charAt(0))) {
				// Numeric literal
				if (token.tokenStr.contains(".")) {
					if (!token.tokenStr.matches("(\\d+\\.\\d*|\\d*\\.\\d+)")) {
						throw new SyntaxError("Invalid floating point literal", iSourceLineNr + 1, debugColPos + 1);
					}
					token.subClassif = Token.FLOAT;
				} else {
					if (!token.tokenStr.matches("\\d+")) {
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
	 * Adds the given source code line to the line print buffer
	 * @param lineNumber The 0-based line number of the line to add to the buffer
	 */
	public void bufferLine(int lineNumber) {
		lineBuffer.add("  " + (lineNumber + 1) + " " + sourceLineM.get(lineNumber));
	}
	
	/**
	 * Advances the scanner's cursor location by one, reading in new lines
	 * as necessary.
	 */
	public void advanceCursor(boolean print) {
		iColPos += 1;
		//if(iColPos < textCharM.length)
		//System.out.println("Line: " + (iSourceLineNr) + " Col: " + (iColPos - 1) + " Char: " + textCharM[iColPos - 1]);
		if (iColPos >= textCharM.length) {
			iColPos = 0;
			iSourceLineNr++;
			if (iSourceLineNr < sourceLineM.size()) {
				textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
			} else {
				done = true;
			}
		}
	}

}

