package output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testExecutor.Failure;
import testExecutor.Result;

public class ResultAnalyzer {

	private ResultStorage resultStorage;
	
	public ResultAnalyzer(ResultStorage resultStorage){
		this.resultStorage = resultStorage;
	}
	
	public void analyze(Map<String, Map<String, Result>> mutatorResults){
		analyzeMutationScore(mutatorResults);
		analyzeTestCasePerformance(mutatorResults);
	}
	
	public void analyzeMutationScore(Map<String, Map<String, Result>> mutatorResults){
		int numberOfMutants = mutatorResults.keySet().size();
		int nrOfKilledMutants = 0;
		Map<String, Boolean> mutantStatus = new HashMap<String, Boolean>();
		Set<String> keySet = mutatorResults.keySet();
		for (String key : keySet){
			Map<String,Result> singleMutantResults = mutatorResults.get(key);
			boolean isKilled = false;
			Set<String> altKeySet = singleMutantResults.keySet();
			for (String altKey : altKeySet){
				Result result = singleMutantResults.get(altKey);
				if (result.getFailureCount() > 0){
					isKilled = true;
					break;
				}
			}
			if (isKilled){
				nrOfKilledMutants++;
				mutantStatus.put(key, true);
			}
			else{
				mutantStatus.put(key, false);
			}
		}
		resultStorage.setNumberOfMutants(numberOfMutants);
		resultStorage.setNumberOfMutantsKilled(nrOfKilledMutants);
		resultStorage.setMutantStatus(mutantStatus);
	}
	
	public void analyzeTestCasePerformance(Map<String, Map<String, Result>> mutatorResults){
		Map<String,Integer> testCasePerformance = new HashMap<String, Integer>();
		Set<String> keys = mutatorResults.keySet();
		for (String key : keys){
			Map<String,Result> result = mutatorResults.get(key);
			Set<String> altKeys = result.keySet();
			for (String altKey : altKeys){
				Result r = result.get(altKey);
				if (r.getFailureCount() > 0){
					List<Failure> failures = r.getFailures();
					for (Failure f : failures){
						String testName = f.getTestHeader();
						if (!testCasePerformance.containsKey(testName)){
							testCasePerformance.put(testName, 1);
						}
						else{
							testCasePerformance.put(testName, testCasePerformance.get(testName)+1);
						}
					}
				}
			}
		}
		resultStorage.setTestCasePerformance(testCasePerformance);
	}
	
	/*public ResultAnalyser(Logger logger,  boolean showMutantStatus, boolean showTestPerformance){
		this.logger = logger;
		this.showMutantStatus = showMutantStatus;
		this.showTestPerformance = showTestPerformance;
		failedOriginalTests = new ArrayList<String>();
	}

	public void analyse(Map<String, Result> originalResults, Map<String, Map<String, Result>> mutatorResults) {
		analyseOriginalResults(originalResults);
		double nrOfKilledMutants = analyseNumberKilled(originalResults, mutatorResults);
		if (verboseOutput && nrOfKilledMutants > 0){
			tryToOutput("\nShowing individual test performance:\n");
			analyseIndividualTestCases(originalResults, mutatorResults);
		}
	}
	
	private double analyseNumberKilled(Map<String, Result> originalResults, Map<String, Map<String, Result>> mutatorResults) {
		double nrOfMutants = 0;
		double nrOfKilledMutants = 0;
		Set<String> keySet = mutatorResults.keySet();
		for (String key : keySet){
			Map<String,Result> singleMutantResults = mutatorResults.get(key);
			boolean isKilled = false;
			Set<String> altKeySet = singleMutantResults.keySet();
			for (String altKey : altKeySet){
				Result result = singleMutantResults.get(altKey);
				if (result.getFailureCount() > originalResults.get(altKey).getFailureCount()){
					List<Failure> failures = result.getFailures();
					for (Failure failure : failures){
						if (!failedOriginalTests.contains(failure.getTestHeader())){
							isKilled = true;
							break;
						}
					}
					if (isKilled){
						break;
					}
				}
			}
			nrOfMutants++;
			if (isKilled){
				tryToOutput(key + " has been killed\n");
				nrOfKilledMutants++;
			}
			else{
				tryToOutput(key + " has not been killed\n");
			}
		}
		tryToOutput((nrOfKilledMutants/nrOfMutants * 100) + "% of the mutants have been killed\n");
		return nrOfKilledMutants;
	}

	private void analyseOriginalResults(Map<String, Result> originalResults) {
		Set<String> keySet = originalResults.keySet();
		for (String key : keySet){
			Result result = originalResults.get(key);
			if (result.getFailureCount() > 0){
				List<Failure> failures = result.getFailures();
				for (Failure failure : failures){
					failedOriginalTests.add(failure.getTestHeader());
				}
			}
		}		
	}

	private void analyseIndividualTestCases(Map<String, Result> originalResults, Map<String, Map<String, Result>> mutatorResults) {
		Map<String,Integer> testCasePerformance = new HashMap<String, Integer>();
		Set<String> keys = mutatorResults.keySet();
		for (String key : keys){
			Map<String,Result> result = mutatorResults.get(key);
			Set<String> altKeys = result.keySet();
			for (String altKey : altKeys){
				Result r = result.get(altKey);
				if (r.getFailureCount() > 0){
					List<Failure> failures = r.getFailures();
					for (Failure f : failures){
						String testName = f.getTestHeader();
						if (!failedOriginalTests.contains(testName)){
							if (!testCasePerformance.containsKey(testName)){
								testCasePerformance.put(testName, 1);
							}
							else{
								testCasePerformance.put(testName, testCasePerformance.get(testName)+1);
							}
						}
					}
				}
			}
		}
		Set<String> keySet = testCasePerformance.keySet();
		for (String k : keySet){
			tryToOutput(k + " killed " + testCasePerformance.get(k) + " mutants\n");
		}
	}

	private void tryToOutput(String s) {
		if (verboseOutput) {
			System.out.print(s);
		}
		if (logger.isEnabled()) {
			logger.log(s);
		}
	}*/
	
}
