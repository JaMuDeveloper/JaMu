package testExecutor;

public class Failure {

	private String message;
	private String testHeader;
	
	public Failure(String message, String testHeader){
		this.message = message;
		this.testHeader = testHeader;
	}
	
	public String getMessage() {
		return message;
	}
	public String getTestHeader() {
		return testHeader;
	}

	public boolean isEquals(Failure f) {
		return (message.equals(f.getMessage()) && testHeader.equals(f.getTestHeader()));
	}

	
}
