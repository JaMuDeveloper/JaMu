package jgit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

public interface JGitHandlerInterface {
	
	public Repository createRepo(String directory, String repoName) throws IllegalStateException, IOException, GitAPIException;
	public Repository createRepo(File f) throws IOException, IllegalStateException, GitAPIException;
	
	public Repository loadRepo(String directory, String repoName) throws IOException;
	public Repository loadRepo(File f) throws IOException;

	public void setGitToRepo(Repository repo);

	public void addFile(File f) throws IOException, NoFilepatternException,	GitAPIException;
	public void addFileAndCommit(File f) throws NoFilepatternException, IOException, GitAPIException;
	public void addFileAndCommit(File f, String message) throws NoFilepatternException, IOException, GitAPIException;
	
	public void deleteFile(File f) throws NoFilepatternException, GitAPIException;
	public void deleteFileAndCommit(File f) throws NoFilepatternException, GitAPIException;
	public void deleteFileAndCommit(File f, String message)	throws NoFilepatternException, GitAPIException;
	
	public void commit() throws GitAPIException;
	public void commit(String message) throws GitAPIException;
	
	public List<String> listFiles() throws IOException;
	public List<String> listFiles(boolean includeDirs) throws IOException;
	
	//Should only return a file with an exact match on the filename
	public File getFile(String filename) throws IOException;
	public File getFile(String filename, boolean includeDirs) throws IOException;
	
	//Should return the first file whose name contains the give filename
	public File findFile(String filename, boolean includeDirs) throws IOException;
	public File findFile(String filename) throws IOException;

	public void switchBranch(String branchName) throws GitAPIException;
	public void createBranch(String branchName) throws GitAPIException;
	public void deleteBranch(String branchName) throws GitAPIException;
	//Should delete all branches except for the master
	public void cleanBranches() throws GitAPIException;
	//Should delete all given branches, but never the master
	public void cleanBranches(List<String> branchNames) throws GitAPIException;
	
	public String getCurrentBranch() throws IOException;
	public String getRealBranchName(Ref branchName);
	public List<Ref> listBranches() throws GitAPIException;
	public boolean branchExists(String branchName) throws GitAPIException;
		
	public void deleteRepo() throws IOException;
	public JGitHandlerInterface resetRepo() throws IOException, GitAPIException;

	public boolean isBare();

}