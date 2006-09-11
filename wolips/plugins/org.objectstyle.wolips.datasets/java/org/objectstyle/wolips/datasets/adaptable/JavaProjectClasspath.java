/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2004 The ObjectStyle Group,
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.wolips.datasets.adaptable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.objectstyle.wolips.datasets.DataSetsPlugin;
import org.objectstyle.wolips.datasets.resources.IWOLipsModel;
import org.objectstyle.wolips.variables.VariablesPlugin;

/**
 * @author ulrich
 * @deprecated Use org.objectstyle.wolips.core.* instead.
 */
public class JavaProjectClasspath extends AbstractJavaProjectAdapterType {
	/**
	 * @param project
	 */
	protected JavaProjectClasspath(IProject project) {
		super(project);
	}

	/**
	 * Method addNewSourcefolderToClassPath.
	 * 
	 * @param newSourceFolder
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public void addNewSourcefolderToClassPath(IFolder newSourceFolder, IProgressMonitor monitor) throws InvocationTargetException {
		// add source classpath entry for project
		IJavaProject actualJavaProject = null;
		IClasspathEntry[] oldClassPathEntries = null;
		IClasspathEntry[] newClassPathEntries = null;
		try {
			actualJavaProject = JavaCore.create(newSourceFolder.getProject());
			oldClassPathEntries = actualJavaProject.getRawClasspath();
		} catch (JavaModelException e) {
			actualJavaProject = null;
			oldClassPathEntries = null;
			throw new InvocationTargetException(e);
		}
		newClassPathEntries = new IClasspathEntry[oldClassPathEntries.length + 1];
		System.arraycopy(oldClassPathEntries, 0, newClassPathEntries, 1, oldClassPathEntries.length);
		newClassPathEntries[0] = JavaCore.newSourceEntry(newSourceFolder.getFullPath());
		try {
			actualJavaProject.setRawClasspath(newClassPathEntries, monitor);
		} catch (JavaModelException e) {
			actualJavaProject = null;
			oldClassPathEntries = null;
			newClassPathEntries = null;
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Method getSubprojectSourceFolder. Searches classpath source entries for
	 * correspondending subproject source folder (first found source folder in
	 * subproject folder)
	 * 
	 * @param subprojectFolder
	 * @param forceCreation -
	 *            create folder if necessary
	 * @return IFolder
	 */
	public IFolder getSubprojectSourceFolder(IFolder subprojectFolder, boolean forceCreation) {
		// ensure that the folder is a subproject
		if (!IWOLipsModel.EXT_SUBPROJECT.equals(subprojectFolder.getFileExtension())) {
			IFolder parentFolder = this.getParentFolderWithPBProject(subprojectFolder);
			// this belongs to the project and not a subproject
			if (parentFolder == null)
				return subprojectFolder.getProject().getFolder(this.getProjectSourceFolder().getProjectRelativePath());
			subprojectFolder = parentFolder;
		}
		List subprojectFolders = getSubProjectsSourceFolder();
		for (int i = 0; i < subprojectFolders.size(); i++) {
			if (((IFolder) subprojectFolders.get(i)).getFullPath().removeLastSegments(1).equals(subprojectFolder.getFullPath())) {
				return (IFolder) subprojectFolders.get(i);
			}
		}
		if (forceCreation) {
			// no folder found - create new source folder
			IFolder subprojectSourceFolder = subprojectFolder.getProject().getFolder(subprojectFolder.getName() + "/" + IWOLipsModel.EXT_SRC);
			if (!subprojectSourceFolder.exists()) {
				try {
					subprojectSourceFolder.create(true, true, null);
				} catch (CoreException e) {
					DataSetsPlugin.getDefault().getPluginLogger().log(e);
				}
			} // add folder to classpath
			try {
				this.addNewSourcefolderToClassPath(subprojectSourceFolder, null);
			} catch (InvocationTargetException e) {
				DataSetsPlugin.getDefault().getPluginLogger().log(e);
			}
			return subprojectSourceFolder;
		}
		return null;
	}

