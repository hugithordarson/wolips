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
package org.objectstyle.wolips.components.input;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.MultiEditorInput;
import org.objectstyle.wolips.components.ComponentsPlugin;
import org.objectstyle.wolips.editors.EditorsPlugin;
import org.objectstyle.wolips.locate.LocateException;
import org.objectstyle.wolips.locate.LocatePlugin;
import org.objectstyle.wolips.locate.result.LocalizedComponentsLocateResult;

public class ComponentEditorInput extends MultiEditorInput implements IPersistableElement {

	private boolean displayHtmlPartOnReveal = true;

	private boolean displayWodPartOnReveal = false;

	private boolean displayApiPartOnReveal = false;

	private boolean displayWooPartOnReveal = false;

	private LocalizedComponentsLocateResult localizedComponentsLocateResult;

	public ComponentEditorInput(String[] editorIDs, IEditorInput[] innerEditors) {
		super(editorIDs, innerEditors);
	}

	private static ComponentEditorInput create(LocalizedComponentsLocateResult localizedComponentsLocateResult) throws CoreException {
		String ids[] = null;
		ComponentEditorFileEditorInput allInput[] = null;
		ids = new String[3];
		allInput = new ComponentEditorFileEditorInput[3];
		ids[0] = EditorsPlugin.HTMLEditorID;
		IFolder folder = localizedComponentsLocateResult.getComponents()[0];
		IFile htmlFile = LocalizedComponentsLocateResult.getHtml(folder);
		IFile wodFile = LocalizedComponentsLocateResult.getWod(folder);
		allInput[0] = new ComponentEditorFileEditorInput(htmlFile);
		ids[1] = EditorsPlugin.WodEditorID;
		allInput[1] = new ComponentEditorFileEditorInput(wodFile);
		if (localizedComponentsLocateResult.getDotApi() != null) {
			ids[2] = EditorsPlugin.ApiEditorID;
			allInput[2] = new ComponentEditorFileEditorInput(localizedComponentsLocateResult.getDotApi());
		} else {
			ids[2] = EditorsPlugin.ApiEditorID;
			String apiFileName = LocatePlugin.getDefault().fileNameWithoutExtension(wodFile);
			IFile api = wodFile.getParent().getParent().getFile(new Path(apiFileName + ".api"));
			allInput[2] = new ComponentEditorFileEditorInput(api);
		}
		ComponentEditorInput input = new ComponentEditorInput(ids, allInput);
		input.localizedComponentsLocateResult = localizedComponentsLocateResult;
		for (int i = 0; i < allInput.length; i++) {
			allInput[i].setComponentEditorInput(input);
		}
		return input;
	}

	/*
	 * may return null
	 */
	private static ComponentEditorInput create(IFile file) throws CoreException {
		LocalizedComponentsLocateResult localizedComponentsLocateResult = null;
		try {
			localizedComponentsLocateResult = LocatePlugin.getDefault().getLocalizedComponentsLocateResult(file);
		} catch (CoreException e) {
			ComponentsPlugin.getDefault().log(e);
			return null;
		} catch (LocateException e) {
			ComponentsPlugin.getDefault().log(e);
			return null;
		}
		if (localizedComponentsLocateResult.getDotJava() == null || localizedComponentsLocateResult.getComponents() == null || localizedComponentsLocateResult.getComponents().length == 0) {
			return null;
		}
		ComponentEditorInput input = create(localizedComponentsLocateResult);
		return input;
	}

	/*
	 * may return null
	 */
	public static ComponentEditorInput createWithDotHtml(IFile file) throws CoreException {
		ComponentEditorInput input = create(file);
		if (input != null) {
			input.displayHtmlPartOnReveal = true;
		}
		return input;
	}

	/*
	 * may return null
	 */
	public static ComponentEditorInput createWithDotWod(IFile file) throws CoreException {
		ComponentEditorInput input = create(file);
		if (input != null) {
			input.displayWodPartOnReveal = true;
		}
		return input;
	}

	/*
	 * may return null
	 */
	public static ComponentEditorInput createWithDotApi(IFile file) throws CoreException {
		ComponentEditorInput input = create(file);
		input.displayApiPartOnReveal = true;
		return input;
	}

	/*
	 * may return null
	 */
	public static ComponentEditorInput createWithDotWoo(IFile file) throws CoreException {
		ComponentEditorInput input = create(file);
		if (input != null) {
			input.displayWooPartOnReveal = true;
		}
		return input;
	}

	public LocalizedComponentsLocateResult getLocalizedComponentsLocateResult() {
		return localizedComponentsLocateResult;
	}

	public String getFactoryId() {
		return ComponentEditorInputFactory.ID_FACTORY;
	}

	public void saveState(IMemento memento) {
		ComponentEditorInputFactory.saveState(memento, this);
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public boolean isDisplayApiPartOnReveal() {
		return displayApiPartOnReveal;
	}

	public void setDisplayApiPartOnReveal(boolean displayApiPartOnReveal) {
		this.displayApiPartOnReveal = displayApiPartOnReveal;
	}

	public boolean isDisplayHtmlPartOnReveal() {
		return displayHtmlPartOnReveal;
	}

	public void setDisplayHtmlPartOnReveal(boolean displayHtmlPartOnReveal) {
		this.displayHtmlPartOnReveal = displayHtmlPartOnReveal;
	}

	public boolean isDisplayWodPartOnReveal() {
		return displayWodPartOnReveal;
	}

	public void setDisplayWodPartOnReveal(boolean displayWodPartOnReveal) {
		this.displayWodPartOnReveal = displayWodPartOnReveal;
	}

	public boolean isDisplayWooPartOnReveal() {
		return displayWooPartOnReveal;
	}

	public void setDisplayWooPartOnReveal(boolean displayWooPartOnReveal) {
		this.displayWooPartOnReveal = displayWooPartOnReveal;
	}
}
