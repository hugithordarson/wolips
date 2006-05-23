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
package org.objectstyle.wolips.htmlpreview.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.html.core.internal.provisional.HTML40Namespace;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.objectstyle.wolips.components.editor.EditorInteraction;
import org.objectstyle.wolips.components.editor.IEmbeddedEditor;
import org.objectstyle.wolips.components.editor.IEmbeddedEditorSelected;
import org.objectstyle.wolips.htmlpreview.HtmlPreviewPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * based on an eclipse.org example
 * 
 * @author uli
 */
public class HtmlPreviewEditor implements IEmbeddedEditor,
		IEmbeddedEditorSelected, IEditorPart {

	private EditorInteraction editorInteraction;

	private IEditorSite site;

	private IEditorInput input;

	private Browser browser;

	public HtmlPreviewEditor() {
		super();
	}

	/**
	 * Update the contents of the Preview page
	 */
	private void updatePreviewContent() {
		IDocument editDocument = editorInteraction.getHtmlDocumentProvider()
				.getHtmlEditDocument();
		IDocument htmlSource = new org.eclipse.jface.text.Document(editDocument
				.get());

		IStructuredModel editModel = null;
		int insertOffset = 0;
		List removalRegions = new ArrayList(2);
		try {
			editModel = StructuredModelManager.getModelManager()
					.getExistingModelForRead(editDocument);
			if (editModel != null && editModel instanceof IDOMModel) {
				Document document = ((IDOMModel) editModel).getDocument();
				// remove meta tags specifying encoding as required by Browser
				// API
				NodeList metaElements = document
						.getElementsByTagName(HTML40Namespace.ElementName.META);
				for (int i = 0; i < metaElements.getLength(); i++) {
					IDOMElement meta = (IDOMElement) metaElements.item(i);
					if (insertOffset == 0)
						insertOffset = meta.getStartOffset();
					insertOffset = Math.max(0, Math.min(insertOffset, meta
							.getStartOffset()));
					String attributeNameHttpEquiv = meta
							.getAttribute(HTML40Namespace.ATTR_NAME_HTTP_EQUIV);
					String attributeNameContent = meta
							.getAttribute(HTML40Namespace.ATTR_NAME_CONTENT);
					if (attributeNameHttpEquiv != null
							&& attributeNameHttpEquiv.equals("Content-Type") && attributeNameContent != null && attributeNameContent.indexOf("charset") > 0) { //$NON-NLS-2$ //$NON-NLS-1$
						if (meta.getStartStructuredDocumentRegion() != null)
							removalRegions.add(meta
									.getStartStructuredDocumentRegion());
						if (meta.getEndStructuredDocumentRegion() != null)
							removalRegions.add(meta
									.getEndStructuredDocumentRegion());
					}
				}
				// remove existing base elements with hrefs so we can add one
				// for the local location
				NodeList baseElements = document
						.getElementsByTagName(HTML40Namespace.ElementName.BASE);
				for (int i = 0; i < baseElements.getLength(); i++) {
					IDOMElement base = (IDOMElement) baseElements.item(i);
					if (insertOffset == 0)
						insertOffset = base.getStartOffset();
					insertOffset = Math.max(0, Math.min(insertOffset, base
							.getStartOffset()));
					if (base.getStartStructuredDocumentRegion() != null)
						removalRegions.add(base
								.getStartStructuredDocumentRegion());
					if (base.getEndStructuredDocumentRegion() != null)
						removalRegions.add(base
								.getEndStructuredDocumentRegion());
				}
			}

			for (int i = removalRegions.size() - 1; i >= 0; i--) {
				IStructuredDocumentRegion region = (IStructuredDocumentRegion) removalRegions
						.get(i);
				try {
					htmlSource.replace(region.getStartOffset(), region
							.getEndOffset()
							- region.getStartOffset(), ""); //$NON-NLS-1$
				} catch (BadLocationException e1) {
					HtmlPreviewPlugin.getDefault().log(e1);
				}
			}

			if (insertOffset == 0) {
				Document document = ((IDOMModel) editModel).getDocument();
				NodeList headElements = document
						.getElementsByTagName(HTML40Namespace.ElementName.HEAD);
				if (headElements.getLength() > 0) {
					IDOMElement head = (IDOMElement) headElements.item(0);
					if (head.getStartStructuredDocumentRegion() != null) {
						insertOffset = head.getStartStructuredDocumentRegion()
								.getEndOffset();
					} else {
						insertOffset = head.getEndOffset();
					}
				}

			}
		} catch (Exception e) {
			HtmlPreviewPlugin.getDefault().log(e);
		} finally {
			if (editModel != null)
				editModel.releaseFromRead();
		}
		//
		// String location = null;
		// if (getEditorInput().getAdapter(IFile.class) != null) {
		// location = "file:" + ((IFile)
		// getEditorInput().getAdapter(IFile.class)).getLocation();
		// //$NON-NLS-1$
		// }
		// else if (getEditorInput() instanceof ILocationProvider) {
		// location = "file:" + ((ILocationProvider)
		// getEditorInput()).getPath(getEditorInput()); //$NON-NLS-1$
		// }
		// else {
		// location = "file:" +
		// ResourcesPlugin.getWorkspace().getRoot().getLocation(); //$NON-NLS-1$
		// }
		//
		// try {
		// htmlSource.replace(insertOffset, 0, "<base href=\"" + location + "\"
		// />"); //$NON-NLS-2$ //$NON-NLS-1$
		// }
		// catch (BadLocationException e1) {
		// Logger.logException(e1);
		// }

		boolean rendered = browser.setText(htmlSource.get());
		if (!rendered) {
			HtmlPreviewPlugin.getDefault().log("Can't create preview");
		}
	}

	public void initEditorInteraction(EditorInteraction editorInteraction) {
		this.editorInteraction = editorInteraction;
	}

	public IEditorInput getEditorInput() {
		return input;
	}

	public IEditorSite getEditorSite() {
		return site;
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.site = site;
		this.input = input;
	}

	public void addPropertyListener(IPropertyListener listener) {
		// do nothing
	}

	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.READ_ONLY);
	}

	public IWorkbenchPartSite getSite() {
		return site;
	}

	public String getTitle() {
		return null;
	}

	public Image getTitleImage() {
		return null;
	}

	public String getTitleToolTip() {
		return null;
	}

	public void removePropertyListener(IPropertyListener listener) {
		// do nothing
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	public void doSaveAs() {
		// do nothing
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	public void dispose() {
		browser.dispose();
	}

	public void setFocus() {
		browser.setFocus();
	}

	public void editorSelected() {
		updatePreviewContent();
	}

	public EditorInteraction getEditorInteraction() {
		return editorInteraction;
	}
}