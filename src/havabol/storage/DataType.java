package havabol.storage;

public enum DataType {

	INTEGER,
	FLOAT,
	STRING,
	BOOLEAN;
	
	public static DataType stringToType(String type) {
		switch (type) {
		case "Int":
			return INTEGER;
		case "Float":
			return FLOAT;
		case "String":
			return STRING;
		case "Bool":
			return BOOLEAN;
		default:
			return null;
		}
	}
	
}
