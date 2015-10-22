package mutationEngine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.ui.console.MessageConsoleStream;
import output.ConsoleController;
import visitors.CustomVisitor;
import visitors.VisitorList;

public class MutationEngine {
	
	private List<String> mutationRules;
	private String source;
	private IProject originalProject;
	private IProject tempProject;
	private List<ICompilationUnit> filesToMutate;
	private List<Mutant> mutantStorage;
	private boolean progressMessages;
	private int mutatedFilesCounter;
	private ConsoleController console;
	private MessageConsoleStream out;
	
	public MutationEngine(ConsoleController console, List<String> mutationRules, List<ICompilationUnit> filesToMutate, List<Mutant> mutantStorage, IProject originalProject, IProject tempProject, boolean progressMessages){
		this.mutationRules = mutationRules;
		this.filesToMutate = filesToMutate;
		this.originalProject = originalProject;
		this.tempProject = tempProject;
		this.mutantStorage = mutantStorage;
		this.progressMessages = progressMessages;
		this.console = console;
		out = console.getOutputStream();
	}
	
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
		
	public boolean generateMutants() throws IOException, JavaModelException {
		List<CounterContainer> counterList = new ArrayList<CounterContainer>();
		for (String label : VisitorList.transformToLabels(mutationRules)){
			counterList.add(new CounterContainer(label));
		}
		int totalMutants = 0;
		String currentCategory = "";
		console.setFocusOnConsole();
		tryToOutput("MUTANT GENERATION\n",false);
		tryToOutput("------------------\n",false);
		for (ICompilationUnit icu : filesToMutate){
			source = icu.getSource();
			tryToOutput("Now generating mutants from: " + icu.getElementName() + "\n", true);
			CompilationUnit cu = generateAST(source);
			VisitorList vl = new VisitorList(cu);
			int totalMutantsForFile = 0;
			for (CounterContainer cc : counterList){
				CustomVisitor v = vl.get(cc.mutatorLabel);
				if (currentCategory != v.getVisitorCategory()){
					currentCategory = v.getVisitorCategory();
					tryToOutput("\tApplying the " + currentCategory + " mutation rules.\n", true);
				}
				if (!handleVisitor(cu, v, cc , icu, source)){
					return false;
				}
				totalMutantsForFile += mutatedFilesCounter;
			}
			totalMutants += totalMutantsForFile;
			tryToOutput("Finished with generating mutants from: " + icu.getElementName() + ".", false);
			tryToOutput(" Generated " + totalMutantsForFile + " mutants.", true);
			tryToOutput("\n",false);
		}
		tryToOutput("Generated " + totalMutants + " mutants in total.\n",true);
		tryToOutput("------------------\n\n",false);
		return true;
	}
	
	public boolean handleVisitor(CompilationUnit cu, CustomVisitor v, CounterContainer cc, ICompilationUnit icu, String oldSource) {
		if (v == null){
			return false;
		}
		mutatedFilesCounter = 0;
		cu.accept(v);
		String[] newSources = v.generateNewSources(source);
		for (String source : newSources){
			mutatedFilesCounter += 1;
			String newName = v.getVisitorType() + cc.branchCounter;
			cc.branchCounter++;
			mutantStorage.add(new Mutant(oldSource,source,adjustPath(icu.getPath().toString()),newName));
		}
		return true;
	}
	
	private String adjustPath(String originialPath) {
		return originialPath.replace(originalProject.getName(), tempProject.getName());
	}

	public static CompilationUnit generateAST(String code) throws IOException{
		ASTParser parser = ASTParser.newParser(AST.JLS8); 
		parser.setSource(code.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setResolveBindings(true);
	    parser.setStatementsRecovery(true);
	    return (CompilationUnit) parser.createAST(null);
	}
		  
	private void tryToOutput(String s,  boolean isProgressMessage) {
		if (progressMessages && isProgressMessage) {
			out.print(s);
	    }
		else if (!isProgressMessage){
			out.print(s);
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