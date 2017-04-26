package havabol.error;

public class InternalError extends Error {
	
	/**	 
	 * Constructs a new HavaBol internal error for serious implementation
	 * errors in the HavaBol interpreter.
	 * @param message: 		message to be displayed
	 */
	public InternalError(String message) {
		super(message);
	}
	
}
