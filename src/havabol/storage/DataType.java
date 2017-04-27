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
		case "Int":
			return INTEGER;
		case "float":
		case "Float":
			return FLOAT;
		case "string":
		case "String":
			return STRING;
		case "bool":
		case "Bool":
			return BOOLEAN;
		case "date":
		case "Date":
			return DATE;
		case "void":
		case "Void":
			return VOID;
		default:
			return null;
		}
	}
	
}
