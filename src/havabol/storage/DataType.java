package havabol.storage;

public enum DataType {

	INTEGER,
	FLOAT,
	STRING,
	BOOLEAN,
	VOID, DATE;
	
	/**
	 * Function:		stringToType
	 * @param type		string whose datatype should be set
	 * @return			appropriate datatype
	 */
	public static DataType stringToType(String type) {
		switch (type.toLowerCase()) {
		case "int":
			return INTEGER;
		case "float":
			return FLOAT;
		case "string":
			return STRING;
		case "bool":
			return BOOLEAN;
		default:
			return null;
		}
	}
	
}
