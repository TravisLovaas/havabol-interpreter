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
	// Comment found
	public boolean commentFound = false;
	public int commentFoundOn = 0;
	// Done scanning this file
	public boolean done = false;
	
	private final static String DELIMITERS = " \t;:()\'\"=!<>+-*/[]#^,\n"; // terminate a token
	private final static String WHITESPACE = " \t\n";
	private final static String QUOTES = "\"'";
	private final static String OPERATORS = "!=<>+-*/#^";
	private final static String SEPARATORS = ",;:[]()";
	private final static String ESCAPEPRINT ="\\'\"";
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

		StringBuilder tokenStr = new StringBuilder();
		currentToken = new Token();
		boolean isStringLiteral = false;
		boolean nonPrintable = false;
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
					if (done || (currentChar == openStringChar && !escapeNext)) {
						isStringLiteral = true;
						break;
					}
						
					if (currentChar == '\\' && !escapeNext) {
						escapeNext = true;
						if(ESCAPEPRINT.contains(Character.toString(textCharM[iColPos + 1])))
							continue;
						else
							nonPrintable = true;
						
					} else 
						escapeNext = false;
					tokenStr.append(currentChar);
				}
			
				if(nonPrintable)
					tokenStr = hexPrint(25, tokenStr.toString());
				advanceCursor();

			} else {
				//Double delimiter token found, else single delimiter token found
				if(OPERATORS.contains(Character.toString(textCharM[iColPos])) && 
						OPERATORS.contains(Character.toString(textCharM[iColPos + 1])))
				{
					tokenStr.append(textCharM[iColPos]);
					tokenStr.append(textCharM[iColPos + 1]);
					advanceCursor();
				}else
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
	
	/**
	 * This method prints Strings that contain non printable chars
	 * @param indent	- the number of spaces to indent second line
	 * @param str		- the string to be modified to print non
	 * 					  printable ASCII chars, including (\t, \n, & \a)
	 * @return			- returns StringBuilder containing modified String
	 * 					  that accommodates for non-printable ASCII chars
	 */
	public StringBuilder hexPrint(int indent, String str)
    {
        int len = str.length();	
        char [] charray = str.toCharArray();
        char ch;
		StringBuilder tokenStr = new StringBuilder();
        // print each character in the string
    	ArrayList<Character> chars = new ArrayList<Character>();
    	
    	//create new char array of corrected String showing hex values 
    	for (int i = 0; i < len; i++) {
    		//if we find escaped non-printable, change to hex
    		if(charray[i] == '\\'){
    			switch (charray[i+1]){
	        		case 'a':
	        			chars.add((char) 0x07);
	        			i++;
	        			break;
	        		case 'n':
	        			chars.add((char) 0x0A);
	        			i++;
	        			break;
	        		case 't':
	        			chars.add((char) 0x09);
	        			i++;
	        			break;
	        		default:
	        			System.out.println("Invalid ascii non printable character found: ' " + textCharM[iColPos - 1]
	        					+ textCharM[iColPos] + " '");
    			}
    		}else{
    			chars.add(charray[i]);
    		}
    	}

        for (int i = 0; i < chars.size(); i++)
        {
            ch = chars.get(i);
            if (ch > 31 && ch < 127)   // ASCII printable characters
                tokenStr.append(ch);
            else
                tokenStr.append(". ");
        }
        tokenStr.append("\n");
        // indent the second line to the number of specified spaces
        for (int i = 0; i < indent; i++)
        {
            tokenStr.append(" ");
        }
        // Non-printable characters will be shown
        // as their hex value.  Printable will simply be a space
        for (int i = 0; i < chars.size(); i++)
        {
            ch = chars.get(i);
            // only deal with the printable characters
            if (ch > 31 && ch < 127)   // ASCII printable characters
                tokenStr.append(" ");
            else
                tokenStr.append(String.format("%02X", (int) ch));
        }
        return tokenStr;
    }


	public void setPosition(int iSourceLineNr, int iColPos) 
	{
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
			case "else":
			case "while":
			case "for":
			case "select":
			case "when":
				token.primClassif = Token.CONTROL;
				token.subClassif = Token.FLOW;
				return;
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
				lineBuffer.add(iSourceLineNr + 1 + " " + sourceLineM.get(iSourceLineNr));
			} else {
				done = true;
			}
			//lineAdvanced = true;
		}
	}

}
