/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */
package org.objectstyle.wolips.eomodeler.model;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.objectstyle.wolips.datasets.adaptable.Project;
import org.objectstyle.wolips.eogenerator.model.EOGeneratorModel;
import org.objectstyle.wolips.eogenerator.model.EOModelReference;
import org.objectstyle.wolips.eomodeler.Activator;

public class EclipseEOModelGroupFactory {
	public static EOModel createModel(IResource _modelResource, Set _failures, boolean _skipOnDuplicates) throws CoreException, IOException, EOModelException, ParseException {
		IProject project = _modelResource.getProject();
		EOModel model = null;
		EOModelGroup modelGroup;
		if ("eomodelgroup".equals(_modelResource.getFileExtension())) {
			modelGroup = new EOModelGroup();
			EOGeneratorModel eogeneratorModel = EOGeneratorModel.createModelFromFile((IFile) _modelResource);
			List modelRefList = new LinkedList();
			modelRefList.addAll(eogeneratorModel.getModels());
			modelRefList.addAll(eogeneratorModel.getRefModels());
			Iterator modelIter = modelRefList.iterator();
			while (modelIter.hasNext()) {
				EOModelReference modelRef = (EOModelReference) modelIter.next();
				String modelPath = modelRef.getPath(project);
				File modelFolder = new File(modelPath);
				if (!modelFolder.isAbsolute()) {
					modelFolder = new File(project.getLocation().toFile(), modelPath);
				}
				EOModel modelGroupModel = modelGroup.addModelFromFolder(modelFolder, _failures, _skipOnDuplicates, _modelResource.getProject());
				if (model == null) {
					model = modelGroupModel;
				}
			}
			modelGroup.resolve(_failures);
			modelGroup.verify(_failures);
		} else {
			IContainer modelContainer;
			if (_modelResource.getType() == IResource.FILE) {
				modelContainer = _modelResource.getParent();
			} else {
				modelContainer = (IContainer) _modelResource;
			}
			modelGroup = EclipseEOModelGroupFactory.createModelGroup(project, _failures, _skipOnDuplicates);
			String modelFileName = modelContainer.getName();
			String modelName = modelFileName.substring(0, modelFileName.indexOf('.'));
			model = modelGroup.getModelNamed(modelName);
		}
		return model;
	}

	protected static void addModelsFromProject(EOModelGroup _modelGroup, IProject _project, Set _searchedFolders, Set _searchedProjects, Set _failures, boolean _skipOnDuplicates) throws IOException, EOModelException, CoreException {
		if (!_searchedProjects.contains(_project)) {
			_searchedProjects.add(_project);
			Project wolipsProject = (Project) _project.getAdapter(Project.class);
			if (wolipsProject != null && wolipsProject.hasWOLipsNature()) {
				IJavaProject javaProject = JavaCore.create(_project);
				IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
				for (int classpathEntryNum = 0; classpathEntryNum < classpathEntries.length; classpathEntryNum++) {
					IClasspathEntry entry = classpathEntries[classpathEntryNum];
					int entryKind = entry.getEntryKind();
					if (entryKind == IClasspathEntry.CPE_LIBRARY) {
						IPath path = entry.getPath();
						IPath frameworkPath = null;
						while (frameworkPath == null && path.lastSegment() != null) {
							String lastSegment = path.lastSegment();
							if (lastSegment != null && lastSegment.endsWith(".framework")) {
								frameworkPath = path;
							} else {
								path = path.removeLastSegments(1);
							}
						}
						if (frameworkPath != null) {
							File resourcesFolder = frameworkPath.append("Resources").toFile();
							if (!_searchedFolders.contains(resourcesFolder) && resourcesFolder.exists()) {
								_searchedFolders.add(resourcesFolder);
								_modelGroup.addModelsFromFolder(resourcesFolder, false, _failures, _skipOnDuplicates, _project);
							}
						}
					} else if (entryKind == IClasspathEntry.CPE_PROJECT) {
						IPath path = entry.getPath();
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.lastSegment());
						EclipseEOModelGroupFactory.addModelsFromProject(_modelGroup, project, _searchedFolders, _searchedProjects, _failures, _skipOnDuplicates);
					}
				}

				_project.accept(new ModelVisitor(_modelGroup, _searchedFolders, _failures, _skipOnDuplicates), IResource.DEPTH_INFINITE, IContainer.EXCLUDE_DERIVED);
			}
		}
	}

	public static EOModelGroup createModelGroup(IProject _project, Set _failures, boolean _skipOnDuplicates) throws CoreException, IOException, EOModelException {
		EOModelGroup modelGroup = new EOModelGroup();
		EclipseEOModelGroupFactory.addModelsFromProject(modelGroup, _project, new HashSet(), new HashSet(), _failures, _skipOnDuplicates);
		modelGroup.resolve(_failures);
		modelGroup.verify(_failures);
		return modelGroup;
	}

	protected static class ModelVisitor implements IResourceVisitor {
		private EOModelGroup myModelGroup;

		private Set myFailures;

		private Set mySearchedFolders;
		
		private boolean mySkipOnDuplicates;

		public ModelVisitor(EOModelGroup _modelGroup, Set _searchedFolders, Set _failures, boolean _skipOnDuplicates) {
			myModelGroup = _modelGroup;
			myFailures = _failures;
			mySearchedFolders = _searchedFolders;
			mySkipOnDuplicates = _skipOnDuplicates;
		}

		public boolean visit(IResource _resource) throws CoreException {
			try {
				boolean visitChildren = true;
				if (_resource.getType() == IResource.FOLDER) {
					File resourceFile = _resource.getLocation().toFile();
					if (!mySearchedFolders.contains(resourceFile) && "eomodeld".equals(_resource.getFileExtension())) {
						myModelGroup.addModelFromFolder(resourceFile, myFailures, mySkipOnDuplicates, _resource.getProject());
						visitChildren = false;
					}
				}
				return visitChildren;
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Failed to load model in " + _resource + ": " + e, e));
			}
		}
	}
}
