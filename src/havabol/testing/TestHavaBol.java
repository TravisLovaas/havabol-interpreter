/*
  This is a simple driver for the first programming assignment.
  Command Arguments:
      java HavaBol arg1
             arg1 is the havabol source file name.
  Output:
      Prints each token in a table.
  Notes:
      1. This creates a SymbolTable object which doesn't do anything
         for this first programming assignment.
      2. This uses the student's Scanner class to get each token from
         the input file.  It uses the getNext method until it returns
         an empty string.
      3. If the Scanner raises an exception, this driver prints 
         information about the exception and terminates.
      4. The token is printed using the Token::printToken() method.
 */
package havabol.testing;

import java.io.File;
import java.io.FilenameFilter;

import havabol.lexer.Scanner;
import havabol.lexer.SymbolTable;

public class TestHavaBol 
{
    public static void main(String[] args) 
    {
        
    	File sourceDir = new File(".");
    	
    	// Find all input files
    	File[] inputFiles = sourceDir.listFiles(new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			return name.matches("^.*Input\\.txt$");
    		}
    	});
    	
    	assert inputFiles.length > 0;
    	
    	boolean testSuccessful = false;
    	for (File f : inputFiles) {
    		System.out.println("===========================================");
    		System.out.println("Testing: " + f.getName());
    		System.out.println(f.getAbsolutePath());
    		System.out.println("===========================================");
    		
    		testSuccessful = testSourceFile(f.getAbsolutePath());
    		assert testSuccessful;
    	}
        
    }
    
    public static boolean testSourceFile(String sourceFile) {
    	
    	
    	SymbolTable symbolTable = new SymbolTable();
    	try
        {
            // Print a column heading 
            System.out.printf("%-11s %-12s %s\n"
                    , "primClassif"
                    , "subClassif"
                    , "tokenStr");
            
            Scanner scan = new Scanner(sourceFile, symbolTable);
            while (! scan.getNext().isEmpty())
            {
                scan.currentToken.printToken();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    	
    	return true;
    	
    }
    
}