package havabol.lexer;

public enum SubClassif {
	
	IDENTFIER,
    INTEGER,
    FLOAT,
    BOOLEAN,
    STRING,
    DATE,
    FLOW,
    END,
    DECLARE,
    BUILTIN,
    USER,
	VOID ("Void");
	
	private String name;
	
	private SubClassif() {
		
	}
	
	private SubClassif(String name) {
		this.name = name;
	}
	
	public String toString() {
		if (name != null) {
			return name;
		} else {
			return super.toString();
		}
	}

}
