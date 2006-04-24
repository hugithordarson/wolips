/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2005 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowlegement: "This product includes software developed by the ObjectStyle
 * Group (http://objectstyle.org/)." Alternately, this acknowlegement may
 * appear in the software itself, if and wherever such third-party
 * acknowlegements normally appear. 4. The names "ObjectStyle Group" and
 * "Cayenne" must not be used to endorse or promote products derived from this
 * software without prior written permission. For written permission, please
 * contact andrus@objectstyle.org. 5. Products derived from this software may
 * not be called "ObjectStyle" nor may "ObjectStyle" appear in their names
 * without prior written permission of the ObjectStyle Group.
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
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/> .
 *  
 */
package org.objectstyle.wolips.componenteditor.part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.IRegion;


/**
 * @author uli
 */
public class ComponentEditor extends ComponentEditorPart implements IGotoMarker, ITextEditor {

	public ComponentEditor() {
		super();
	}

	
	public IDocumentProvider getDocumentProvider() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return null;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.getDocumentProvider();
	}

	public void close(boolean save) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.close(save);
	}

	public boolean isEditable() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.isEditable();
	}

	public void doRevertToSaved() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.doRevertToSaved();
	}

	public void setAction(String actionID, IAction action) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.setAction(actionID, action);
	}

	public IAction getAction(String actionId) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return null;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.getAction(actionId);
	}

	public void setActionActivationCode(String actionId, char activationCharacter, int activationKeyCode, int activationStateMask) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.setActionActivationCode(actionId, activationCharacter, activationKeyCode, activationStateMask);
	}

	public void removeActionActivationCode(String actionId) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.removeActionActivationCode(actionId);
	}

	public boolean showsHighlightRangeOnly() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return false;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.showsHighlightRangeOnly();
	}

	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.showHighlightRangeOnly(showHighlightRangeOnly);
	}

	public void setHighlightRange(int offset, int length, boolean moveCursor) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.setHighlightRange(offset, length, moveCursor);
	}

	public IRegion getHighlightRange() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return null;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.getHighlightRange();
	}

	public void resetHighlightRange() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.resetHighlightRange();
	}

	public ISelectionProvider getSelectionProvider() {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return null;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		return textEditor.getSelectionProvider();
	}

	public void selectAndReveal(int offset, int length) {
		IEditorPart editorPart = this.getActiveEditor();
		if(editorPart == null || !(this.getActiveEditor() instanceof ITextEditor)) {
			return;
		}
		ITextEditor textEditor = (ITextEditor)editorPart;
		textEditor.selectAndReveal(offset, length);
	}

	public String getTitleToolTip() {
		StringBuffer toolTip = new StringBuffer();

		IEditorInput[] editorInputArray = componentEditorInput.getInput();
		for (int i = 0; i < editorInputArray.length; i++) {
			IFile inputFileFromEditor = ResourceUtil.getFile(editorInputArray[i]);
			if(inputFileFromEditor == null) {
				continue;
			}
			IPath pathFromInputFile = inputFileFromEditor.getFullPath();
			if(pathFromInputFile == null) {
				continue;
			}
			toolTip.append(pathFromInputFile.toString());
			toolTip.append("\n");
		}
        return toolTip.toString();
    }
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IGotoMarker.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public void gotoMarker(IMarker marker) {
		IResource resource = marker.getResource();
		if(resource == null) {
			return;
		}
		IEditorInput[] editorInputArray = componentEditorInput.getInput();
		for (int i = 0; i < editorInputArray.length; i++) {
			IFile inputFileFromEditor = ResourceUtil.getFile(editorInputArray[i]);
			if(inputFileFromEditor == null) {
				continue;
			}
			IPath pathFromInputFile = inputFileFromEditor.getLocation();
			if(pathFromInputFile == null) {
				continue;
			}
			IPath pathFromResource = resource.getLocation();
			if(pathFromResource == null) {
				continue;
			}
			if(pathFromInputFile.equals(pathFromResource)) {
				IEditorPart editorPart = null;
				if(i == 0) {
					editorPart = compilationUnitEditor;
				}
				if(i == 1) {
					editorPart = structuredTextEditorHTMLWithWebObjectTags;
				}
				if(i == 2) {
					editorPart = wodEditor;
				}
				if(editorPart == null) {
					continue;
				}
				IGotoMarker gotoMarker = (IGotoMarker)editorPart.getAdapter(IGotoMarker.class);
				if(gotoMarker == null) {
					return;
				}
				if(i == 0) {
					this.switchToJava();
				}
				if(i == 1) {
					this.switchToHtml();
				}
				if(i == 2) {
					this.switchToWod();
				}
				gotoMarker.gotoMarker(marker);
				return;
			}
			
		}
	}

}