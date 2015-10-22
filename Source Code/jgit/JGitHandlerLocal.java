package jgit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class JGitHandlerLocal implements JGitHandlerInterface {

	private File gitDir;
	private Repository localRepo;
	private Git git;

	public JGitHandlerLocal(String directory, String repoName) throws GitAPIException, IOException {
		gitDir = new File(directory + "/" + repoName);
		init();
		git = new Git(localRepo);
		if (headIsWrong()){
			makeRepoNotBare();
		}
	}

	private void init() throws GitAPIException, IOException {
		try {
			Repository repo = loadRepo();
			localRepo = repo;
		} catch (IOException e) {
			createRepo();
			Repository repo = loadRepo();
			localRepo = repo;
		}
	}

	public Repository createRepo(String directory, String repoName) throws IOException, GitAPIException {
		return createRepo(new File(directory + "/" + repoName));
	}
	public Repository createRepo(File f) throws IOException, GitAPIException {
		Git.init().setBare(false).setDirectory(f).call();
		Repository repository = FileRepositoryBuilder.create(new File(f.getAbsolutePath()));
		return repository;
	}
	private Repository createRepo() throws IOException, GitAPIException {
		return createRepo(gitDir);
	}

	public Repository loadRepo(String directory, String repoName) throws IOException {
		return loadRepo(new File(directory + "/" + repoName + "/.git"));
	}
	public Repository loadRepo(File f) throws IOException {
		Git git = Git.open(f);
		return git.getRepository();
	}
	private Repository loadRepo() throws IOException {
		return loadRepo(new File(gitDir.getAbsolutePath() + "/.git"));
	}
		
	public void setGitToRepo(Repository repo){
		localRepo = repo;
		git = new Git(localRepo);
	}

	private void add(File f) throws GitAPIException {
		git.add().addFilepattern(f.getName()).call();
	}
	public void addFile(File f) throws IOException, GitAPIException {
		if (f.isDirectory()) {
			FileUtils.copyDirectory(f, new File(gitDir.getAbsolutePath() + "/"
					+ f.getName()));
		} else if (!f.isDirectory()) {
			FileUtils.copyFile(f,
					new File(gitDir.getAbsolutePath() + "/" + f.getName()));
		}
		add(f);
	}
	public void addFileAndCommit(File f) throws IOException, GitAPIException {
		if (f.isDirectory()) {
			addFileAndCommit(f, "Added directory with name " + f.getName());
		}
		addFileAndCommit(f, "Added file with name " + f.getName());
	}
	public void addFileAndCommit(File f, String message)
			throws IOException, GitAPIException {
		addFile(f);
		git.commit().setMessage(message).call();
	}	
	
	public void deleteFile(File f) throws GitAPIException {
		git.rm().setCached(false).addFilepattern(f.getName()).call();
	}
	public void deleteFileAndCommit(File f) throws GitAPIException {
		if (f.isDirectory()) {
			deleteFileAndCommit(f, "Trying to delete directory " + f.getName());
		}
		deleteFileAndCommit(f, "Trying to delete file " + f.getName());
	}
	public void deleteFileAndCommit(File f, String message)
			throws GitAPIException {
		deleteFile(f);
		git.commit().setMessage(message).call();
	}

	public void commit() throws GitAPIException {
		commit("Commited all changes");
	}
	public void commit(String message) throws GitAPIException {
		git.commit().setAll(true).setMessage(message).call();
	}

	public List<String> listFiles() throws IOException {
		return listFiles(false);
	}
	public List<String> listFiles(boolean includeDirs) throws IOException {
		List<String> files = new ArrayList<String>();
		Ref head;
		head = localRepo.getRef("HEAD");
		RevWalk walk = new RevWalk(localRepo);
		ObjectId objID = head.getObjectId();
		if (objID == null) {
			return files;
		}
		RevCommit commit = walk.parseCommit(objID);
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(localRepo);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(false);
		while (treeWalk.next()) {
			if (treeWalk.isSubtree()) {
				if (includeDirs) {
					files.add(treeWalk.getPathString());
				}
				treeWalk.enterSubtree();
			} else {
				files.add(treeWalk.getPathString());
			}
		}
		return files;
	}
	
	private File getFile(String filename, boolean exactMatch, boolean includeDirs) throws IOException {
		List<String> fileList = listFiles(includeDirs);
		for (String file : fileList) {
			if (exactMatch) {
				if (file.equals(filename)) {
					return new File(gitDir.getAbsolutePath() + "/" + file);
				}
			} else {
				if (file.contains(filename)) {
					return new File(gitDir.getAbsolutePath() + "/" + file);
				}
			}
		}
		return null;
	}
	public File getFile(String filename) throws IOException {
		return getFile(filename, true, false);
	}
	public File getFile(String filename, boolean includeDirs)
			throws IOException {
		return getFile(filename, true, includeDirs);
	}
	public File findFile(String filename) throws IOException {
		return getFile(filename, false, false);
	}
	public File findFile(String filename, boolean includeDirs)
			throws IOException {
		return getFile(filename, false, includeDirs);
	}

	public void switchBranch(String branchName)
			throws GitAPIException {
		git.checkout().setName(branchName).call();
	}
	public void createBranch(String branchName) throws GitAPIException {
		git.branchCreate().setName(branchName).setForce(true).call();
	}	
	public void deleteBranch(String branchName) throws GitAPIException {
		git.branchDelete().setBranchNames(branchName).setForce(true).call();
	}
	public void cleanBranches() throws GitAPIException{
		List<String> branchNames = new ArrayList<String>();
		cleanBranches(branchNames);
	}
	public void cleanBranches(List<String> branchNames) throws GitAPIException {
		switchBranch("master");
		List<Ref> branches = listBranches();
		for (Ref branch : branches){
			String branchName = getRealBranchName(branch);
			if (!branchName.equals("master") && (branchNames.contains(branchName) || branchNames.size() == 0)){
				deleteBranch(branchName);
			}
		}
	}
	
	public String getCurrentBranch() throws IOException {
		return localRepo.getBranch();
	}
	public String getRealBranchName(Ref branch) {
		String explicitName = branch.getName();
		return explicitName.substring(explicitName.indexOf("heads") + "heads".length() + 1);
	}
	public List<Ref> listBranches() throws GitAPIException {
		return git.branchList().call();
	}
	public boolean branchExists(String branchName) throws GitAPIException {
		List<Ref> call = listBranches();
		for (Ref ref : call) {
			if (getRealBranchName(ref).equals(branchName)) {
				return true;
			}
		}
		return false;
	}

	public void deleteRepo() throws IOException {
		localRepo.close();
		FileUtils.deleteDirectory(gitDir);
	}
	public JGitHandlerLocal resetRepo() throws IOException, GitAPIException {
		deleteRepo();
		JGitHandlerLocal newHandler = new JGitHandlerLocal(gitDir.getParent(),
				gitDir.getName());
		return newHandler;
	}
	
	public boolean isBare(){
		return localRepo.isBare();
	}
	
	private boolean headIsWrong() throws GitAPIException{
		try {
			createBranch("Head_test_branch");
			deleteBranch("Head_test_branch");
			return false;
		} catch (RefNotFoundException e) {
			return true;
		} 		
	}
	private void makeRepoNotBare() throws IOException, GitAPIException{
		File f = File.createTempFile("git", null);
		addFileAndCommit(f,"Added temp file for initialization");
		deleteFileAndCommit(f,"Removed temp file for initialization");
		localRepo.writeMergeCommitMsg("Initialization");
	}
	
}