package jamu.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ShowOnlyJavaFiles extends ViewerFilter {
	
   private String projectName;
   
   ShowOnlyJavaFiles(String projectName){
	   this.projectName = projectName;
   }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
	if (element instanceof IProject){
		IProject pj = (IProject) element;
		if (projectName.equals(pj.getName())){
			return true;
		}
	}
	if (element instanceof IFolder){
		IFolder f = (IFolder) element;
		return containsJavaFiles(f);
	}
	if (element instanceof IFile){
		IFile f = (IFile) element;
		return (f.getName().contains(".java"));
	}
	return false;
  }
  
  public static boolean containsJavaFiles(IFolder folder) {
	  try {
			IResource[] childResources = folder.members();
			for (IResource r : childResources){
				if (r instanceof IFile){
					IFile f = (IFile) r;
					return (f.getName().contains(".java"));				
				}
				if (r instanceof IFolder){
					IFolder fd = (IFolder) r;
					if (containsJavaFiles(fd)){
						return true;
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
  }
 
} 