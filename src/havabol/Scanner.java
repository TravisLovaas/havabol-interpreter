package havabol;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
	private boolean printedLast = false;
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
		
		getNext();
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
		 * 
		 * 
		 * Comment parsing
		 * ---------------------------
		 * If we find a "//":
		 * 		set commentFound = true
		 * 		set commentFoundOn = iSourceLineNr
		 * 		while iSourceLineNr == commentFoundOn:
		 * 			advanceCursor()
		 * 
		 * Handling escaped chars
		 * ---------------------------
		 * if find a "/" followed by a printable char,
		 * 		continue
		 * if the string contains non printable char,
		 * 		then call the hex printing method
		 */
		currentToken = nextToken.clone();
		nextToken = new Token();

		StringBuilder tokenStr = new StringBuilder();
		boolean isStringLiteral = false;
		boolean doubleOperator = false;
		commentFound = false;
		
		// Skip until we find something other than whitespace, comments, or we finish
		while ((iColPos >= textCharM.length || textCharM[iColPos] == '/'
				|| WHITESPACE.contains(Character.toString(textCharM[iColPos]))) && !done)
		{
			//System.out.println("SKIP");
			if ((iColPos >= textCharM.length || WHITESPACE.contains(Character.toString(textCharM[iColPos]))) && !done)
				advanceCursor();
			else if (textCharM[iColPos] == '/')
			{
				if(iColPos < (textCharM.length - 1) && textCharM[iColPos + 1] == '/'){
					commentFound = true; 
					commentFoundOn = iSourceLineNr;
					while (iSourceLineNr == commentFoundOn)
						advanceCursor();
				} else {
					throw new SyntaxError("Invalid char \' " + textCharM[iColPos] + " \' found", iSourceLineNr + 1, iColPos);
				}
			}
		}
		
		// Print any source lines we've read before we scan the next token
		while (!lineBuffer.isEmpty()) {
			System.out.println(lineBuffer.remove(0));
		}
		
		// If the done flag was set, there are no more tokens
		if (done) {
			if (printedLast) {
				currentToken.primClassif = Token.EOF;
				return "";
			} else {
				printedLast = true;
				return currentToken.tokenStr;
			}
		}
		
		
		//System.out.println("Found non-whitespace: " + textCharM[iColPos]);
		// Save the start position of this token in case of error
		nextToken.iColPos = iColPos;
		nextToken.iSourceLineNr = iSourceLineNr;
		debugColPos = iColPos;
		char currentChar = textCharM[iColPos];
		char [] retCharM = new char[textCharM.length];
		int iRet = 0;
		int i = 0;
		if (DELIMITERS.contains(Character.toString(currentChar))) {
			if (QUOTES.contains(Character.toString(currentChar))) {
				//System.out.println("Quote found.");
				char openStringChar = currentChar;
				boolean escapeNext = false;
				int openQuoteLineNr = iSourceLineNr;
				for (;;) {
					
					advanceCursor();	
					
					//if(currentToken.nonPrintable){
						//currentToken.nonPrintable = false;
						//continue;
					//}
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
							nextToken.nonPrintable = true;
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
				advanceCursor();

			} else {
				if(OPERATORS.contains(Character.toString(textCharM[iColPos])) && 
						OPERATORS.contains(Character.toString(textCharM[iColPos + 1])))
				{
					tokenStr.append(textCharM[iColPos]);
					tokenStr.append(textCharM[iColPos + 1]);
					advanceCursor();
				}else
					tokenStr.append(currentChar);
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
		
		classifyToken(nextToken, tokenStr.toString(), isStringLiteral);
		
		return tokenStr.toString();
		
	}

	public void setPosition(int iSourceLineNr, int iColPos) 
	{
		this.iSourceLineNr = iSourceLineNr;
		this.iColPos = iColPos;
	}
	
	/**
	 * Classifies a token and sets necessary token fields.
	 * @param token Token to populate
	 * @param tokenStr Token string
	 * @param isStringLiteral Identifies this token as a string literal
	 */
	public void classifyToken(Token token, String tokenStr, boolean isStringLiteral) throws SyntaxError {
		
		token.tokenStr = tokenStr;
		
		// Check if tokenStr is a data type
		switch (tokenStr) {
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
				token.primClassif = Token.OPERATOR;
				return;
		}
		
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
	
	public void bufferLine(int lineNumber) {
		lineBuffer.add("  " + (lineNumber + 1) + " " + sourceLineM.get(lineNumber));
	}
	
	/**
	 * Advances the scanner's cursor location by one, reading in new lines
	 * as necessary.
	 */
	public void advanceCursor() {
		iColPos += 1;
		//System.out.println("Line: " + (iSourceLineNr + 1) + " Col: " + iColPos);
		if (iColPos >= textCharM.length) {
			iColPos = 0;
			iSourceLineNr++;
			if (iSourceLineNr < sourceLineM.size()) {
				textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
				bufferLine(iSourceLineNr);
				printBuffer = true;
			} else {
				done = true;
			}
			//lineAdvanced = true;
		}
	}

}
