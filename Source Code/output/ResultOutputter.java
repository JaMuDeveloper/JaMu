package output;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.console.MessageConsoleStream;

import testExecutor.Failure;
import testExecutor.Result;

public class ResultOutputter {
	
	private Logger logger;
	private boolean showMutantStatus;
	private boolean showTestResults;
	private boolean showTestPerformance;
	private ConsoleController console;
	private MessageConsoleStream out;
	
	private static final String GENERAL = "TYPE_GENERAL_INFORMATION";
	private static final String TEST_RESULTS = "TYPE_TEST_RESULTS";
	private static final String TEST_PERFORMANCE = "TYPE_TEST_PERFORMANCE";
	private static final String MUTANT_STATUS = "TYPE_MUTANT_STATUS";
	public static final String CONSOLE_NAME = "Console";
	
	public ResultOutputter(Logger logger, ConsoleController console, boolean showMutantStatus, boolean showTestResults, boolean showTestPerformance){
		this.logger = logger;
		this.showMutantStatus = showMutantStatus;
		this.showTestPerformance = showTestPerformance;
		this.showTestResults = showTestResults;
		this.console = console;
		out = console.getOutputStream();
	}
	
	public void doOutput(ResultStorage resultStorage){
		console.setFocusOnConsole();
		printTestResults(resultStorage);
		printMutantStatus(resultStorage);
		printTestPerformance(resultStorage);
		printGeneralInformation(resultStorage);
	}

	public void printGeneralInformation(ResultStorage resultStorage) {
		tryToOutput("MUTATION SCORE\n",GENERAL);
		tryToOutput("------------------\n",GENERAL);
		tryToOutput(resultStorage.getNumberOfMutants() + " mutants were generated.\n",GENERAL);
		tryToOutput(resultStorage.getNumberOfMutantsKilled() + " mutants were killed.\n",GENERAL);
		double mutationScore = ((double) resultStorage.getNumberOfMutantsKilled())/ ((double) resultStorage.getNumberOfMutants())*100;
		String mutationScoreFormatted = String.format("%.2f", mutationScore);
		tryToOutput("The mutation score is " + mutationScoreFormatted + "%.\n" ,GENERAL);
		tryToOutput("------------------\n\n",GENERAL);
	}
	
	public void printTestResults(ResultStorage resultStorage) {
		Map<String, Result> originalResults = resultStorage.getOriginalResults();
		printOriginalTestResults(originalResults);
		Map<String, Map<String, Result>> mutationTestResulst = resultStorage.getMutatorResults();
		printMutationTestResults(mutationTestResulst);
	}
	
	private void printOriginalTestResults(Map<String, Result> originalResults){
		tryToOutput("ORIGINAL TEST RESULTS\n",TEST_RESULTS);	
		tryToOutput("------------------\n",TEST_RESULTS);
		Set<String> testFileNames = originalResults.keySet();
		for (String testFileName : testFileNames){
			Result testResults = originalResults.get(testFileName);
			printSingleTestFileResult(testFileName, testResults.getFailures(), testResults.getRunTime(), testResults.getRunCount(), testResults.getFailureCount(), testResults.getIgnoredCount());
		}
		tryToOutput("------------------\n\n",TEST_RESULTS);
	}
	
	private void printMutationTestResults( Map<String, Map<String, Result>> mutationTestResults) {
		tryToOutput("MUTANT TEST RESULTS\n",TEST_RESULTS);
		tryToOutput("------------------\n",TEST_RESULTS);
		Set<String> mutantNames = mutationTestResults.keySet();
		for (String mutantName : mutantNames){
			Map<String, Result> mutantTestResults = mutationTestResults.get(mutantName);
			Set<String> testFileNames = mutantTestResults.keySet();
			for (String testFileName : testFileNames){
				Result testResults = mutantTestResults.get(testFileName);
				printSingleTestFileResult(testFileName, testResults.getFailures(), testResults.getRunTime(), testResults.getRunCount(), testResults.getFailureCount(), testResults.getIgnoredCount());
			}
		}
		tryToOutput("------------------\n\n",TEST_RESULTS);
	}
	
	private void printSingleTestFileResult(String testFileName, List<Failure> failureList, long totalRunTime, int totalRunCount, int totalFailureCount, int totalIgnoreCount) {
		String stringToPrint = testFileName + ":\n";
		stringToPrint += "Running all tests took " + totalRunTime + " ms. " + totalRunCount + " tests were run. " + totalFailureCount + " tests failed. " + totalIgnoreCount + " tests were ignored\n";
		for (Failure f : failureList){
			stringToPrint += "\t" + f.getTestHeader() + " failed";
			if (f.getMessage() != null){
				stringToPrint += " because " + f.getMessage();
			}
			stringToPrint += ".\n";
		}
		tryToOutput(stringToPrint+"\n",TEST_RESULTS);
	}
	
	public void printTestPerformance(ResultStorage resultStorage) {
		tryToOutput("MUTANT TEST RESULTS\n",TEST_PERFORMANCE);
		tryToOutput("------------------\n",TEST_PERFORMANCE);
		Map<String, Integer> testCasePerformance = resultStorage.getTestCasePerformance();
		Set<String> testCaseNames = testCasePerformance.keySet();
		for (String testCaseName : testCaseNames){
			int index = testCaseName.indexOf("(");
			if (index != -1){
				tryToOutput(testCaseName.substring(0,index) + " in " + testCaseName.substring(index+1,testCaseName.length()-1),TEST_PERFORMANCE);
				double mutationScore = ((double) testCasePerformance.get(testCaseName))/ ((double) resultStorage.getNumberOfMutants())*100;
				String mutationScoreFormatted = String.format("%.2f", mutationScore);
				tryToOutput(" killed " + testCasePerformance.get(testCaseName) + " mutants. This test has a mutation score of " + mutationScoreFormatted + "%.\n",TEST_PERFORMANCE);
			}
		}
		tryToOutput("------------------\n\n",TEST_PERFORMANCE);
	}

	public void printMutantStatus(ResultStorage resultStorage) {
		tryToOutput("MUTANT STATUS\n",MUTANT_STATUS);
		tryToOutput("------------------\n",MUTANT_STATUS);
		Map<String, Boolean> mutantStatus = resultStorage.getMutantStatus();
		Set<String> mutantNames = mutantStatus.keySet();
		for (String mutantName : mutantNames){
			if (mutantStatus.get(mutantName)){
				tryToOutput(mutantName + " has been killed.\n",MUTANT_STATUS);
			}
			else{
				tryToOutput(mutantName + " is still alive.\n",MUTANT_STATUS);
			}
		}
		tryToOutput("------------------\n\n",MUTANT_STATUS);
	}

	public void tryToOutput(String s, String outputType){
		if (logger.logEverything()) {
			logger.log(s);
			if ((outputType.equals(TEST_RESULTS) && showTestResults) || (outputType.equals(TEST_PERFORMANCE) && showTestPerformance) || (outputType.equals(MUTANT_STATUS) && showMutantStatus) || outputType.equals(GENERAL)){
					out.print(s);
			}
		}
		else{
			if ((outputType.equals(TEST_RESULTS) && showTestResults) || (outputType.equals(TEST_PERFORMANCE) && showTestPerformance) || (outputType.equals(MUTANT_STATUS) && showMutantStatus) || outputType.equals(GENERAL)){
				out.print(s);
				if (logger.isEnabled()){
					logger.log(s);
				}
			}
		}
	}
		
}