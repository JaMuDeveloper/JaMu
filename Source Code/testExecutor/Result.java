package testExecutor;

import java.util.List;

public class Result {
	
	private int failureCount; 
    private List<Failure> failures;
	private int ignoredCount;
	private int runCount;
	private long runTime;
	private boolean successful;
	 
	public Result(int failureCount, List<Failure> failures, int ignoredCount, int runCount, long runTime, boolean successful){
		this.failureCount = failureCount;
		this.failures = failures;
		this.ignoredCount = ignoredCount;
		this.runCount = runCount;
		this.runTime = runTime;
		this.successful = successful;
	}
	 
	public int getFailureCount() {
		return failureCount;
	}
	public List<Failure> getFailures() {
		return failures;
	}
	public int getIgnoredCount() {
		return ignoredCount;
	}
	public int getRunCount() {
		return runCount;
	}
	public long getRunTime() {
		return runTime;
	}
	public boolean wasSuccessful() {
		return successful;
	}

}