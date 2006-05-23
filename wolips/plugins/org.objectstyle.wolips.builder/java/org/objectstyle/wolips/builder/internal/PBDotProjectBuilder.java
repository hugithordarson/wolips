/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2005 - 2006 The ObjectStyle Group,
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
 * ========================================================
 * ============
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.wolips.builder.internal;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IProgressMonitor;
import org.objectstyle.wolips.core.resources.builder.ICleanBuilder;
import org.objectstyle.wolips.core.resources.builder.IDeltaBuilder;
import org.objectstyle.wolips.core.resources.types.ILocalizedPath;
import org.objectstyle.wolips.core.resources.types.IPBDotProjectOwner;
import org.objectstyle.wolips.core.resources.types.file.IPBDotProjectAdapter;
import org.objectstyle.wolips.core.resources.types.folder.IDotEOModeldAdapter;
import org.objectstyle.wolips.core.resources.types.folder.IDotSubprojAdapter;
import org.objectstyle.wolips.core.resources.types.folder.IDotWoAdapter;
import org.objectstyle.wolips.core.resources.types.project.IProjectAdapter;

public class PBDotProjectBuilder implements IDeltaBuilder, ICleanBuilder {
	private Hashtable affectedPBDotProjectOwner;

	public PBDotProjectBuilder() {
		super();
	}

	private String key(IResource resource) {
		return resource.getLocation().toPortableString();
	}

	private IPBDotProjectAdapter getIPBDotProjectAdapterForKey(
			IResource resource) {
		String key = this.key(resource);
		if (affectedPBDotProjectOwner.containsKey(key)) {
			return (IPBDotProjectAdapter) affectedPBDotProjectOwner.get(key);
		}
		return null;
	}

	private void setIPBDotProjectOwnerForKey(
			IPBDotProjectAdapter pbDotProjectAdapter, IResource resource) {
		affectedPBDotProjectOwner.put(this.key(resource), pbDotProjectAdapter);
	}

	public boolean buildStarted(int kind, Map args, IProgressMonitor monitor,
			IProject project, Map _buildCache) {
		this.affectedPBDotProjectOwner = new Hashtable();
		IProjectAdapter projectAdapter = (IProjectAdapter) project
				.getAdapter(IProjectAdapter.class);
		IPBDotProjectAdapter adapter = projectAdapter.getPBDotProjectAdapter();
		boolean fullBuildRequested = adapter.isRebuildRequired();
		return fullBuildRequested;
	}

