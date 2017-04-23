package havabol.runtime;

import java.util.*;
import havabol.error.*;
import havabol.parser.*;
import havabol.storage.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;


public class Functions {
	
	/**
	 * Function: print
	 * Purpose: 		Print values in source code print function
	 * @param parser	information about  values being parsed
	 * @param args		arguments passed to be printed
	 * @return			Value type of print function (void)
	 */
	
	
	public static Value print(Parser parser, List<Value> args) {
		if (args.size() == 0) {
			return new Value().asVoid();
		}

		System.out.print(args.get(0).asString(parser).strValue);
		
		for (int i = 1; i < args.size(); i++) {
			System.out.print(" ");
			System.out.print(args.get(i).asString(parser).strValue);
		}
		
		System.out.println();
		
		return new Value().asVoid();
	}
	
	
	/**
	 * Function: 		length
	 * Purpose:  		Havabol LENGTH implementation. Returns the length of a given string
	 * @param parser 	information about  values being parsed
	 * @param string 	the STIdentifier containing string to be processed
	 * @return 			Value with the length of a string
	 */
	public static Value length(Parser parser, Value string){
		return new Value(string.asString(parser).strValue.length());
	}
	
	/**
	 * Function: 		spaces
	 * Purpose:  		Havabol SPACES implementation.
	 * @param parser 	information about  values being parsed
	 * @param string 	the STIdentifier containing string to be processed
	 * @return 			Value with the boolean T/F if string contains spaces(T),
	 * 		   			is empty (T), or doesn't contain spaces (F) 
	 */
	public static Value spaces(Parser parser, Value string){
		char[] check = string.asString(parser).strValue.toCharArray();
		
		if(string.strValue.isEmpty()){
			return new Value(true);
		}
		for (char c : check) {
			if (c == ' ') {
				return new Value(true);
			}
		}
		
		return new Value(false);
		
	}

	/**
	 * Function:		elem
	 * Purpose:			finds maximum populated index + 1
	 * @param parser	information about  values being parsed
	 * @param array		the STIdentifier containing array to be parsed
	 * @return			Value containing the number of populated elements
	 * 					in an array
	 */
	public static Value elem(Parser parser, STIdentifier array) {
		
		if (array.structure != StorageStructure.FIXED_ARRAY
			&& array.structure != StorageStructure.UNBOUNDED_ARRAY) {
			
			throw new TypeError("Invalid args to elem(), argument must be array variable");
			
		}
		
		int highestIndex = 0;
		
		for (int i = 0; i < array.declaredSize; i++) {
			if (array.arrayValue[i] != null)
				highestIndex = i;
		}
		
		if(highestIndex == 0)
			return new Value(highestIndex);
		else
			return new Value(highestIndex + 1);

	}
	
	
	/**
	 * Function: maxElem
	 * Purpose:			finds maximum declared size
	 * @param parser: 	information about  values being parsed
	 * @param array		the STIdentifier containing array to be parsed
	 * @return 			Value containing the declared size of an array
	 */
	public static Value maxElem(Parser parser, STIdentifier array) {
		
		return new Value(array.declaredSize);
		
//		if (value.structure == Structure.PRIMITIVE) {
//			throw new TypeError("MAXELEM may only operate on array-like values.");
//		}
		
	}


	public static Value dateDiff(Parser parser, Value dateVal1, Value dateVal2)
	{
		// TODO Auto-generated method stub
		int iJulian1, iJulian2;
	    if (validateDate(dateVal1) != 0)
	       throw new SyntaxError("Invalid 1st date for dateDiff: ", dateVal1.toToken(parser));

	    if (validateDate(dateVal2) != 0)
		       throw new SyntaxError("Invalid 2nd date for dateDiff: ", dateVal2.toToken(parser));

	    iJulian1 = DateToJulian(dateVal1);
	    iJulian2 = DateToJulian(dateVal2);

	    return new Value(iJulian1 - iJulian2);
	}
	

