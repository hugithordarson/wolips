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

import org.w3c.dom.Element;

public class Binding extends AbstractApiModelElement {

	private BindingNameChangedListener bindingNameChangedListener;

	public final static String BINDING = "binding";

	public final static String NAME = "name";

	private final static String DEFAULTS = "defaults";

	public final static String[] ALL_DEFAULTS = new String[] { "Undefined", "Actions", "Boolean", "YES/NO", "Date Format Strings", "Number Format Strings", "MIME Types", "Direct Actions", "Direct Action Classes", "Page Names", "Frameworks", "Resources" };

	private Wo parent;

	protected Binding(Element element, ApiModel apiModel, Wo parent) {
		super(element, apiModel);
		this.parent = parent;
	}

	public String getName() {
		return element.getAttribute(NAME);
	}

	public void setName(String className) {
		element.setAttribute(NAME, className);
		apiModel.markAsDirty();
		if (bindingNameChangedListener != null) {
			bindingNameChangedListener.namedChanged(this);
		}
	}

	public String getDefaults() {
		return element.getAttribute(DEFAULTS);
	}

	public int getSelectedDefaults() {
		String defaults = this.getDefaults();
		if (defaults == null) {
			return 0;
		}
		for (int i = 0; i < ALL_DEFAULTS.length; i++) {
			String string = ALL_DEFAULTS[i];
			if (string.equals(defaults)) {
				return i;
			}
		}
		return 0;
	}

	public void setDefaults(String defaults) {
		element.setAttribute(DEFAULTS, defaults);
	}

	public void setDefaults(int defaults) {
		if (defaults == 0) {
			if (getDefaults() == null) {
				return;
			}
			element.removeAttribute(DEFAULTS);
		} else {
			if (getDefaults() != null && getDefaults().equals(ALL_DEFAULTS[defaults])) {
				return;
			}
			this.setDefaults(ALL_DEFAULTS[defaults]);
		}
		apiModel.markAsDirty();
	}

	public boolean isExplicitlyRequired() {
		return "YES".equalsIgnoreCase(element.getAttribute("required"));
	}

	public boolean isRequired() {
		boolean required = isExplicitlyRequired();
		if (!required) {
			Validation[] validations = parent.getValidations();
			for (int i = 0; !required && i < validations.length; i++) {
				Validation validation = validations[i];
				Unbound[] unbounds = validation.getUnbounds();
				if (unbounds.length == 1) {
					if (unbounds[0].isAffectedByBindingNamed(this.getName())) {
						required = true;
					}
				}
			}
		}
		return required;
	}

	public void setIsRequired(boolean isResquired) {
		if (this.isRequired() == isResquired) {
			return;
		}
		if (isResquired) {
			Unbound.addToWoWithBinding(parent, this);
		} else {
			Unbound.removeFromWoWithBinding(parent, this);
		}
		apiModel.markAsDirty();
	}

	public boolean isWillSet() {
		Validation[] validations = parent.getValidations();
		for (int i = 0; i < validations.length; i++) {
			Validation validation = validations[i];
			Unsettable[] unsettables = validation.getUnsettables();
			if (unsettables.length == 1) {
				if (unsettables[0].isAffectedByBindingNamed(this.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public void setIsWillSet(boolean isWillSet) {
		if (this.isWillSet() == isWillSet) {
			return;
		}
		if (isWillSet) {
			Unsettable.addToWoWithBinding(parent, this);
		} else {
			Unsettable.removeFromWoWithBinding(parent, this);
		}
		apiModel.markAsDirty();
	}

	public interface BindingNameChangedListener {
		public abstract void namedChanged(Binding binding);
	}

	public void setBindingNameChangedListener(BindingNameChangedListener bindingNameChangedListener) {
		this.bindingNameChangedListener = bindingNameChangedListener;
	}
}