	public boolean buildPreparationDone(int _kind, Map _args,
			IProgressMonitor _monitor, IProject _project, Map _buildCache) {
		Iterator iterator = affectedPBDotProjectOwner.values().iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			IPBDotProjectAdapter pbDotProjectAdapter = (IPBDotProjectAdapter) object;
			pbDotProjectAdapter.save();
		}
		this.affectedPBDotProjectOwner = null;
		return false;
	}

	private IPBDotProjectOwner getIPBDotProjectOwner(IResource resource) {
		IProject project = resource.getProject();
		IProjectAdapter projectAdapter = (IProjectAdapter) project
				.getAdapter(IProjectAdapter.class);
		IPBDotProjectOwner pbDotProjectOwner = projectAdapter
				.getPBDotProjectOwner(resource);
		return pbDotProjectOwner;
	}

	public IPBDotProjectAdapter getIPBDotProjectAdapter(
			IPBDotProjectOwner pbDotProjectOwner) {
		IPBDotProjectAdapter pbDotProjectAdapter = this
				.getIPBDotProjectAdapterForKey(pbDotProjectOwner
						.getUnderlyingResource());
		if (pbDotProjectAdapter == null) {
			pbDotProjectAdapter = pbDotProjectOwner.getPBDotProjectAdapter();
			this.setIPBDotProjectOwnerForKey(pbDotProjectAdapter,
					pbDotProjectOwner.getUnderlyingResource());
		}
		return pbDotProjectAdapter;
	}

	public boolean handleSourceDelta(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		IResource resource = delta.getResource();
		handleSource(delta.getKind(), resource, monitor, _buildCache);
		return false;
	}

	public void handleSource(IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		handleSource(IResourceDelta.ADDED, _resource, _progressMonitor,
				_buildCache);
	}

	public boolean handleSource(int _kind, IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		String extension = _resource.getFileExtension();
		if (_kind == IResourceDelta.ADDED || _kind == IResourceDelta.CHANGED
				|| _kind == IResourceDelta.REMOVED) {
			IPBDotProjectOwner pbDotProjectOwner = this
					.getIPBDotProjectOwner(_resource);
			IPBDotProjectAdapter pbDotProjectAdapter = this
					.getIPBDotProjectAdapter(pbDotProjectOwner);
			ILocalizedPath localizedPath = pbDotProjectAdapter
					.localizedRelativeResourcePath(pbDotProjectOwner, _resource);
			if (_kind == IResourceDelta.ADDED
					|| _kind == IResourceDelta.CHANGED) {
				pbDotProjectAdapter.addClass(localizedPath);
			} else if (_kind == IResourceDelta.REMOVED) {
				pbDotProjectAdapter.removeClass(localizedPath);
			}
		}
		return false;
	}

	public boolean handleClassesDelta(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		IResource resource = delta.getResource();
		handleClasses(delta.getKind(), resource, monitor, _buildCache);
		return false;
	}

	public void handleClasses(IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		handleClasses(IResourceDelta.ADDED, _resource, _progressMonitor,
				_buildCache);
	}

	public boolean handleClasses(int _kind, IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		return false;
	}

	public boolean handleWoappResourcesDelta(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		handleWoappResources(delta.getKind(), delta.getResource(), monitor,
				_buildCache);
		return false;
	}

	public void handleWoappResources(IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		handleWoappResources(IResourceDelta.ADDED, _resource, _progressMonitor,
				_buildCache);
	}

	public void handleWoappResources(int _kind, IResource resource,
			IProgressMonitor monitor, Map _buildCache) {
		if (_kind == IResourceDelta.ADDED || _kind == IResourceDelta.CHANGED
				|| _kind == IResourceDelta.REMOVED) {
			IPBDotProjectOwner pbDotProjectOwner = this
					.getIPBDotProjectOwner(resource);
			IPBDotProjectAdapter pbDotProjectAdapter = this
					.getIPBDotProjectAdapter(pbDotProjectOwner);
			ILocalizedPath localizedPath = pbDotProjectAdapter
					.localizedRelativeResourcePath(pbDotProjectOwner, resource);
			IDotWoAdapter dotWoAdapter = (IDotWoAdapter) resource
					.getAdapter(IDotWoAdapter.class);
			boolean isDotWO = dotWoAdapter != null;
			IDotWoAdapter parentWoAdapter = null;
			if (resource.getParent() != null) {
				parentWoAdapter = (IDotWoAdapter) resource.getParent()
						.getAdapter(IDotWoAdapter.class);
			}
			boolean parentIsDotWO = parentWoAdapter != null;
			if (parentIsDotWO) {
				return;
			}
			IDotEOModeldAdapter parentDotEOModeldAdapter = null;
			if (resource.getParent() != null) {
				parentDotEOModeldAdapter = (IDotEOModeldAdapter) resource
						.getParent().getAdapter(IDotEOModeldAdapter.class);
			}
			boolean parentIsDotEOModeld = parentDotEOModeldAdapter != null;
			if (parentIsDotEOModeld) {
				return;
			}
			if (_kind == IResourceDelta.ADDED
					|| _kind == IResourceDelta.CHANGED) {
				if (isDotWO) {
					pbDotProjectAdapter.addWoComponent(localizedPath);
				} else {
					pbDotProjectAdapter.addWoappResource(localizedPath);
				}
			} else if (_kind == IResourceDelta.REMOVED) {
				if (isDotWO) {
					pbDotProjectAdapter.removeWoComponent(localizedPath);
				} else {
					pbDotProjectAdapter.removeWoappResource(localizedPath);
				}
			}
		}
	}

	public void handleWebServerResources(IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		handleWebServerResources(IResourceDelta.ADDED, _resource,
				_progressMonitor, _buildCache);
	}

	public boolean handleWebServerResourcesDelta(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		IResource resource = delta.getResource();
		handleWebServerResources(delta.getKind(), resource, monitor,
				_buildCache);
		return false;
	}

	public void handleWebServerResources(int _kind, IResource resource,
			IProgressMonitor monitor, Map _buildCache) {
		if (_kind == IResourceDelta.ADDED || _kind == IResourceDelta.CHANGED
				|| _kind == IResourceDelta.REMOVED) {
			IPBDotProjectOwner pbDotProjectOwner = this
					.getIPBDotProjectOwner(resource);
			IPBDotProjectAdapter pbDotProjectAdapter = this
					.getIPBDotProjectAdapter(pbDotProjectOwner);
			ILocalizedPath localizedPath = pbDotProjectAdapter
					.localizedRelativeResourcePath(pbDotProjectOwner, resource);
			if (_kind == IResourceDelta.ADDED
					|| _kind == IResourceDelta.CHANGED) {
				pbDotProjectAdapter.addWebServerResource(localizedPath);
			} else if (_kind == IResourceDelta.REMOVED) {
				pbDotProjectAdapter.removeWebServerResource(localizedPath);
			}
		}
	}

	public void handleOther(IResource resource, IProgressMonitor monitor,
			Map _buildCache) {
		handleOther(IResourceDelta.ADDED, resource, monitor, _buildCache);
	}

	public boolean handleOtherDelta(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		IResource resource = delta.getResource();
		handleOther(delta.getKind(), resource, monitor, _buildCache);
		return false;
	}

	public void handleOther(int _kind, IResource resource,
			IProgressMonitor monitor, Map _buildCache) {
		IDotSubprojAdapter dotSubprojAdapter = (IDotSubprojAdapter) resource
				.getAdapter(IDotSubprojAdapter.class);
		if (dotSubprojAdapter == null) {
			return;
		}
		IPBDotProjectOwner pbDotProjectOwner = this
				.getIPBDotProjectOwner(resource.getParent());
		IPBDotProjectAdapter pbDotProjectAdapter = this
				.getIPBDotProjectAdapter(pbDotProjectOwner);
		if (_kind == IResourceDelta.ADDED) {
			pbDotProjectAdapter.addSubproject(dotSubprojAdapter);
		}
		if (_kind == IResourceDelta.REMOVED) {
			pbDotProjectAdapter.removeSubproject(dotSubprojAdapter);
		}
	}

	public void handleClasspath(IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		handleClasspath(IResourceDelta.ADDED, _resource, _progressMonitor,
				_buildCache);
	}

	public boolean classpathChanged(IResourceDelta delta,
			IProgressMonitor monitor, Map _buildCache) {
		IResource resource = delta.getResource();
		handleClasspath(delta.getKind(), resource, monitor, _buildCache);
		return false;
	}

	public void handleClasspath(int _kind, IResource _resource,
			IProgressMonitor _progressMonitor, Map _buildCache) {
		IPBDotProjectOwner pbDotProjectOwner = this
				.getIPBDotProjectOwner(_resource);
		IPBDotProjectAdapter pbDotProjectAdapter = this
				.getIPBDotProjectAdapter(pbDotProjectOwner);
		IProject project = _resource.getProject();
		IProjectAdapter projectAdapter = (IProjectAdapter) project
				.getAdapter(IProjectAdapter.class);
		List frameworkNames = projectAdapter.getFrameworkNames();
		pbDotProjectAdapter.updateFrameworkNames(frameworkNames);
	}

}