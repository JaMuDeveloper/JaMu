package output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Logger {

	private PrintWriter writer;
	private String logFilePath;
	private boolean loggingEnabled;

	public Logger(String logFilePath, boolean loggingEnabled) {
		this.logFilePath = logFilePath;
		this.loggingEnabled = loggingEnabled;
		emptyLog();
	}

	public void log(String content) {
		if (loggingEnabled) {
			writer.append(content);
		}
	}

	public void emptyLog() {
		if (this.loggingEnabled) {
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

	public void doneLogging() {
		loggingEnabled = false;
		writer.close();
	}
}
