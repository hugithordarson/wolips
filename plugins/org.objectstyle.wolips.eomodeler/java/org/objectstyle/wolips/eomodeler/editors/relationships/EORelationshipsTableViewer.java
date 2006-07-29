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
package org.objectstyle.wolips.eomodeler.editors.relationships;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.objectstyle.wolips.eomodeler.Activator;
import org.objectstyle.wolips.eomodeler.Messages;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EORelationship;
import org.objectstyle.wolips.eomodeler.utils.EmptyTableRowDoubleClickHandler;
import org.objectstyle.wolips.eomodeler.utils.TableRefreshPropertyListener;
import org.objectstyle.wolips.eomodeler.utils.TableRowRefreshPropertyListener;
import org.objectstyle.wolips.eomodeler.utils.TableUtils;

public class EORelationshipsTableViewer extends Composite implements ISelectionProvider {
  private TableViewer myRelationshipsTableViewer;
  private EOEntity myEntity;
  private RelationshipsChangeRefresher myRelationshipsChangedRefresher;
  private TableRefreshPropertyListener myParentChangedRefresher;
  private TableRowRefreshPropertyListener myTableRowRefresher;

  public EORelationshipsTableViewer(Composite _parent, int _style) {
    super(_parent, _style);
    setLayout(new GridLayout(1, true));
    myRelationshipsTableViewer = TableUtils.createTableViewer(this, "EORelationship", EORelationshipsConstants.COLUMNS, new EORelationshipsContentProvider(), null, new EORelationshipsViewerSorter(EORelationshipsConstants.COLUMNS));
    myRelationshipsTableViewer.setLabelProvider(new EORelationshipsLabelProvider(myRelationshipsTableViewer, EORelationshipsConstants.COLUMNS));
    new DoubleClickNewRelationshipHandler().attachTo(myRelationshipsTableViewer);
    myRelationshipsChangedRefresher = new RelationshipsChangeRefresher(myRelationshipsTableViewer);
    myParentChangedRefresher = new TableRefreshPropertyListener(myRelationshipsTableViewer);
    myTableRowRefresher = new TableRowRefreshPropertyListener(myRelationshipsTableViewer);

    Table relationshipsTable = myRelationshipsTableViewer.getTable();
    relationshipsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

    TableColumn toManyColumn = relationshipsTable.getColumn(TableUtils.getColumnNumber(EORelationshipsConstants.COLUMNS, EORelationship.TO_MANY));
    toManyColumn.setText("");

    TableColumn classPropertyColumn = relationshipsTable.getColumn(TableUtils.getColumnNumber(EORelationshipsConstants.COLUMNS, EORelationship.CLASS_PROPERTY));
    classPropertyColumn.setText("");
    classPropertyColumn.setImage(Activator.getDefault().getImageRegistry().get(Activator.CLASS_PROPERTY_ICON));

    TableUtils.sort(myRelationshipsTableViewer, EORelationship.NAME);

    CellEditor[] cellEditors = new CellEditor[EORelationshipsConstants.COLUMNS.length];
    cellEditors[TableUtils.getColumnNumber(EORelationshipsConstants.COLUMNS, EORelationship.TO_MANY)] = new CheckboxCellEditor();
    cellEditors[TableUtils.getColumnNumber(EORelationshipsConstants.COLUMNS, EORelationship.CLASS_PROPERTY)] = new CheckboxCellEditor();
    cellEditors[TableUtils.getColumnNumber(EORelationshipsConstants.COLUMNS, EORelationship.NAME)] = new TextCellEditor(relationshipsTable);
    myRelationshipsTableViewer.setCellModifier(new EORelationshipsCellModifier(myRelationshipsTableViewer));
    myRelationshipsTableViewer.setCellEditors(cellEditors);
  }

  public void setEntity(EOEntity _entity) {
    if (myEntity != null) {
      myEntity.removePropertyChangeListener(EOEntity.PARENT, myParentChangedRefresher);
      myEntity.removePropertyChangeListener(EOEntity.RELATIONSHIPS, myRelationshipsChangedRefresher);
      myEntity.removePropertyChangeListener(EOEntity.RELATIONSHIP, myTableRowRefresher);
    }
    myEntity = _entity;
    if (myEntity != null) {
      myRelationshipsTableViewer.setInput(myEntity);
      TableUtils.packTableColumns(myRelationshipsTableViewer);
      myEntity.addPropertyChangeListener(EOEntity.PARENT, myParentChangedRefresher);
      myEntity.addPropertyChangeListener(EOEntity.RELATIONSHIPS, myRelationshipsChangedRefresher);
      myEntity.addPropertyChangeListener(EOEntity.RELATIONSHIP, myTableRowRefresher);
    }
  }

  public EOEntity getEntity() {
    return myEntity;
  }

  public TableViewer getTableViewer() {
    return myRelationshipsTableViewer;
  }

  public void addSelectionChangedListener(ISelectionChangedListener _listener) {
    myRelationshipsTableViewer.addSelectionChangedListener(_listener);
  }

  public void removeSelectionChangedListener(ISelectionChangedListener _listener) {
    myRelationshipsTableViewer.removeSelectionChangedListener(_listener);
  }

  public ISelection getSelection() {
    return myRelationshipsTableViewer.getSelection();
  }

  public void setSelection(ISelection _selection) {
    myRelationshipsTableViewer.setSelection(_selection);
  }

  protected class DoubleClickNewRelationshipHandler extends EmptyTableRowDoubleClickHandler {
    protected void doubleSelectionOccurred() {
      try {
        EORelationshipsTableViewer.this.getEntity().addBlankRelationship(Messages.getString("EORelationship.newName"));
      }
      catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  protected class RelationshipsChangeRefresher extends TableRefreshPropertyListener {
    public RelationshipsChangeRefresher(TableViewer _tableViewer) {
      super(_tableViewer);
    }

    public void propertyChange(PropertyChangeEvent _event) {
      super.propertyChange(_event);
      Set oldValues = (Set) _event.getOldValue();
      Set newValues = (Set) _event.getNewValue();
      if (newValues != null && oldValues != null) {
        if (newValues.size() > oldValues.size()) {
          List newList = new LinkedList(newValues);
          newList.removeAll(oldValues);
          EORelationshipsTableViewer.this.setSelection(new StructuredSelection(newList));
        }
        TableUtils.packTableColumns(EORelationshipsTableViewer.this.getTableViewer());
      }
    }
  }
}
