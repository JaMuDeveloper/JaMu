package output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Logger {

	private PrintWriter writer;
	private String logFilePath;
	private boolean loggingEnabled;
	private boolean logEverything;

	public Logger(String logFilePath, String typeOfLogging) {
		this.logFilePath = logFilePath;
		if (typeOfLogging.equals("NO_LOGGING")){
			loggingEnabled = false;
			logEverything = false;
		}
		else if (typeOfLogging.equals("LOGGING_ON")){
			loggingEnabled = true;
			logEverything = false;
		}
		else{
			loggingEnabled = true;
			logEverything = true;
		}
		emptyLog();
	}

	public void log(String content) {
		if (loggingEnabled) {
			writer.append(content);
		}
	}

	public void emptyLog() {
		if (loggingEnabled) {
			try {
				writer = new PrintWriter(new File(this.logFilePath));
			} catch (FileNotFoundException localFileNotFoundException) {
				//TODO Show error message that the log cannot be written to
			}
		}
	}

	public boolean isEnabled() {
		return loggingEnabled;
	}
	
	public boolean logEverything(){
		return logEverything;
	}

	public void doneLogging() {
		loggingEnabled = false;
		writer.close();
	}
}
