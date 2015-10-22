package jamu;

import jamu.Activator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Settings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
  
	public void init(IWorkbench arg0) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
  
	public static List<String> validatePreferences(Shell s) {
		List<String> settings = new ArrayList<String>();
		boolean hasError = false;
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		String errorMessage = "The following options must be set before JaMu can execute:\n\n";
		String projectName = prefStore.getString("PROJECT_NAME");
		settings.add(projectName);
		if (projectName.equals("")) {
			errorMessage = errorMessage + "The name of the project created by JaMu\n";
			hasError = true;
		}
		String logging = prefStore.getString("LOGGING");
		String logDir = prefStore.getString("LOG_DIR");
		if (!logging.equals("NO_LOGGING") && (logDir.equals(""))) {
			errorMessage = errorMessage + "A folder to write the log file to has to be selected because logging is enabled\n";
			hasError = true;
		}
		settings.add(logging);
		settings.add(logDir);
		convertAndAddBoolean(prefStore.getBoolean("EXPORT_MUTANTS"), settings);
		String exportDir = prefStore.getString("EXPORT_DIR");
		settings.add(exportDir);
		if ((prefStore.getBoolean("EXPORT_MUTANTS")) && (exportDir.equals(""))) {
			errorMessage = errorMessage + "The location of the mutant output directory has to be selected because exporting mutants is enabled\n";
			hasError = true;
		}
		if (hasError) {
			MessageDialog.openError(s, "Settings incomplete", errorMessage);
			return new ArrayList<String>();
		}
		convertAndAddBoolean(prefStore.getBoolean("MUTANT_STATUS"), settings);
		convertAndAddBoolean(prefStore.getBoolean("TEST_RESULTS"), settings);
		convertAndAddBoolean(prefStore.getBoolean("TEST_PERFORMANCE"), settings);
		convertAndAddBoolean(prefStore.getBoolean("PROGRESS_MESSAGES"), settings);
		return settings;
	}
  
  private static void convertAndAddBoolean(boolean bool, List<String> settings) {
	  if (bool) {
		  settings.add("true");
	  } else {
		  settings.add("false");
	  }
  }
  
  protected void createFieldEditors() {
	  addField(new StringFieldEditor("PROJECT_NAME", "JaMu Project Name:", getFieldEditorParent()));
	  addField(new BooleanFieldEditor("PROGRESS_MESSAGES", "Show more progress messages during the mutant generation process ?", getFieldEditorParent()));
	  addField(new BooleanFieldEditor("MUTANT_STATUS", "Show the status of each mutant ?", getFieldEditorParent()));
	  addField(new BooleanFieldEditor("TEST_RESULTS", "Show complete test results ?", getFieldEditorParent()));
	  addField(new BooleanFieldEditor("TEST_PERFORMANCE", "Show the performance of each test ?", getFieldEditorParent()));
	  addField(new BooleanFieldEditor("EXPORT_MUTANTS", "Export mutants to disk ?", getFieldEditorParent()));
	  addField(new DirectoryFieldEditor("EXPORT_DIR", "Directory the mutants should be exported to:", getFieldEditorParent()));
	  addField(new RadioGroupFieldEditor("LOGGING", "Logging preference", 1,
			  new String[][] { { "Disable logging of the results", "NO_LOGGING" }, { "Log the shown results", "LOGGING_ON" }, 
			  {"Always log the most detailed results", "ALWAYS_DETAILED_LOGGING"} }, getFieldEditorParent()));
	  addField(new DirectoryFieldEditor("LOG_DIR", "Directory to create the log file in:", getFieldEditorParent()));
  }
  
}