	public static Value dateAdj(Parser parser, Value dateVal, Value days)
	{
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// Unlike HavaBol, the GregorianCalendar constructor's month is relative to zero
		// not 01.  (e.g., February is 01).
		 if (validateDate(dateVal) != 0)
			 throw new SyntaxError("Invalid 1st date for dateDiff: ", dateVal.toToken(parser));

		Calendar calendar = new GregorianCalendar(dateVal.year, (dateVal.month - 1), dateVal.day);
		//System.out.println("Date : " + sdf.format(calendar.getTime()));
		 
		//subtract 10 days
		calendar.add(Calendar.DAY_OF_MONTH, days.intValue);
		//System.out.println("Date : " + sdf.format(calendar.getTime()));

		return new Value(sdf.format(calendar.getTime()));
	}


	public static Value dateAge(Parser parser, Value dateVal1, Value dateVal2)
	{
		// TODO Auto-generated method stub
		 if (validateDate(dateVal1) != 0)
	       throw new SyntaxError("Invalid 1st date for dateDiff: ", dateVal1.toToken(parser));

	    if (validateDate(dateVal2) != 0)
		       throw new SyntaxError("Invalid 2nd date for dateDiff: ", dateVal2.toToken(parser));
		
	    LocalDate date1 = LocalDate.of(dateVal1.year, dateVal1.month, dateVal1.day);
		LocalDate date2=  LocalDate.of(dateVal2.year, dateVal2.month, dateVal2.day);
		return new Value(Math.abs(Period.between(date1, date2).getYears()));
	}
	
	private static int DateToJulian(Value dateVal)
	{
		// TODO Auto-generated method stub
	
	    int iCountDays;
	    // Calculate number of days since 0000-03-01

	    // If month is March or greater, decrease it by 3.
	    if (dateVal.month > 2)
	        dateVal.month -= 3;
	    else
	    {
	        dateVal.month += 9;  // adjust the month since we begin with March
	        dateVal.year--;      // subtract 1 from year if the month was Jan or Feb
	    }
	    iCountDays = 365 * dateVal.year                    // 365 days in a year
	        + dateVal.year / 4 - dateVal.year / 100 + dateVal.year / 400   // add a day for each leap year
	        + (dateVal.month * 306 + 5) / 10               // see note 4
	        + (dateVal.day);                              // add the days
	    return iCountDays;
		
	}


	private static int validateDate(Value dateVal)
	{
		int iDaysPerMonth[] = 
		       { 0, 31, 29, 31
	           , 30, 31, 30
	           , 31, 31, 30
	           , 31, 30, 31 };

	    // Check for too few characters for the yyyy-mm-dd format
	    if (dateVal.strValue.length() != 10)
	        return 4;  // invalid format due to length
	    
	    String parts[] = dateVal.strValue.split("-");
	    // The year should be 4 characters 
	    if (parts[0].length() != 4)
	        return 1;  // invalid year
	    
	    //if there are less than 3 parts to our date, 
	    //we have an error
	    if(parts.length < 3){
	    	return 4; //invalid date format
	    }
	    
	    // pluck out the year, month, and day
	    dateVal.year = Integer.parseInt(parts[0]);
	    dateVal.month = Integer.parseInt(parts[1]);
	    dateVal.day = Integer.parseInt(parts[2]);
	 
	    // Validate Month
	    if (dateVal.month < 1 || dateVal.month > 12)
	        return 2;  // month invalid
	    
	    // Validate day based on max days per month 
	    if (dateVal.day < 1 || dateVal.day > iDaysPerMonth[dateVal.month])
	        return 3;  // day invalid

	    // if the 29th of Feb, check for leap year
	    if (dateVal.day == 29 && dateVal.month == 2)
	    {
	        if (dateVal.year % 4 == 0 && (dateVal.year % 100 != 0 || dateVal.year % 400 == 0))
	            return 0;    // it is a leap year
	        else return 3;   // not a leap year, so the day is invalid
	    }
		    
		return 0;
	}



}
