package mutation;

public class Failure {

	private Throwable exception;
	private String message;
	private String testHeader;
	private String trace;
	private String toString;
	
	public Failure(Throwable exception, String message, String testHeader, String trace, String toString){
		this.exception = exception;
		this.message = message;
		this.testHeader = testHeader;
		this.trace = trace;
		this.toString = toString;		
	}
	
	public Throwable getException() {
		return exception;
	}
	public String getMessage() {
		return message;
	}
	public String getTestHeader() {
		return testHeader;
	}
	public String getTrace() {
		return trace;
	}
	public String getToString() {
		return toString;
	}
	
}
