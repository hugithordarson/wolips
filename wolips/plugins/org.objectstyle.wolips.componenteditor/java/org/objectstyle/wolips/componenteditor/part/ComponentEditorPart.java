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
package org.objectstyle.wolips.componenteditor.part;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.objectstyle.wolips.baseforuiplugins.IEditorTarget;
import org.objectstyle.wolips.componenteditor.outline.ComponentEditorOutline;
import org.objectstyle.wolips.components.editor.EditorInteraction;
import org.objectstyle.wolips.components.input.ComponentEditorInput;

/**
 * @author uli
 */
public class ComponentEditorPart extends MultiPageEditorPart implements IEditorTarget, IResourceChangeListener {

	ComponentEditorInput componentEditorInput;

	private EditorInteraction editorInteraction = new EditorInteraction();

	private ComponentEditorOutline componentEditorOutline;

	protected HtmlWodTab[] htmlWodTabs;

	private HtmlPreviewTab htmlPreviewTab;

	private ApiTab apiTab;

	private ComponentEditorTab[] componentEditorTabs;

	public ComponentEditorPart() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private ComponentEditorOutline getComponentEditorOutline() {
		if (componentEditorOutline == null) {
			componentEditorOutline = new ComponentEditorOutline();
		}
		return componentEditorOutline;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			return this.getComponentEditorOutline();
		}
		return super.getAdapter(adapter);
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		componentEditorInput = (ComponentEditorInput) input;
		if (input != null) {
			String inputName = input.getName();
			String partName = inputName.substring(0, inputName.indexOf("."));
			setPartName(partName);
		}
		site.setSelectionProvider(new ComponentEditorPartSelectionProvider(this));
	}

	public Object getJavaFile() {
		return ((IFileEditorInput) componentEditorInput.getInput()[0]).getFile();
	}

	public IEditorInput getEditorInput() {
		return componentEditorTabs[this.getActivePage()].getActiveEditorInput();
	}

	protected void createPages() {
		if (componentEditorInput == null) {
			return;
		}
		IEditorInput[] editorInput = componentEditorInput.getComponentEditors();
		componentEditorTabs = new ComponentEditorTab[editorInput.length / 2 + 2];
		htmlWodTabs = new HtmlWodTab[editorInput.length / 2];
		// htmlwod tabs
		IFileEditorInput htmlInput = null;
		IFileEditorInput wodInput = null;
		int j = 0;
		int tabIndex = 0;
		for (int i = 0; i < editorInput.length / 2; i++) {
			htmlInput = (IFileEditorInput) editorInput[j];
			j++;
			wodInput = (IFileEditorInput) editorInput[j];
			j++;
			HtmlWodTab htmlWodTab = new HtmlWodTab(this, tabIndex, htmlInput, wodInput);
			componentEditorTabs[tabIndex] = htmlWodTab;
			htmlWodTabs[tabIndex] = htmlWodTab;
			htmlWodTab.createTab();
			this.addPage(htmlWodTab);
			this.setPageText(tabIndex, "Component");
			tabIndex++;
		}

		// html preview tab
		htmlPreviewTab = new HtmlPreviewTab(this, tabIndex, htmlInput);
		componentEditorTabs[tabIndex] = htmlPreviewTab;
		htmlPreviewTab.createTab();
		this.addPage(htmlPreviewTab);
		this.setPageText(tabIndex, "Preview");
		tabIndex++;
		// api tab
		IFileEditorInput apiInput = (IFileEditorInput) componentEditorInput.getApiEditor();
		apiTab = new ApiTab(this, tabIndex, apiInput);
		componentEditorTabs[tabIndex] = apiTab;
		apiTab.createTab();
		this.addPage(apiTab);
		this.setPageText(tabIndex, "Api");

		CTabFolder tabFolder = (CTabFolder) this.getContainer();
		tabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ComponentEditorPart.this.updateOutline();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}

		});
		if (componentEditorInput.isDisplayHtmlPartOnReveal()) {
			this.switchToHtml();
		} else if (componentEditorInput.isDisplayWodPartOnReveal()) {
			this.switchToWod();
		} else if (componentEditorInput.isDisplayWooPartOnReveal()) {
			this.switchToWod();
		} else if (componentEditorInput.isDisplayApiPartOnReveal()) {
			this.switchToApi();
		}
		return;
	}

	public void doSave(IProgressMonitor monitor) {
		for (int i = 0; i < componentEditorTabs.length; i++) {
			if (componentEditorTabs[i] != null && componentEditorTabs[i].isDirty()) {
				componentEditorTabs[i].doSave(monitor);
			}
		}
		return;
	}

	public void close(boolean save) {
		for (int i = 0; i < componentEditorTabs.length; i++) {
			if (componentEditorTabs[i] != null) {
				componentEditorTabs[i].close(save);
			}
		}
	}

	public void doSaveAs() {
		assert false;
		return;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void updateOutline() {
		IEditorPart editorPart = this.getActiveEditor();
		if (editorPart != null) {
			IContentOutlinePage contentOutlinePage = (IContentOutlinePage) editorPart.getAdapter(IContentOutlinePage.class);
			this.getComponentEditorOutline().setPageActive(contentOutlinePage);
		}
	}

	public boolean isDirty() {
		if (super.isDirty()) {
			return true;
		}
		for (int i = 0; i < componentEditorTabs.length; i++) {
			if (componentEditorTabs[i].isDirty()) {
				return true;
			}
		}
		return false;
	}

	public IEditorPart switchTo(int targetEditorID) {
		switch (targetEditorID) {
		case IEditorTarget.TARGET_API:
			this.switchToApi();
			break;
		case IEditorTarget.TARGET_HTML:
			this.switchToHtml();
			break;
		case IEditorTarget.TARGET_PREVIEW:
			this.switchToPreview();
			break;
		case IEditorTarget.TARGET_WOD:
			this.switchToWod();
			break;

		default:
			break;
		}
		IEditorPart editorPart = getActiveEditor();
		return editorPart;
	}

	public void switchToHtml() {
		this.htmlWodTabs[0].setHtmlActive();
		switchToPage(0);
	}

	public void switchToWod() {
		this.htmlWodTabs[0].setWodActive();
		switchToPage(0);
	}

	public void switchToPreview() {
		switchToPage(htmlWodTabs.length);
	}

	public void switchToApi() {
		switchToPage(htmlWodTabs.length + 1);
	}

	public void switchToPage(int page) {
		this.setActivePage(page);
		setFocus();
	}

	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		componentEditorTabs[newPageIndex].editorSelected();
	}

	public ComponentEditorInput getComponentEditorInput() {
		return componentEditorInput;
	}

	protected IEditorPart getEditor(int pageIndex) {
		return componentEditorTabs[this.getActivePage()].getActiveEmbeddedEditor();
	}

	protected IEditorPart getActiveEditor() {
		return componentEditorTabs[this.getActivePage()].getActiveEmbeddedEditor();
	}

	private static class ComponentEditorPartSelectionProvider extends MultiPageSelectionProvider {
		private ISelection globalSelection;

		public ComponentEditorPartSelectionProvider(ComponentEditorPart componentEditorPart) {
			super(componentEditorPart);
		}

		public ISelection getSelection() {
			IEditorPart activeEditor = ((ComponentEditorPart) getMultiPageEditor()).getActiveEditor();
			if (activeEditor != null) {
				ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
				if (selectionProvider != null) {
					return selectionProvider.getSelection();
				}
			}
			return globalSelection;
		}

		public void setSelection(ISelection selection) {
			IEditorPart activeEditor = ((ComponentEditorPart) getMultiPageEditor()).getActiveEditor();
			if (activeEditor != null) {
				ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
				if (selectionProvider != null) {
					selectionProvider.setSelection(selection);
				}
			} else {
				this.globalSelection = selection;
				fireSelectionChanged(new SelectionChangedEvent(this, globalSelection));
			}
		}
	}

	public void publicHandlePropertyChange(int propertyId) {
		this.handlePropertyChange(propertyId);
	}

	public IEditorSite publicCreateSite(IEditorPart editor) {
		return this.createSite(editor);
	}

	public Composite publicGetContainer() {
		return super.getContainer();
	}

	public EditorInteraction getEditorInteraction() {
		return editorInteraction;
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					boolean closed = false;
					for (int i = 0; i < componentEditorInput.getInput().length; i++) {
						if (((FileEditorInput) componentEditorInput.getInput()[i]).getFile().getProject().equals(event.getResource())) {
							IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
							for (int j = 0; j < pages.length; j++) {
								IEditorPart editorPart = pages[i].findEditor(componentEditorInput);
								if (editorPart != null) {
									if (pages[i].closeEditor(ComponentEditorPart.this, true)) {
										closed = true;
									}
								}
							}
						}
						if (closed) {
							break;
						}
					}
				}
			});
		}
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		for (int i = 0; i < componentEditorTabs.length; i++) {
			if (componentEditorTabs[i] != null) {
				componentEditorTabs[i].dispose();
			}
		}
		super.dispose();
	}

}
