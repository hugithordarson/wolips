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
package org.objectstyle.wolips.eomodeler.editors.attributes;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.objectstyle.wolips.eomodeler.Messages;
import org.objectstyle.wolips.eomodeler.model.EOAttribute;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.utils.KeyComboBoxCellEditor;
import org.objectstyle.wolips.eomodeler.utils.TablePropertyCellModifier;
import org.objectstyle.wolips.eomodeler.utils.TableUtils;

public class EOAttributesCellModifier extends TablePropertyCellModifier {
  private static final String NO_PROTOYPE_VALUE = Messages.getString("EOAttributesCellModifier.noPrototype");
  private CellEditor[] myCellEditors;
  private List myPrototypeNames;

  public EOAttributesCellModifier(TableViewer _attributesTableViewer, CellEditor[] _cellEditors) {
    super(_attributesTableViewer);
    myCellEditors = _cellEditors;
  }

  protected boolean _canModify(Object _element, String _property) {
    boolean canModify = true;
    //    EOAttribute attribute = (EOAttribute) _element;
    //    if (attribute.isInherited()) {
    //      canModify = false;
    //    }
    if (_property == EOAttribute.PROTOTYPE) {
      EOEntity entity = (EOEntity) getTableViewer().getInput();
      myPrototypeNames = new LinkedList(entity.getModel().getModelGroup().getPrototypeAttributeNames());
      myPrototypeNames.add(0, EOAttributesCellModifier.NO_PROTOYPE_VALUE);
      String[] prototypeNames = (String[]) myPrototypeNames.toArray(new String[myPrototypeNames.size()]);
      KeyComboBoxCellEditor cellEditor = (KeyComboBoxCellEditor) myCellEditors[TableUtils.getColumnNumber(EOAttributesConstants.COLUMNS, _property)];
      cellEditor.setItems(prototypeNames);
    }
    return canModify;
  }

  public Object getValue(Object _element, String _property) {
    EOAttribute attribute = (EOAttribute) _element;
    Object value = null;
    if (_property == EOAttribute.PROTOTYPE) {
      EOAttribute prototype = attribute.getPrototype();
      String prototypeName;
      if (prototype == null) {
        prototypeName = EOAttributesCellModifier.NO_PROTOYPE_VALUE;
      }
      else {
        prototypeName = prototype.getName();
      }
      value = new Integer(myPrototypeNames.indexOf(prototypeName));
    }
    else if (_property == EOAttribute.ALLOWS_NULL || _property == EOAttribute.CLASS_PROPERTY || _property == EOAttribute.CLIENT_CLASS_PROPERTY || _property == EOAttribute.INDEXED || _property == EOAttribute.PRIMARY_KEY || _property == EOAttribute.READ_ONLY || _property== EOAttribute.USED_FOR_LOCKING) {
      value = super.getValue(_element, _property);
      if (value == null) {
        value = Boolean.FALSE;
      }
    }
    else {
      value = super.getValue(_element, _property);
    }
    return value;
  }

  protected boolean _modify(Object _element, String _property, Object _value) throws Throwable {
    boolean modified = false;
    EOAttribute attribute = (EOAttribute) _element;
    if (_property == EOAttribute.PROTOTYPE) {
      Integer prototypeIndex = (Integer) _value;
      int prototypeIndexInt = prototypeIndex.intValue();
      String prototypeName = (prototypeIndexInt == -1) ? null : (String) myPrototypeNames.get(prototypeIndexInt);
      if (EOAttributesCellModifier.NO_PROTOYPE_VALUE.equals(prototypeName)) {
        attribute.setPrototype(null, true);
      }
      else {
        EOAttribute prototype = attribute.getEntity().getModel().getModelGroup().getPrototypeAttributeNamed(prototypeName);
        attribute.setPrototype(prototype, true);
      }
      modified = true;
    }
    return modified;
  }
}