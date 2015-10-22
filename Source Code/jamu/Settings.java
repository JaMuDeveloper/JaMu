package jamu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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
		String gitDir = prefStore.getString("GIT_DIR");
		settings.add(gitDir);
		if (gitDir.equals("")) {
			errorMessage = errorMessage + "The location of the git directory\n";
			hasError = true;
		}
		String projectName = prefStore.getString("PROJECT_NAME");
		settings.add(projectName);
		if (projectName.equals("")) {
			errorMessage = errorMessage + "The name of the project created by JaMu\n";
			hasError = true;
		}
		String branchName = prefStore.getString("BRANCH_NAME");
		settings.add(branchName);
		if (branchName.equals("")) {
			errorMessage = errorMessage + "The name of the git branch used to store mutants\n";
			hasError = true;
		}
		convertAndAddBoolean(prefStore.getBoolean("LOGGING"), settings);
		String logDir = prefStore.getString("LOG_DIR");
		settings.add(logDir);
		if ((prefStore.getBoolean("LOGGING")) && (logDir.equals(""))) {
			errorMessage = errorMessage + "The location of the output file, if writing to a file is enabled\n";
			hasError = true;
		}
		if (hasError) {
			MessageDialog.openError(s, "Settings incomplete", errorMessage);
			return new ArrayList<String>();
		}
		convertAndAddBoolean(prefStore.getBoolean("KEEP_PROJECT_GIT"), settings);
		convertAndAddBoolean(prefStore.getBoolean("RESET_GIT"), settings);
		convertAndAddBoolean(prefStore.getBoolean("VERBOSE_OUTPUT"), settings);
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
    addField(new DirectoryFieldEditor("GIT_DIR", "Location of the git directory:", getFieldEditorParent()));
    addField(new StringFieldEditor("PROJECT_NAME", "JaMu Project Name:", getFieldEditorParent()));
    addField(new StringFieldEditor("BRANCH_NAME", "JaMu Branch Name:", getFieldEditorParent()));
    addField(new BooleanFieldEditor("KEEP_PROJECT_GIT", "Keep the generated mutants in git ?", getFieldEditorParent()));
    addField(new BooleanFieldEditor("RESET_GIT", "Empty the git repository before generating mutants ? HANDLE WITH CARE!", getFieldEditorParent()));
    addField(new BooleanFieldEditor("VERBOSE_OUTPUT", "Show detailed output ?", getFieldEditorParent()));
    addField(new BooleanFieldEditor("LOGGING", "Write the output to a file ?", getFieldEditorParent()));
    addField(new DirectoryFieldEditor("LOG_DIR", "Location of the output file:", getFieldEditorParent()));
  }
  
}