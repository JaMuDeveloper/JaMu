package jamu;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "JaMu"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static Shell getActiveWorkbenchShell() {
 		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
 		if (workBenchWindow == null)
 			return null;
 		return workBenchWindow.getShell();
 	}
	

 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		if (plugin == null)
 			return null;
 		IWorkbench workBench= plugin.getWorkbench();
 		if (workBench == null)
 			return null;
 			return workBench.getActiveWorkbenchWindow();
 	}
 	
 	public IWorkbench getWorkbench() {
 	    return PlatformUI.getWorkbench();
 	}

	
}
