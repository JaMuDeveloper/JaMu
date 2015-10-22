package jamu.handlers;

import jamu.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import mutationEngine.Mutant;
import mutationEngine.MutationEngine;
import output.ConsoleController;
import output.ResultAnalyzer;
import output.ResultOutputter;
import output.ResultStorage;

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import output.Logger;
import testExecutor.Result;
import testExecutor.TestExecutor;
import visitors.VisitorList;

public class TestHandler extends AbstractHandler {

	private IProject tempProject;
	private IProject projectFolder;
	private IJavaProject javaProjectFolder;
	private IJavaProject javaTempProject;
	private List<IPackageFragmentRoot> sourceFolders;
	private Logger logger;
	private List<Mutant> mutantStorage;
	private static String JAMU_PROJECT_NAME;
	private static String LOGGING;
	private static String PATH_TO_LOG_FILE;
	private boolean EXPORTING_ON;
	private String PATH_TO_EXPORT_DIR;
	private static boolean MUTANT_STATUS;
	private static boolean TEST_RESULTS;
	private static boolean TEST_PERFORMANCE;
	private static boolean PROGRESS_MESSAGES;
	
	private final static String ERROR_FILE_NOT_IN_PROJECT = "THE_FILE_IS_NOT_PRESENT_IN_THE_PROJECT_OR_IS_NOT_A_JAVA_FILE";
	public final static String CONSOLE_NAME = "Console";
	
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
			tempProject = createTempProject(projectFolder,shell);
			if (tempProject == null){
				return null;
			}
			javaTempProject = JavaCore.create(tempProject);
			List<IFile> selectedFiles = selectFilesFromProject(shell,javaProjectFolder,"Class selection", "Select the java files to mutate:");
			if (selectedFiles == null){
				MessageDialog.openInformation(shell, "Error", "No files have been selected.");
		    	doCleanUpEclipse(shell);
				return null;
			}
			List<ICompilationUnit> selectedCompilationUnits = convertToCompilation(shell, selectedFiles);
			List<String> selectedMutators = selectMutators(shell);
			if (selectedMutators == null){
				MessageDialog.openInformation(shell, "Error", "No mutation rules have been selected.");
		    	doCleanUpEclipse(shell);
				return null;
			}
			List<String> selectedTests = getPackageIncludedPaths(shell, selectFilesFromProject(shell,javaProjectFolder,"Test selection","Select the tests to run:"));
			if (selectedTests == null){
				MessageDialog.openInformation(shell, "Error", "No tests have been selected.");
		    	doCleanUpEclipse(shell);
				return null;
			}
			if (selectedTests.contains(ERROR_FILE_NOT_IN_PROJECT)){
		    	doCleanUpEclipse(shell);
				return null;
			}
			ConsoleController console = new ConsoleController(CONSOLE_NAME);
			try{
				success = generateMutants(shell, console, selectedMutators, selectedCompilationUnits);
			}
			catch (OutOfMemoryError e){
				MessageDialog.openError(shell, "Error", "JaMu ran out of memory during the mutant generation.");
				doCleanUpEclipse(shell);
				return null;
			}
			if (!success){
		    	doCleanUpEclipse(shell);
				return null;
			}
			ResultStorage resultStorage = new ResultStorage();
			runTests(shell, selectedTests, resultStorage);
			ResultOutputter resultOutputter = new ResultOutputter(logger, console, MUTANT_STATUS, TEST_RESULTS, TEST_PERFORMANCE);
			resultOutputter.doOutput(resultStorage);
	    	doCleanUpEclipse(shell);
		    if (logger.isEnabled()){
		    	logger.doneLogging();
		    }
		    console.closeOutputStream();
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
	    	JAMU_PROJECT_NAME = it.next();
		    LOGGING = it.next();
		    PATH_TO_LOG_FILE = it.next() + "/JaMu_Log_File.txt";
		    EXPORTING_ON = it.next().equals("true");
		    PATH_TO_EXPORT_DIR = it.next();
		    MUTANT_STATUS = it.next().equals("true");
		    TEST_RESULTS = it.next().equals("true");
		    TEST_PERFORMANCE = it.next().equals("true");
		    PROGRESS_MESSAGES = it.next().equals("true");
		    logger = new Logger(PATH_TO_LOG_FILE, LOGGING);
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
			MessageDialog.openError(shell, "Error", "Unable to copy files to the temporary project.");
			return null;
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
		dialog.addFilter(new ShowOnlyJavaFiles(javaProjectFolder.getElementName()));
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
			return selectedFiles;
		}
		return null;
	}
	
	private List<ICompilationUnit> convertToCompilation(Shell shell, List<IFile> selectedFiles){
		List<ICompilationUnit> selectedCompilationUnits = new ArrayList<ICompilationUnit>();
		List<String> stringList = new ArrayList<String>();
		for (IFile f : selectedFiles){
			stringList.add(f.getName());
		}
		try {
			IPackageFragmentRoot[] packageFragmentRoots = javaProjectFolder.getAllPackageFragmentRoots();
			for(int i = 0; i < packageFragmentRoots.length; i++) {
				IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
				IJavaElement[] fragments = packageFragmentRoot.getChildren();
				for(int j = 0; j < fragments.length; j++) {
					IPackageFragment fragment = (IPackageFragment)fragments[j];
					IJavaElement[] javaElements = fragment.getChildren();
					for(int k = 0; k < javaElements.length; k++) {
						IJavaElement javaElement = javaElements[k];
						if(javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
							ICompilationUnit icu = (ICompilationUnit) javaElement;
							if (stringList.contains(icu.getElementName())){
								selectedCompilationUnits.add((ICompilationUnit)javaElement);
							}
						}
					}
				}
			}
		}
	    catch(Exception e) {
	    	MessageDialog.openError(shell, "Conversion error", "Failed to convert the selected files");
	    }
		return selectedCompilationUnits;
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
		List<String> categories = VisitorList.getCategories();
		java.util.Collections.sort(categories);
		List<String> mutatorsToRun = new ArrayList<String>();
		ListSelectionDialog dialog = new ListSelectionDialog(shell, categories , new ArrayContentProvider(), new LabelProvider(), "Select the mutation rules to run:");
		dialog.setTitle("Mutation rule selection");
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

	private List<String> getPackageIncludedPaths(Shell shell, List<IFile> files){
		List<String> newList = new ArrayList<String>();
		for (IFile f : files){
			String qualifiedPath = getPackageDeclaration(shell, f.getProjectRelativePath().toString());
			if (qualifiedPath.equals(ERROR_FILE_NOT_IN_PROJECT)){
				newList.add(qualifiedPath);
				MessageDialog.openError(shell, "Test selection error", "Unable to determine the package to which the selected tests belong.");
				return newList;
			}
			newList.add(qualifiedPath);
		}
		return newList;
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
	
	private boolean generateMutants(Shell shell, ConsoleController console, List<String> mutationRules, List<ICompilationUnit> filesToMutate) {
		mutantStorage = new ArrayList<Mutant>();
		MutationEngine m = new MutationEngine(console, mutationRules, filesToMutate, mutantStorage, projectFolder, tempProject, PROGRESS_MESSAGES);
		try {
			m.generateMutants();
		} catch (JavaModelException | IOException e) {
			MessageDialog.openError(shell, "Mutant generation error", "Unable to generate all the mutants.");
		}
		return true;
	}
	
	private boolean runTests(Shell shell, List<String> selectedTests, ResultStorage resultStorage){
		try {
			TestExecutor originalExecutor = new TestExecutor(javaProjectFolder);
			Map<String,Result> originalResults = executeTestsInProject(shell, originalExecutor,selectedTests);
			if (originalResults == null){
				return false;
			}
			resultStorage.setOriginalResults(originalResults);
			Map<String,Map<String,Result>> mutatorResults = new HashMap<String,Map<String,Result>>();
			for (Mutant mu: mutantStorage){
				boolean success = loadMutant(mu, true);
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
				mutatorResults.put(mu.getName(),mutatedResults);
				loadMutant(mu, false);
			}
			resultStorage.setMutatorResults(mutatorResults);
			ResultAnalyzer ra = new ResultAnalyzer(resultStorage);
			ra.analyze(mutatorResults);
		} catch (MalformedURLException | CoreException | ReflectiveOperationException | FileNotFoundException e1) {
			MessageDialog.openError(shell, "Execution error", "Failed to run all tests");
			return false;
		}
		return true;
	}

	private boolean loadMutant(Mutant mu, boolean loadNewSource) throws FileNotFoundException {
		String root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		File file = new File(root+mu.getLocation());
		if (loadNewSource){
			writeToFile(file, mu.getNewSource());
			if (EXPORTING_ON){
				File exportFile = new File(PATH_TO_EXPORT_DIR + "/" + mu.getName() + ".txt");
				writeToFile(exportFile, mu.getNewSource());
			}
		}
		else{
			writeToFile(file, mu.getOldSource());
		}
		return true;
	}

	private void writeToFile(File javaFile, String content) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(javaFile);
        writer.print(content);
        writer.close();
	}
	
	private Map<String, Result> executeTestsInProject(Shell shell, TestExecutor te, List<String> selectedTests){
		try {
			Map<String,Result> results = new HashMap<String,Result>();
			for (String fileName : selectedTests){
				Result r = te.runAllTests(fileName);
				results.put(fileName, r);		
			}
			return results;
		} catch (IOException | ReflectiveOperationException  e) {
			MessageDialog.openError(shell, "Test execution failure", "Failed to run all tests");
			return null;
		}
	}
	
	private void doCleanUpEclipse(Shell shell) {
		try {
			tempProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			tempProject.delete(true, true, null);
		} catch (CoreException e1) {
			MessageDialog.openError(shell, "Cleanup failure", "Could not delete the temporary project from the workspace");
		}
	 }
	  
	
}