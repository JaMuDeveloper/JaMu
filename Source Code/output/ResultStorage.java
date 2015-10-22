package output;

import java.util.HashMap;
import java.util.Map;

import testExecutor.Result;

public class ResultStorage {
	
	private int numberOfMutants;
	private int numberOfMutantsKilled;
	private Map<String, Boolean> mutantStatus;
	private Map<String, Integer> testCasePerformance;
	private Map<String,Map<String,Result>> mutatorResults;
	private Map<String, Result> originalResults;
	
	public ResultStorage(){
		numberOfMutants = 0;
		numberOfMutantsKilled = 0;
		mutantStatus = new HashMap<String, Boolean>();
		testCasePerformance = new HashMap<String, Integer>();
		mutatorResults = new HashMap<String,Map<String,Result>>();
		originalResults = new HashMap<String, Result>();
	}

	public Map<String, Map<String, Result>> getMutatorResults() {
		return mutatorResults;
	}
	public void setMutatorResults(Map<String, Map<String, Result>> mutatorResults) {
		this.mutatorResults = mutatorResults;
	}

	public void setNumberOfMutants(int numberOfMutants) {
		this.numberOfMutants = numberOfMutants;
	}
	public int getNumberOfMutants(){
		return numberOfMutants;
	}

	public void setNumberOfMutantsKilled(int numberOfMutantsKilled) {
		this.numberOfMutantsKilled = numberOfMutantsKilled;		
	}
	public int getNumberOfMutantsKilled(){
		return numberOfMutantsKilled;
	}

	public void setMutantStatus(Map<String, Boolean> mutantStatus) {
		this.mutantStatus = mutantStatus;		
	}
	public Map<String, Boolean> getMutantStatus(){
		return mutantStatus;
	}

	public void setTestCasePerformance(Map<String, Integer> testCasePerformance) {
		this.testCasePerformance = testCasePerformance;
	}
	public Map<String, Integer> getTestCasePerformance(){
		return testCasePerformance;
	}

	public void setOriginalResults(Map<String, Result> originalResults) {
		this.originalResults = originalResults;	
	}
	public Map<String, Result> getOriginalResults(){
		return originalResults;
	}

}
