package jamu.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

public class SettingsHandler extends AbstractHandler {

	private static final String PREFERENCE_PAGE_NAME = "JaMu.SettingsPage";
  
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		Shell shell = HandlerUtil.getActiveShell(event);
		PreferencesUtil.createPreferenceDialogOn(shell, PREFERENCE_PAGE_NAME, new String[] { PREFERENCE_PAGE_NAME }, null).open();
		return null;
	}

}