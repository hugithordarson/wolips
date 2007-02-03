/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2005 The ObjectStyle Group,
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
package org.objectstyle.wolips.core.resources.types.api;

import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Wo extends AbstractApiModelElement {

	private final static String CLASS = "class";

	private final static String WOCOMPONENTCONTENT = "wocomponentcontent";

	protected Wo(Element element, ApiModel apiModel) {
		super(element, apiModel);
	}

	public String getClassName() {
		return element.getAttribute(CLASS);
	}

	public void setClassName(String className) {
		element.setAttribute(CLASS, className);
	}

	public boolean getIsWocomponentcontent() {
		String value = element.getAttribute(WOCOMPONENTCONTENT);
		if (value == null) {
			return false;
		}
		return value.equals("true");
	}

	public void setIsWocomponentcontent(boolean isWocomponentcontent) {
		if (isWocomponentcontent) {
			element.setAttribute(WOCOMPONENTCONTENT, "true");
		} else {
			element.setAttribute(WOCOMPONENTCONTENT, "false");
		}
	}

	public Binding[] getBindings() {
		NodeList bindingElements = element.getElementsByTagName(Binding.BINDING);
		ArrayList bindings = new ArrayList();
		for (int i = 0; i < bindingElements.getLength(); i++) {
			Element bindingElement = (Element) bindingElements.item(i);
			Binding binding = new Binding(bindingElement, apiModel, this);
			bindings.add(binding);
		}
		return (Binding[]) bindings.toArray(new Binding[bindings.size()]);
	}

	public Validation[] getValidations() {
		NodeList validationElements = element.getElementsByTagName(Validation.VALIDATION);
		ArrayList validations = new ArrayList();
		for (int i = 0; i < validationElements.getLength(); i++) {
			Element validationElement = (Element) validationElements.item(i);
			Validation validation = new Validation(validationElement, apiModel);
			validations.add(validation);
		}
		return (Validation[]) validations.toArray(new Validation[validations.size()]);
	}

	public Validation[] getAffectedValidations(String bindingName) {
		Validation[] validations = this.getValidations();
		ArrayList validationsList = new ArrayList();
		for (int i = 0; i < validations.length; i++) {
			Validation validation = validations[i];
			if (validation.isAffectedByBindingNamed(bindingName)) {
				validationsList.add(validation);
			}

		}
		return (Validation[]) validationsList.toArray(new Validation[validationsList.size()]);
	}

	public Validation[] getFailedValidations(Map _bindings) {
		Validation[] validations = this.getValidations();
		ArrayList validationsList = new ArrayList();
		for (int i = 0; i < validations.length; i++) {
			Validation validation = validations[i];
			if (validation.evaluate(_bindings)) {
				validationsList.add(validation);
			}
		}
		return (Validation[]) validationsList.toArray(new Validation[validationsList.size()]);
	}

	public Binding getBinding(String name) {
		Binding matchingBinding = null;
		Binding[] bindings = getBindings();
		for (int bindingNum = 0; matchingBinding == null && bindingNum < bindings.length; bindingNum++) {
			Binding binding = bindings[bindingNum];
			if (name != null && name.equals(binding.getName())) {
				matchingBinding = binding;
			}
		}
		return matchingBinding;
	}
	
	public boolean containsBinding(String name) {
		return getBinding(name) == null;
	}
	
	public Binding createBinding(String name) {
		Binding binding = getBinding(name);
		if (binding == null) {
			Element newBindingElement = this.element.getOwnerDocument().createElement(Binding.BINDING);
			newBindingElement.setAttribute(Binding.NAME, name);
			this.element.appendChild(newBindingElement);
			this.apiModel.markAsDirty();
			binding = getBinding(name);
		}
		return binding;
	}

	public void removeBinding(String name) {
		Binding binding = getBinding(name);
		if (binding != null) {
			removeBinding(binding);
		}
	}
	
	public void removeBinding(Binding binding) {
		Validation[] validations = this.getAffectedValidations(binding.getName());
		for (int i = 0; i < validations.length; i++) {
			this.element.removeChild(validations[i].element);
		}
		this.element.removeChild(binding.element);
		this.apiModel.markAsDirty();
	}
}
