package havabol.lexer;

public enum PrimClassif {
	
	OPERATOR,
	OPERAND,
	SEPARATOR,
	CONTROL,
	FUNCTION,
	EOF,
	RT_PAREN,
	UNDEFINED ("Undefined");
	
	private String name;
	
	private PrimClassif() {
		
	}
	
	private PrimClassif(String name) {
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
