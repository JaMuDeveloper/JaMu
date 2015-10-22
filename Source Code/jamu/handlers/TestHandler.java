package jamu.handlers;

import jamu.Settings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jgit.JGitHandlerLocal;
import mutation.MutationEngine;
import mutation.Result;
import output.ResultOutputter;
import mutation.TestExecutor;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import output.Logger;
import visitors.VisitorList;

public class TestHandler extends AbstractHandler {

	private String projectName;
	  private IProject tempProject;
	  private IProject projectFolder;
	  private IJavaProject javaProjectFolder;
	  private IJavaProject javaTempProject;
	  private List<IPackageFragmentRoot> sourceFolders;
	  private JGitHandlerLocal API;
	  public String defaultBranch = "master";
	  private Logger logger;
	  public static String PATH_TO_GIT_DIR;
	  public static String JAMU_PROJECT_NAME;
	  public static String JAMU_BRANCHNAME;
	  public static boolean LOGGING_ON;
	  public static String PATH_TO_LOG_FILE;
	  public static boolean KEEP_FILES_GIT;
	  public static boolean RESET_GIT;
	  public static boolean VERBOSE_OUTPUT;private final static String ERROR_FILE_NOT_IN_PROJECT = "THE_FILE_IS_NOT_PRESENT_IN_THE_PROJECT_OR_IS_NOT_A_JAVA_FILE";
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
		IStructuredSelection selection = (IStructuredSelection) sel;
		Object firstElement = selection.getFirstElement();
		if (!loadPreferences(shell)) {
			return null;
		}
		if (firstElement instanceof IJavaElement) {
			boolean success = readProjectData((IJavaElement) firstElement, shell);
			if (!success){
				return null;
			}
			tempProject = runSetup(shell);
			if (tempProject == null){
				return null;
			}
			javaTempProject = JavaCore.create(tempProject);
			List<IFile> selectedFiles = selectFilesFromProject(shell,javaProjectFolder,"Class selection", "Select the java files to mutate:");
			if (selectedFiles == null){
				MessageDialog.openError(shell, "Error", "No files have been selected.");
				return null;
			}
			List<String> selectedMutators = selectMutators(shell);
			if (selectedMutators == null){
				MessageDialog.openError(shell, "Error", "No mutation rules have been selected.");
				return null;
			}
			List<String> selectedTests = getPackageIncludedPaths(shell, selectFilesFromProject(shell,javaProjectFolder,"Test selection","Select the tests to run:"));
			if (selectedTests == null){
				MessageDialog.openError(shell, "Error", "No tests have been selected.");
				return null;
			}
			if (selectedTests.contains(ERROR_FILE_NOT_IN_PROJECT)){
				return null;
			}
			List<String> branchNameStorage = new ArrayList<String>();
			success = generateMutants(shell, selectedMutators, IFileListToFileList(selectedFiles),branchNameStorage);
			if (!success){
				return null;
			}
			runTests(shell, selectedTests,branchNameStorage);
		    if (!KEEP_FILES_GIT) {
		    	doCleanUpGit(shell, branchNameStorage);
		    }
		    doCleanUpEclipse(shell);
		    if (LOGGING_ON){
		    	logger.doneLogging();
		    }
		}
		return null;
	}
	
	private boolean loadPreferences(Shell shell) {
		List<String> options = Settings.validatePreferences(shell);
		if (options.isEmpty()) {
			return false;
	    }
	    try {
	    	Iterator<String> it = options.iterator();
		    PATH_TO_GIT_DIR = (String)it.next();
		    JAMU_PROJECT_NAME = (String)it.next();
		    JAMU_BRANCHNAME = (String)it.next();
		    LOGGING_ON = ((String)it.next()).equals("true");
		    PATH_TO_LOG_FILE = (String)it.next() + "/JaMu_Log_File.txt";
		    KEEP_FILES_GIT = ((String)it.next()).equals("true");
		    RESET_GIT = ((String)it.next()).equals("true");
		    VERBOSE_OUTPUT = ((String)it.next()).equals("true");
		    this.logger = new Logger(PATH_TO_LOG_FILE, LOGGING_ON);
	    }
	    catch (NoSuchElementException localNoSuchElementException) {
			MessageDialog.openError(shell, "Error", "There was an error loading the settings.");
	    	return false;
	    }
	    return true;
	}
	
	private boolean readProjectData(IJavaElement javaElement, Shell shell) {
		javaProjectFolder = javaElement.getJavaProject();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		projectFolder = root.getProject(javaProjectFolder.getElementName());
		if (!projectFolder.isOpen()) {
			try {
				projectFolder.open(null);
			} catch (CoreException e) {
				MessageDialog.openError(shell, "Error", "JaMu was unable to read data from the workspace.");
				return false;
			}
		}
		projectName = projectFolder.getName();
		return true;
	}
	
	private IProject runSetup(Shell shell) {
		boolean succes = runRepoSetup(shell);
		if (!succes){
			return null;
		}
		IProject tempfolder = createTempProject(projectFolder,shell);
		try {
			API.addFileAndCommit(tempfolder.getLocation().toFile(), "Original code added");
		} catch (IOException | GitAPIException e) {
			MessageDialog.openError(shell, "Error", "Unable to add files to the git repository.");
			e.printStackTrace();
			return null;
		}
		try {
			API.switchBranch(defaultBranch);
		} catch (GitAPIException e) {
			MessageDialog.openError(shell, "Error", "Unable to switch branches.");
			e.printStackTrace();
			return null;
		}
		return tempfolder;
	}
	
	private boolean runRepoSetup(Shell shell){
		// Switching error streams here to avoid a SLF4J error that is related
		// to the project dependencies
		PrintStream oldErrStream = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
		try {
			API = new JGitHandlerLocal(PATH_TO_GIT_DIR, projectName);
			if (RESET_GIT) {
				API = API.resetRepo();
		    }
		} catch (GitAPIException | IOException e) {
			MessageDialog.openError(shell, "Error", "Internal error with the git setup.");
			e.printStackTrace();
			return false;
		}
		System.setErr(oldErrStream);
		try {
			API.createBranch(JAMU_BRANCHNAME);
			defaultBranch = API.getCurrentBranch();
			API.switchBranch(JAMU_BRANCHNAME);
		} catch (GitAPIException | IOException e) {
			MessageDialog.openError(shell, "Error", "Unable to switch branches.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private IProject createTempProject(IProject projectFolder, Shell shell) {
		try{
			String root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			File toDir = new File(root + "/" + JAMU_PROJECT_NAME);
			if (toDir.exists()) {
				FileUtils.deleteDirectory(toDir);
			}
			File fromDir = new File(root + projectFolder.getFullPath().toString());
			File[] files = fromDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(file, toDir);
				} else {
					FileUtils.copyFileToDirectory(file, toDir);
				}
			}
		} catch (IOException e){
			MessageDialog.openError(shell, "Error", "Unable to copy files on disk.");
			e.printStackTrace();
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(JAMU_PROJECT_NAME);
		try {
			if (!project.exists()) {
				project.create(null);
			}
		    if (!project.isOpen()) {
		    	project.open(null);
		    }
		} catch (CoreException e) {
			MessageDialog.openError(shell, "Error", "Unable to create a temporary project in eclipse.");
			e.printStackTrace();
		}
		return project;
	}
	
	private List<IFile> selectFilesFromProject(Shell shell, IJavaProject javaProjectFolder, String dialogTitle, String dialogMessage) {
		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(shell, new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
		dialog.setTitle(dialogTitle);
		dialog.setMessage(dialogMessage);
		dialog.setContainerMode(true);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		dialog.setExpandedElements(getAllElements());
		dialog.addFilter(new ShowOnlyJavaFiles(javaProjectFolder));
		int status = dialog.open();
		if (status == Window.CANCEL){
			return null;
		}
		List<IFile> selectedFiles = new ArrayList<IFile>();
		Object[] dialogResult = dialog.getResult();
		if (dialogResult != null) {
			for (Object o : dialogResult) {
				if (o instanceof IFile) {
					IFile f = (IFile) o;
					selectedFiles.add(f);
				}
			}
		}
		return selectedFiles;
	}
	
	private Object[] getAllElements() {
		List<Object> elements = new ArrayList<Object>();
		elements.add(projectFolder);
		try {
			IResource[] items = projectFolder.members();
			for (IResource r : items){
				if ((r instanceof IFolder)) {
					elements.add(r);
					expandFolder((IFolder)r, elements);
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return elements.toArray();
	}
	  
	private void expandFolder(IFolder resource, List<Object> elements) throws CoreException {
		IResource[] items = resource.members();
		for (IResource r : items){
			if (((r instanceof IFolder)) && (ShowOnlyJavaFiles.containsJavaFiles((IFolder)r))) {
				elements.add(r);
			    expandFolder((IFolder)r, elements);
			}
		}
	}
	
	private List<String> selectMutators(Shell shell) {
		List<String> labels = VisitorList.getLabels();
		List<String> mutatorsToRun = new ArrayList<String>();
		ListSelectionDialog dialog = new ListSelectionDialog(shell, labels , new ArrayContentProvider(),new LabelProvider(), "Select the mutators to run:");
		dialog.setTitle("Mutator selection");
		int result = dialog.open();
		if (result == Window.CANCEL){
			return null;
		}
		Object[] results = dialog.getResult();
		if (results != null){
			for (Object o : results){
				mutatorsToRun.add((String) o);
			}
		}
		return mutatorsToRun;
	}

	private List<File> IFileListToFileList(List<IFile> files){
		List<File> newList = new ArrayList<File>();
		for (IFile f : files){
			newList.add(f.getLocation().toFile());
		}
		return newList;
	}
	
	private List<String> getPackageIncludedPaths(Shell shell, List<IFile> files){
		List<String> newList = new ArrayList<String>();
		for (IFile f : files){
			String qualifiedPath = getPackageDeclaration(shell, f.getProjectRelativePath().toString());
			if (qualifiedPath.equals(ERROR_FILE_NOT_IN_PROJECT)){
				return null;
			}
			newList.add(qualifiedPath);
		}
		return newList;
	}

	private boolean generateMutants(Shell shell, List<String> mutatorLabels, List<File> filesToMutate, List<String> branchNameStorage) {
		try{
			MutationEngine m = new MutationEngine(mutatorLabels, API, filesToMutate, projectFolder, tempProject, VERBOSE_OUTPUT, logger);
			API.switchBranch(JAMU_BRANCHNAME);
			boolean success = m.start(branchNameStorage);
			// The default branch is empty if this code creates the project, so it
			// saves disk space to keep that one checked out
			API.switchBranch(defaultBranch);
			return success;
		} catch(GitAPIException | JavaModelException | IOException e){
			MessageDialog.openError(shell, "Mutant generation error", "Unable to generate all the mutants.");
			return false;
		}
	}
	
	private boolean runTests(Shell shell, List<String> selectedTests, List<String> branchNameList){
		try {
			TestExecutor originalExecutor = new TestExecutor(javaProjectFolder);
			Map<String,Result> originalResults = executeTestsInProject(shell, originalExecutor,selectedTests);
			if (originalResults == null){
				return false;
			}
			Map<String,Map<String,Result>> mutatorResults = new HashMap<String,Map<String,Result>>();
			for (String branchName : branchNameList){
				boolean success = loadFilesIntoProject(shell, branchName);
				if (!success){
					return false;
				}
				try {
					tempProject.refreshLocal(IResource.DEPTH_INFINITE, null);
					tempProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
				} catch (CoreException e) {
					MessageDialog.openError(shell, "Load error", "Failed to load all mutated files into the workspace");
					return false;
				}				
				TestExecutor mutantExecutor = new TestExecutor(javaTempProject);
				Map<String,Result> mutatedResults = executeTestsInProject(shell, mutantExecutor,selectedTests);
				if (mutatedResults == null){
					return false;
				}
				mutatorResults.put(branchName,mutatedResults);
			}
			//TODO compare results;
			ResultOutputter ro = new ResultOutputter(logger, VERBOSE_OUTPUT);
		    ro.printResults(originalResults, mutatorResults);
		    try{
		    	API.switchBranch(JAMU_BRANCHNAME);
		    }
		    catch (GitAPIException e){
		    	return true;
		    }
		} catch (MalformedURLException | CoreException | ReflectiveOperationException e1) {
			MessageDialog.openError(shell, "Execution error", "Failed to run all tests");
			return false;
		}
		return true;
	}

	private boolean loadFilesIntoProject(Shell shell, String branchName) {
		String root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		File toDir = new File(root);
		try {
			API.switchBranch(branchName);
		} catch (GitAPIException e) {
			MessageDialog.openError(shell, "Load error", "Failed to load all mutated files into the workspace");
			return false;
		}
		File fromDir = new File(PATH_TO_GIT_DIR + "/" + projectName);
		File[] files = fromDir.listFiles();
		try{
			for (File file : files) {
				if (file.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(file, toDir);
				} else {
					FileUtils.copyFileToDirectory(file, toDir);
				}
			}
		} catch (IOException e){
			MessageDialog.openError(shell, "Copy error", "Failed to load copy files to temporary project. Cannot run all tests");
			return false;
		}
		return true;
	}



	private Map<String, Result> executeTestsInProject(Shell shell, TestExecutor te, List<String> selectedTests){
		try {
			Map<String,Result> results = new HashMap<String,Result>();
			for (String fileName : selectedTests){
				Result r = te.runAllTests(fileName);
				results.put(fileName, r);
				return results;
			}
		} catch (IOException | ReflectiveOperationException  e) {
			MessageDialog.openError(shell, "Test execution failure", "Failed to run all tests");
			return null;
		}
		return null;
	}
	
	private void findSourceFolders(Shell shell){
		try {
			IPackageFragmentRoot[] packageRoots = javaProjectFolder.getAllPackageFragmentRoots();
			sourceFolders = new ArrayList<IPackageFragmentRoot>();
			for (IPackageFragmentRoot fragment : packageRoots){
				if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE){
					sourceFolders.add(fragment);
				}
			}
		} catch (JavaModelException e) {
			MessageDialog.openError(shell, "Test selection error", "Unable to determine the package to which the selected tests belong.");
		}
	}
	
	private String getPackageDeclaration(Shell shell, String projectRelativePath){
		findSourceFolders(shell);
		for (IPackageFragmentRoot pfr : sourceFolders){
			String folderName = pfr.getElementName();
			if (projectRelativePath.contains(folderName)){
				int index = projectRelativePath.indexOf(folderName);
				String partialPath = projectRelativePath.substring(index+folderName.length()+1);
				index = partialPath.indexOf(".java");
				if (index != -1){
					String qualifiedPath = partialPath.substring(0, index).replace("/", ".");
					return qualifiedPath;
				}
			}
		}
		return ERROR_FILE_NOT_IN_PROJECT;
	}
		
	private void doCleanUpEclipse(Shell shell) {
		try {
			tempProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			tempProject.delete(true, true, null);
		} catch (CoreException e1) {
			MessageDialog.openError(shell, "Cleanup failure", "Could not delete the temporary project from the workspace");
		}
	 }
	  
	private void doCleanUpGit(Shell shell, List<String> branchNames) {
		try {
			API.cleanBranches(branchNames);
		} catch (GitAPIException e) {
			MessageDialog.openError(shell, "Cleanup failure", "Could not fully delete the created branches from the git repository");
		}
	}
	
}