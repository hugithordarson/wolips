/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2005 - 2006 The ObjectStyle Group and individual authors of the
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
package org.objectstyle.wolips.wodclipse.wod;

import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.objectstyle.wolips.components.editor.EditorInteraction;
import org.objectstyle.wolips.components.editor.IEmbeddedEditor;
import org.objectstyle.wolips.components.editor.IWebobjectTagListener;
import org.objectstyle.wolips.components.editor.IWodDocumentProvider;
import org.objectstyle.wolips.htmleditor.HtmleditorPlugin;
import org.objectstyle.wolips.locate.LocateException;
import org.objectstyle.wolips.locate.LocatePlugin;
import org.objectstyle.wolips.locate.result.LocalizedComponentsLocateResult;
import org.objectstyle.wolips.wodclipse.WodclipsePlugin;
import org.objectstyle.wolips.wodclipse.wod.parser.ElementNameRule;
import org.objectstyle.wolips.wodclipse.wod.parser.RulePosition;
import org.objectstyle.wolips.wodclipse.wod.parser.WodScanner;

/**
 * @author mike
 * @author uli
 */
public class WodEditor extends TextEditor implements IEmbeddedEditor, IWebobjectTagListener, IWodDocumentProvider {
	private WodContentOutlinePage myContentOutlinePage;

	private IEditorInput myInput;

	private LocalizedComponentsLocateResult myComponentsLocateResults;

	private EditorInteraction editorInteraction;

	public WodEditor() {
		setSourceViewerConfiguration(new WodSourceViewerConfiguration(this));
	}

	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.objectstyle.wolips.wodclipse.wodEditorScope" });
	}

	protected ISourceViewer createSourceViewer(Composite _parent, IVerticalRuler _verticalRuler, int _styles) {
		return super.createSourceViewer(_parent, _verticalRuler, _styles);
	}

	protected void createActions() {
		super.createActions();

		String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.eclipse.jdt.internal.ui.javaeditor.ConstructedJavaEditorMessages";//$NON-NLS-1$
		ResourceBundle fgBundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
		// WodclipsePlugin.getDefault().getResourceBundle()
		ContentAssistAction action = new ContentAssistAction(fgBundleForConstructedKeys, "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);
		markAsStateDependentAction("ContentAssistProposal", true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.CONTENT_ASSIST_ACTION);
	}

	public void updateWebObjectsTagNames() {
		try {
			IDocument document = getDocumentProvider().getDocument(getEditorInput());
			Set elementNamesSet = WodScanner.getTextForRulesOfType(document, ElementNameRule.class);
			String[] elementNames = (String[]) elementNamesSet.toArray(new String[elementNamesSet.size()]);
			HtmleditorPlugin.getDefault().setWebObjectsTagNames(elementNames);
		} catch (BadLocationException t) {
			// null means no tags
			// the user has to enter the name manually
			HtmleditorPlugin.getDefault().setWebObjectsTagNames(null);
		}
	}

	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (myContentOutlinePage == null) {
				myContentOutlinePage = new WodContentOutlinePage(getDocumentProvider(), this);
				myContentOutlinePage.setInput(myInput);
			}
			return myContentOutlinePage;
		}
		return super.getAdapter(adapter);
	}

	public void init(IEditorSite _site, IEditorInput _input) throws PartInitException {
		super.init(_site, _input);
		myInput = _input;
		IDocument document = getDocumentProvider().getDocument(getEditorInput());
		document.addDocumentListener(new IDocumentListener() {

			public void documentAboutToBeChanged(DocumentEvent event) {
				// Do nothing
			}

			public void documentChanged(DocumentEvent event) {
				IContentOutlinePage contentOutlinePage = (IContentOutlinePage) WodEditor.this.getAdapter(IContentOutlinePage.class);
				if (contentOutlinePage instanceof WodContentOutlinePage) {
					WodContentOutlinePage wodContentOutlinePage = (WodContentOutlinePage) contentOutlinePage;
					wodContentOutlinePage.update();
				}
			}
		});
	}

	public LocalizedComponentsLocateResult getComponentsLocateResults() throws CoreException, LocateException {
		if (myComponentsLocateResults == null) {
			myComponentsLocateResults = LocatePlugin.getDefault().getLocalizedComponentsLocateResult(((IFileEditorInput) myInput).getFile());
		}
		return myComponentsLocateResults;
	}

	public void initEditorInteraction(EditorInteraction intiEditorInteraction) {
		this.editorInteraction = intiEditorInteraction;
		editorInteraction.setWebObjectTagListener(this);
		editorInteraction.setWodDocumentProvider(this);
	}

	public void webObjectTagSelected(String name) {
		try {
			IDocument document = getDocumentProvider().getDocument(getEditorInput());
			WodScanner wodScanner = WodScanner.wodScannerForDocument(document);
			RulePosition elementNameRulePosition = wodScanner.firstRulePositionOfTypeWithText(ElementNameRule.class, name);
			if (elementNameRulePosition != null) {
				IRegion region = document.getLineInformationOfOffset(elementNameRulePosition.getTokenOffset());
				setHighlightRange(region.getOffset(), region.getLength(), true);
			}
		} catch (BadLocationException e) {
			WodclipsePlugin.getDefault().log(e);
		}
	}

	public IDocument getWodEditDocument() {
		IDocument document = getDocumentProvider().getDocument(getEditorInput());
		return document;
	}

	public EditorInteraction getEditorInteraction() {
		return editorInteraction;
	}

}