	/**
	 * Method getProjectSourceFolder. Searches classpath source entries for
	 * project source folder. The project source folder is the first found
	 * source folder the project container contains.
	 * 
	 * @return IContainer found source folder
	 */
	public IContainer getProjectSourceFolder() {
		IClasspathEntry[] classpathEntries;
		try {
			classpathEntries = this.getIJavaProject().getRawClasspath();
		} catch (JavaModelException e) {
			DataSetsPlugin.getDefault().getPluginLogger().log(e);
			return null;
		}
		for (int i = 0; i < classpathEntries.length; i++) {
			if (IClasspathEntry.CPE_SOURCE == classpathEntries[i].getEntryKind()) {
				// source entry found
				if (classpathEntries[i].getPath() != null && classpathEntries[i].getPath().removeLastSegments(1).equals(this.getIProject().getFullPath())) {
					// source folder's parent is project
					// project source folder found
					return this.getIProject().getWorkspace().getRoot().getFolder(classpathEntries[i].getPath());
				}
				/*
				 * if (classpathEntries[i].getPath() != null &&
				 * classpathEntries[i].getPath().toString().indexOf( "." +
				 * IWOLipsPluginConstants.EXT_SUBPROJECT + "." +
				 * IWOLipsPluginConstants.EXT_SRC) == -1) { // non subproject
				 * entry found if (classpathEntries[i].getPath().segmentCount() >
				 * 1) { return project.getWorkspace().getRoot().getFolder(
				 * classpathEntries[i].getPath()); } break; }
				 */
			}
		}
		// no source folder found -> create new one
		IFolder projectSourceFolder = this.getIProject().getFolder(IWOLipsModel.EXT_SRC);
		if (!projectSourceFolder.exists()) {
			try {
				projectSourceFolder.create(true, true, null);
			} catch (CoreException e) {
				DataSetsPlugin.getDefault().getPluginLogger().log(e);
			}
		}
		// add to classpath
		try {
			this.addNewSourcefolderToClassPath(projectSourceFolder, null);
		} catch (InvocationTargetException e) {
			DataSetsPlugin.getDefault().getPluginLogger().log(e);
		}
		return projectSourceFolder;
	}

	/**
	 * Method getSubProjectsSourceFolder. Searches classpath source entries for
	 * all source folders who's parents are NOT project.
	 * 
	 * @return List
	 */
	public List getSubProjectsSourceFolder() {
		IClasspathEntry[] classpathEntries;
		ArrayList foundFolders = new ArrayList();
		try {
			classpathEntries = this.getIJavaProject().getRawClasspath();
		} catch (JavaModelException e) {
			DataSetsPlugin.getDefault().getPluginLogger().log(e);
			return null;
		}
		for (int i = 0; i < classpathEntries.length; i++) {
			if (IClasspathEntry.CPE_SOURCE == classpathEntries[i].getEntryKind()) {
				// source entry found
				if (classpathEntries[i].getPath() != null && !classpathEntries[i].getPath().removeLastSegments(1).equals(this.getIProject().getFullPath())) {
					// source folder's parent is not project
					// project source folder found
					foundFolders.add(this.getIProject().getWorkspace().getRoot().getFolder(classpathEntries[i].getPath()));
				}
				/*
				 * if (classpathEntries[i].getPath() != null &&
				 * classpathEntries[i].getPath().toString().indexOf( "." +
				 * IWOLipsPluginConstants.EXT_SUBPROJECT + "/" +
				 * IWOLipsPluginConstants.EXT_SRC) != -1) { foundFolders.add(
				 * project.getWorkspace().getRoot().getFolder(
				 * classpathEntries[i].getPath())); }
				 */
			}
		}
		return foundFolders;
	}

