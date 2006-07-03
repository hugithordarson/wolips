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
package org.objectstyle.wolips.eomodeler.editors.entities;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.objectstyle.wolips.eomodeler.editors.KeyComboBoxCellEditor;
import org.objectstyle.wolips.eomodeler.editors.TableUtils;
import org.objectstyle.wolips.eomodeler.model.DuplicateEntityNameException;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EOModel;

public class EOEntitiesCellModifier implements ICellModifier {
  private static final String NO_PARENT_VALUE = "No Parent";
  private TableViewer myModelTableViewer;
  private CellEditor[] myCellEditors;
  private List myEntityNames;

  public EOEntitiesCellModifier(TableViewer _modelTableViewer, CellEditor[] _cellEditors) {
    myModelTableViewer = _modelTableViewer;
    myCellEditors = _cellEditors;
  }

  public boolean canModify(Object _element, String _property) {
    if (_property == EOEntitiesConstants.PARENT) {
      EOModel model = (EOModel) myModelTableViewer.getInput();
      myEntityNames = model.getModelGroup().getEntityNames();
      myEntityNames.add(0, EOEntitiesCellModifier.NO_PARENT_VALUE);
      String[] entityNames = (String[]) myEntityNames.toArray(new String[myEntityNames.size()]);
      KeyComboBoxCellEditor cellEditor = (KeyComboBoxCellEditor) myCellEditors[TableUtils.getColumnNumber(EOEntitiesConstants.COLUMNS, _property)];
      cellEditor.setItems(entityNames);
    }
    return true;
  }

  public Object getValue(Object _element, String _property) {
    EOEntity entity = (EOEntity) _element;
    Object value = null;
    if (_property == EOEntitiesConstants.NAME) {
      value = entity.getName();
    }
    else if (_property == EOEntitiesConstants.TABLE) {
      value = entity.getExternalName();
    }
    else if (_property == EOEntitiesConstants.CLASS_NAME) {
      value = entity.getClassName();
    }
    else if (_property == EOEntitiesConstants.PARENT) {
      String parentName = entity.getParentName();
      if (parentName == null) {
        parentName = EOEntitiesCellModifier.NO_PARENT_VALUE;
      }
      value = Integer.valueOf(myEntityNames.indexOf(parentName));
    }
    else {
      throw new IllegalArgumentException("Unknown property '" + _property + "'");
    }
    return value;
  }

  public void modify(Object _element, String _property, Object _value) {
    try {
      TableItem tableItem = (TableItem) _element;
      EOEntity entity = (EOEntity) tableItem.getData();
      if (_property == EOEntitiesConstants.NAME) {
        entity.setName((String) _value);
      }
      else if (_property == EOEntitiesConstants.TABLE) {
        entity.setExternalName((String) _value);
      }
      else if (_property == EOEntitiesConstants.CLASS_NAME) {
        entity.setClassName((String) _value);
      }
      else if (_property == EOEntitiesConstants.PARENT) {
        Integer entityIndex = (Integer) _value;
        int entityIndexInt = entityIndex.intValue();
        String entityName = (entityIndexInt == -1) ? null : (String) myEntityNames.get(entityIndexInt);
        if (EOEntitiesCellModifier.NO_PARENT_VALUE.equals(entityName)) {
          entity.setParentName(null);
        }
        else {
          entity.setParentName(entityName);
        }
      }
      else {
        throw new IllegalArgumentException("Unknown property '" + _property + "'");
      }
      myModelTableViewer.refresh(entity);
    }
    catch (DuplicateEntityNameException e) {
      MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage());
    }
  }
}