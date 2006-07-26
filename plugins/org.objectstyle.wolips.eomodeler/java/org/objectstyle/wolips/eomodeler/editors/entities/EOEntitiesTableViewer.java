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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.objectstyle.wolips.eomodeler.Messages;
import org.objectstyle.wolips.eomodeler.editors.IEOModelEditor;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EOModel;
import org.objectstyle.wolips.eomodeler.utils.EmptyTableRowDoubleClickHandler;
import org.objectstyle.wolips.eomodeler.utils.KeyComboBoxCellEditor;
import org.objectstyle.wolips.eomodeler.utils.TableRefreshPropertyListener;
import org.objectstyle.wolips.eomodeler.utils.TableRowRefreshPropertyListener;
import org.objectstyle.wolips.eomodeler.utils.TableUtils;

public class EOEntitiesTableViewer extends Composite implements ISelectionProvider, IEOModelEditor {
  private TableViewer myEntitiesTableViewer;
  private EOModel myModel;
  private TableRefreshPropertyListener myTableRefresher;
  private TableRowRefreshPropertyListener myTableRowRefresher;

  public EOEntitiesTableViewer(Composite _parent, int _style) {
    super(_parent, _style);
    setLayout(new GridLayout(1, true));
    myEntitiesTableViewer = TableUtils.createTableViewer(this, "EOEntity", EOEntitiesConstants.COLUMNS, new EOEntitiesContentProvider(), new EOEntitiesLabelProvider(EOEntitiesConstants.COLUMNS), new EOEntitiesViewerSorter(EOEntitiesConstants.COLUMNS));
    new DoubleClickNewEntityHandler().attachTo(myEntitiesTableViewer);
    Table entitiesTable = myEntitiesTableViewer.getTable();
    entitiesTable.setLayoutData(new GridData(GridData.FILL_BOTH));
    TableUtils.sort(myEntitiesTableViewer, EOEntity.NAME);

    CellEditor[] cellEditors = new CellEditor[EOEntitiesConstants.COLUMNS.length];
    cellEditors[TableUtils.getColumnNumber(EOEntitiesConstants.COLUMNS, EOEntity.NAME)] = new TextCellEditor(entitiesTable);
    cellEditors[TableUtils.getColumnNumber(EOEntitiesConstants.COLUMNS, EOEntity.EXTERNAL_NAME)] = new TextCellEditor(entitiesTable);
    cellEditors[TableUtils.getColumnNumber(EOEntitiesConstants.COLUMNS, EOEntity.CLASS_NAME)] = new TextCellEditor(entitiesTable);
    cellEditors[TableUtils.getColumnNumber(EOEntitiesConstants.COLUMNS, EOEntity.PARENT)] = new KeyComboBoxCellEditor(entitiesTable, new String[0], SWT.READ_ONLY);
    myEntitiesTableViewer.setCellModifier(new EOEntitiesCellModifier(myEntitiesTableViewer, cellEditors));
    myEntitiesTableViewer.setCellEditors(cellEditors);

    myTableRefresher = new TableRefreshPropertyListener(myEntitiesTableViewer);
    myTableRowRefresher = new TableRowRefreshPropertyListener(myEntitiesTableViewer);
  }

  public void setModel(EOModel _model) {
    if (myModel != null) {
      myModel.removePropertyChangeListener(EOModel.ENTITIES, myTableRefresher);
      myModel.removePropertyChangeListener(EOModel.ENTITY, myTableRowRefresher);
    }
    myModel = _model;
    myEntitiesTableViewer.setInput(myModel);
    TableUtils.packTableColumns(myEntitiesTableViewer);
    if (myModel != null) {
      myModel.addPropertyChangeListener(EOModel.ENTITIES, myTableRefresher);
      myModel.addPropertyChangeListener(EOModel.ENTITY, myTableRowRefresher);
    }

  }

  public EOModel getModel() {
    return myModel;
  }

  public void setSelectedEntity(EOEntity _entity) {
    IStructuredSelection selection = (IStructuredSelection) myEntitiesTableViewer.getSelection();
    if ((_entity == null && !selection.isEmpty()) || (selection != null && !selection.toList().contains(_entity))) {
      if (_entity == null) {
        myEntitiesTableViewer.setSelection(new StructuredSelection(), true);
      }
      else {
        myEntitiesTableViewer.setSelection(new StructuredSelection(_entity), true);
      }
    }
  }

  public TableViewer getTableViewer() {
    return myEntitiesTableViewer;
  }

  public void setSelection(ISelection _selection) {
    myEntitiesTableViewer.setSelection(_selection);
  }

  public ISelection getSelection() {
    return myEntitiesTableViewer.getSelection();
  }

  public void addSelectionChangedListener(ISelectionChangedListener _listener) {
    myEntitiesTableViewer.addSelectionChangedListener(_listener);
  }

  public void removeSelectionChangedListener(ISelectionChangedListener _listener) {
    myEntitiesTableViewer.removeSelectionChangedListener(_listener);
  }

  protected class DoubleClickNewEntityHandler extends EmptyTableRowDoubleClickHandler {
    protected void doubleSelectionOccurred() {
      try {
        EOEntitiesTableViewer.this.getModel().addBlankEntity(Messages.getString("EOEntity.newName"));
      }
      catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }
}