	/**
	 * Method removeSourcefolderFromClassPath.
	 * 
	 * @param folderToRemove
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public void removeSourcefolderFromClassPath(IFolder folderToRemove, IProgressMonitor monitor) throws InvocationTargetException {
		if (folderToRemove != null) {
			IClasspathEntry[] oldClassPathEntries;
			try {
				oldClassPathEntries = this.getIJavaProject().getRawClasspath();
			} catch (JavaModelException e) {
				oldClassPathEntries = null;
				throw new InvocationTargetException(e);
			}
			IClasspathEntry[] newClassPathEntries = new IClasspathEntry[oldClassPathEntries.length - 1];
			int offSet = 0;
			for (int i = 0; i < oldClassPathEntries.length; i++) {
				if (IClasspathEntry.CPE_SOURCE == oldClassPathEntries[i].getEntryKind() && oldClassPathEntries[i].getPath().equals(folderToRemove.getFullPath())) {
					offSet = 1;
				} else {
					newClassPathEntries[i - offSet] = oldClassPathEntries[i];
				}
			}
			if (offSet != 0) {
				try {
					this.getIJavaProject().setRawClasspath(newClassPathEntries, monitor);
				} catch (JavaModelException e) {
					oldClassPathEntries = null;
					newClassPathEntries = null;
					throw new InvocationTargetException(e);
				}
			}
		}
	}

	/**
	 * Method addFrameworkListToClasspathEntries.
	 * 
	 * @param frameworkList
	 * @throws JavaModelException
	 * @return Returns the array of classpath entries.
	 */
	public IClasspathEntry[] addFrameworkListToClasspathEntries(List frameworkList) throws JavaModelException {
		IClasspathEntry[] oldClasspathEntries = this.getIJavaProject().getResolvedClasspath(true);
		IPath nextRootAsPath = VariablesPlugin.getDefault().getSystemRoot();
		ArrayList classpathEntries = new ArrayList(frameworkList.size());
		IPath frameworkPath;
		String jarName;
		String frameworkName;
		int frameworkExtIndex;
		for (int i = 0; i < frameworkList.size(); i++) {
			frameworkName = (String) frameworkList.get(i);
			// check for framework extentsion
			frameworkExtIndex = frameworkName.indexOf(IWOLipsModel.EXT_FRAMEWORK);
			if (frameworkExtIndex == -1 || frameworkExtIndex == 0) { // invalid
				// framework
				// name
				continue;
			}
			jarName = frameworkName.substring(0, frameworkExtIndex - 1).toLowerCase() + ".jar";
			// check for root
			frameworkPath = VariablesPlugin.getDefault().getSystemRoot().append("Library").append("Frameworks");
			frameworkPath = frameworkPath.append(frameworkName);
			if (!frameworkPath.toFile().isDirectory()) {
				frameworkPath = VariablesPlugin.getDefault().getLocalRoot().append("Library").append("Frameworks");
				frameworkPath = frameworkPath.append(frameworkName);
			}
			if (!frameworkPath.toFile().isDirectory()) { // invalid path
				continue;
			} // check for jar existance
			int j = 0;
			frameworkPath = frameworkPath.append("Resources/Java/");
			String[] frameJarDirContent = frameworkPath.toFile().list();
			for (j = 0; j < frameJarDirContent.length; j++) {
				if (jarName.equals(frameJarDirContent[j].toLowerCase())) {
					// get case sensitive jar name
					jarName = frameJarDirContent[j];
					break;
				}
			}
			if (j == frameJarDirContent.length) { // jar doesn't exists
				continue;
			} // add case-sensitive jar name
			frameworkPath = frameworkPath.append(jarName);
			// check for existing classpath entries
			for (j = 0; j < oldClasspathEntries.length; j++) {
				if (oldClasspathEntries[j].getPath().equals(frameworkPath)) {
					break;
				}
			}
			if (j != oldClasspathEntries.length) { // entry already set
				continue;
			} // determine if new class path begins with next root
			if ((frameworkPath.segmentCount() > nextRootAsPath.segmentCount()) && frameworkPath.removeLastSegments(frameworkPath.segmentCount() - nextRootAsPath.segmentCount()).equals(nextRootAsPath)) {
				// replace beginning of class path with next root
				frameworkPath = VariablesPlugin.getDefault().getSystemRoot().append(frameworkPath.removeFirstSegments(nextRootAsPath.segmentCount()));
				// set path as variable entry
				classpathEntries.add(JavaCore.newVariableEntry(frameworkPath, null, null));
			} else {
				classpathEntries.add(JavaCore.newLibraryEntry(frameworkPath, null, null));
			}
		} // build new class path entry array
		oldClasspathEntries = this.getIJavaProject().getRawClasspath();
		IClasspathEntry[] newClasspathEntries = new IClasspathEntry[classpathEntries.size() + oldClasspathEntries.length];
		for (int i = 0; i < oldClasspathEntries.length; i++) {
			newClasspathEntries[i] = oldClasspathEntries[i];
		}
		for (int i = 0; i < classpathEntries.size(); i++) {
			newClasspathEntries[i + oldClasspathEntries.length] = (IClasspathEntry) classpathEntries.get(i);
		}
		return newClasspathEntries;
	}

