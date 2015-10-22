package output;

import java.util.Map;
import java.util.Set;
import mutation.Failure;
import mutation.Result;

public class ResultOutputter {
	private Logger logger;
	private boolean verboseOutput;

	public ResultOutputter(Logger logger, boolean verboseOutput) {
		this.logger = logger;
		this.verboseOutput = verboseOutput;
	}

	public void printResults(Map<String, Result> originalResults, Map<String, Map<String, Result>> mutatorResults) {
		tryToOutput("Showing original test results:\n");
		printResultMap(originalResults);
		Set<String> keys = mutatorResults.keySet();
		for (String key : keys) {
			tryToOutput("Showing test results of mutant " + key + ":\n");
			printResultMap(mutatorResults.get(key));
		}
	}

	public void printResultMap(Map<String, Result> resultMap) {
		Set<String> origKeys = resultMap.keySet();
		for (String key : origKeys) {
			Result r = (Result) resultMap.get(key);
			printIndividualResult(r);
		}
	}

	public void printIndividualResult(Result r) {
		String stringToPrint = "Running all tests took " + r.getRunTime() + " ms. " + r.getRunCount() + " tests were run. " + r.getFailureCount() + " tests failed. " + r.getIgnoredCount() + " tests were ignored\n";
		for (Failure f : r.getFailures()){
			stringToPrint += f.getTestHeader() + " failed because " + f.getMessage() + "\n";
		}
		tryToOutput(stringToPrint+"\n");
	}

	private void tryToOutput(String s) {
		if (verboseOutput) {
			System.out.print(s);
		}
		if (logger.isEnabled()) {
			logger.log(s + "\n");
		}
	}
}
