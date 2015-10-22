package mutation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import jgit.JGitHandlerLocal;
import jamu.handlers.TestHandler;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;

import output.Logger;
import visitors.CustomVisitor;
import visitors.VisitorList;

public class MutationEngine {
	
	private List<String> mutatorLabels;
	private String source;
	private IProject originalProject;
	private IProject tempProject;
	private List<File> filesToMutate;
	private JGitHandlerLocal API;
	private boolean verboseOutput;
	private int mutatedFilesCounter;
	private Logger logger;
	  
	private static String errorMessage = "ERROR_MESSAGE_UNABLE_TO_OBTAIN_RELATIVE_PATH_OF_FILE";
	
	public MutationEngine(List<String> mutatorLabels, JGitHandlerLocal API, List<File> filesToMutate, IProject originalProject, IProject tempProject, boolean verboseOutput, Logger logger){
		this.mutatorLabels = mutatorLabels;
		this.API = API;
		this.filesToMutate = filesToMutate;
		this.originalProject = originalProject;
		this.tempProject = tempProject;
		this.verboseOutput = verboseOutput;
		this.logger = logger;
	}
	
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
		
	public boolean start(List<String> branchNameStorage) throws IOException, JavaModelException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, GitAPIException {
		List<CounterContainer> counterList = new ArrayList<CounterContainer>();
		for (String label : mutatorLabels){
			counterList.add(new CounterContainer(label));
		}			
		for (File f : filesToMutate){
			source = readFile(f.getPath(),Charset.defaultCharset());
			tryToOutput("Now generating mutants from: " + f.getName());
			CompilationUnit cu = generateAST(source);
			VisitorList vl = new VisitorList(cu);
			int totalMutantsForFile = 0;
			for (CounterContainer cc : counterList){
				tryToOutput("Applying the " + vl.get(cc.mutatorLabel).getVisitorLabel() + " mutation rule");
				if (!handleVisitor(cu, vl.get(cc.mutatorLabel), cc , f, branchNameStorage)){
					return false;
				}
				totalMutantsForFile += mutatedFilesCounter;
				API.commit();
			}
			tryToOutput("Finished with generating mutants from: " + f.getName() + ". Generated " + totalMutantsForFile + " mutants\n");
			
		}
		return true;
	}
	
	public boolean handleVisitor(CompilationUnit cu, CustomVisitor v, CounterContainer cc, File f, List<String> branchNameStorage) throws JavaModelException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, GitAPIException, IOException {
		if (v == null){
			//TODO error handling, tried to get a visitor belonging to a nonexistant label
			return false;
		}
		mutatedFilesCounter = 0;
		cu.accept(v);
		String[] newSources = v.generateNewSources(source);
		String relativePath = getRelativePath(f);
		if (relativePath.equals(errorMessage)){
			return false;
		}
		File writeTo = new File (tempProject.getLocation().toString() + relativePath);
		for (String source : newSources){
			writeToFile(writeTo,source);
			mutatedFilesCounter += 1;
			String newBranchName = v.getVisitorType() + cc.branchCounter;
			Long timerStart = System.currentTimeMillis();
			if (!API.getCurrentBranch().equals(TestHandler.JAMU_BRANCHNAME)){
				API.switchBranch(TestHandler.JAMU_BRANCHNAME);
			}
			System.out.println("Timer A: " + (System.currentTimeMillis()-timerStart) + " ms");
			timerStart = System.currentTimeMillis();
			API.createBranch(newBranchName);
			System.out.println("Timer B: " + (System.currentTimeMillis()-timerStart) + " ms");
			timerStart = System.currentTimeMillis();
			API.switchBranch(newBranchName);
			System.out.println("Timer C: " + (System.currentTimeMillis()-timerStart) + " ms");
			timerStart = System.currentTimeMillis();
			API.addFile(tempProject.getLocation().toFile());
			//API.addFileAndCommit(tempProject.getLocation().toFile(),"commited mutations of type " + v.getVisitorType() + " on file " + f.getName());
			System.out.println("Timer D: " + (System.currentTimeMillis()-timerStart) + " ms");
			timerStart = System.currentTimeMillis();
			API.switchBranch(TestHandler.JAMU_BRANCHNAME);
			System.out.println("Timer E: " + (System.currentTimeMillis()-timerStart) + " ms");
			cc.branchCounter++;
			branchNameStorage.add(newBranchName);
		}
		return true;
	}
	
	private String getRelativePath(File f) {
		String filePath = f.getPath().replace('\\','/');
		String originalProjectPath = originalProject.getLocation().toString();
		if (filePath.indexOf(originalProjectPath) == -1){
			//TODO error handling
			return errorMessage;
		}
		return filePath.substring(originalProjectPath.length());
	}

	public static CompilationUnit generateAST(String code) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS8); 
		parser.setSource(code.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setResolveBindings(true);
	    parser.setStatementsRecovery(true);
	    return (CompilationUnit) parser.createAST(null);
	}
	
	public File writeToFile(File javaFile, String content) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(javaFile);
        writer.print(content);
        writer.close();
        return javaFile;
	}
	  
	private void tryToOutput(String s) {
		if (this.verboseOutput) {
			System.out.println(s);
	    }
	    if (logger.isEnabled()) {
	    	logger.log(s + "\n");
	    }
	  }
	
	private class CounterContainer {
		public String mutatorLabel;
		public int branchCounter;
		
		public CounterContainer(String mutatorLabel){
			this.mutatorLabel = mutatorLabel;
			branchCounter = 0;
		}
		
	}
	
}