	private IResource getJar(String prefix, String postfix) {
		IResource result = null;
		String projectName = this.getIProject().getName();
		result = this.getIProject().getFile(prefix + projectName + postfix + "Resources/Java/" + projectName + ".jar");
		if (result == null || !result.exists()) {
			result = this.getIProject().getFile(prefix + projectName + postfix + "Resources/Java/" + projectName.toLowerCase() + ".jar");
		}
		return result;
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	public IPath getWOJavaArchive() throws CoreException {
		IResource resource = null;
		IPath path = null;
		String projectName = this.getIProject().getName();
		// String projectNameLC = projectName.toLowerCase();
		// I'd rather use the knowledge from the IncrementalNature, but
		// that fragment is not
		// visible here (so I can't use the class, I think) [hn3000]
		if (this.isFramework()) {
			if (this.isAnt()) {
				resource = getJar("dist/", ".framework/");
				if (!resource.exists())
					resource = getJar("", ".framework/");
			} else if (this.isIncremental()) {
				resource = this.getIProject().getFolder("build/" + projectName + ".framework/Resources/Java");
			}
			if (resource != null && (resource.exists())) {
				path = resource.getLocation();
			} else {
				path = VariablesPlugin.getDefault().getExternalBuildRoot().append(projectName + ".framework/Resources/Java/" + projectName + ".jar");
			}
		} else if (this.isApplication()) { // must be application
			IFolder wdFolder = null;
			if (this.isAnt()) {
				wdFolder = this.getIProject().getFolder("dist");
			} else {
				wdFolder = this.getIProject().getFolder("build");
			}
			if (wdFolder != null || !wdFolder.exists()) {
				IResource[] members = wdFolder.members();
				for (int i = 0; i < members.length; i++) {
					IResource member = members[i];
					if (member.getType() == IResource.FOLDER && member.getName().endsWith(".woa")) {
						wdFolder = (IFolder) member;
						break;
					}
				}
			}
			if (wdFolder != null && wdFolder.exists()) {
				IFolder javaFolder = wdFolder.getFolder("Contents/Resources/Java");
				if (this.isAnt()) {
					resource = javaFolder.findMember(wdFolder.getName().substring(0, wdFolder.getName().length() - 4).toLowerCase() + ".jar");
					if (!resource.exists())
						resource = getJar("", ".woa/Contents/");
				} else if (this.isIncremental()) {
					resource = javaFolder;
				}
			}
			if (resource != null && (resource.exists())) {
				path = resource.getLocation();
			} else {
				path = VariablesPlugin.getDefault().getExternalBuildRoot().append(projectName + ".woa/Contents/Resources/Java/" + projectName + ".jar");
			}
		}
		return path;
	}

	public List getFrameworkNames() {
		ArrayList list = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (isAFramework(projects[i])) {
				list.add(projects[i].getName() + "." + IWOLipsModel.EXT_FRAMEWORK);
			}
		}
		try {
			list.addAll(this.toFrameworkNames(this.getIJavaProject().getResolvedClasspath(false)));
		} catch (JavaModelException e) {
			DataSetsPlugin.getDefault().getPluginLogger().log(e);
		}
		return list;
	}

	private List toFrameworkNames(IClasspathEntry[] classpathEntries) {
		ArrayList arrayList = new ArrayList();
		for (int i = 0; i < classpathEntries.length; i++) {
			IPath path = classpathEntries[i].getPath();
			String name = this.getFrameworkName(path);
			if (name != null && !name.startsWith("JavaVM")) {
				arrayList.add(name);
			}
		}
		return arrayList;
	}

	private String getFrameworkName(IPath frameworkPath) {
		String frameworkName = null;
		int i = 0;
		int count = frameworkPath.segmentCount();
		while (i < count && frameworkName == null) {
			String segment = frameworkPath.segment(i);
			if (segment.endsWith("." + IWOLipsModel.EXT_FRAMEWORK))
				frameworkName = segment;
			else
				i++;
		}
		return frameworkName;
	}

	/**
	 * Method isTheLaunchAppOrFramework.
	 * 
	 * @param iProject
	 * @return boolean
	 */
	public boolean isAFramework(IProject iProject) {
		IJavaProject buildProject = null;
		try {
			buildProject = this.getIJavaProject();
			Project project = (Project) iProject.getAdapter(Project.class);
			if (project.isFramework() && projectISReferencedByProject(iProject, buildProject.getProject()))
				return true;
		} catch (Exception anException) {
			DataSetsPlugin.getDefault().getPluginLogger().log(anException);
			return false;
		}
		return false;
	}

	/**
	 * Method projectISReferencedByProject.
	 * 
	 * @param child
	 * @param mother
	 * @return boolean
	 */
	public boolean projectISReferencedByProject(IProject child, IProject mother) {
		IProject[] projects = null;
		try {
			projects = mother.getReferencedProjects();
		} catch (Exception anException) {
			DataSetsPlugin.getDefault().getPluginLogger().log(anException);
			return false;
		}
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].equals(child))
				return true;
		}
		return false;
	}

}