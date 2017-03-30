package havabol.storage;

public enum DataType {

	INTEGER,
	FLOAT,
	STRING,
	BOOLEAN,
	VOID, DATE;
	
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
