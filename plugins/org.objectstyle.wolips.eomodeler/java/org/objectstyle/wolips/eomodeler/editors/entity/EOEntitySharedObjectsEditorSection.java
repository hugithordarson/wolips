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
package org.objectstyle.wolips.eomodeler.editors.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.objectstyle.wolips.eomodeler.Messages;
import org.objectstyle.wolips.eomodeler.model.DuplicateFetchSpecNameException;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EOFetchSpecification;
import org.objectstyle.wolips.eomodeler.utils.BindingFactory;
import org.objectstyle.wolips.eomodeler.utils.TablePropertyCellModifier;
import org.objectstyle.wolips.eomodeler.utils.TablePropertyViewerSorter;
import org.objectstyle.wolips.eomodeler.utils.TableUtils;

public class EOEntitySharedObjectsEditorSection extends AbstractPropertySection {
  private EOEntity myEntity;

  private Button myShareNoObjectsButton;
  private Button myShareAllObjectsButton;
  private Button myShareFetchSpecsButton;
  private TableViewer myFetchSpecsViewer;

  private DataBindingContext myBindingContext;
  private PropertyChangeListener myFetchSpecListener;

  public EOEntitySharedObjectsEditorSection() {
    myFetchSpecListener = new FetchSpecChangeListener();
  }

  public void createControls(Composite _parent, TabbedPropertySheetPage _tabbedPropertySheetPage) {
    super.createControls(_parent, _tabbedPropertySheetPage);
    Composite form = getWidgetFactory().createFlatFormComposite(_parent);
    FormLayout formLayout = new FormLayout();
    form.setLayout(formLayout);

    Composite topForm = getWidgetFactory().createPlainComposite(form, SWT.NONE);
    FormData topFormData = new FormData();
    topFormData.top = new FormAttachment(0, 5);
    topFormData.left = new FormAttachment(0, 5);
    topFormData.right = new FormAttachment(100, -5);
    topForm.setLayoutData(topFormData);

    GridLayout topFormLayout = new GridLayout();
    topFormLayout.numColumns = 1;
    topForm.setLayout(topFormLayout);

    myShareNoObjectsButton = new Button(topForm, SWT.RADIO);
    myShareNoObjectsButton.setText(Messages.getString("EOEntity.shareNoObjects")); //$NON-NLS-1$
    myShareNoObjectsButton.addSelectionListener(new ShareNoObjectsListener());

    myShareAllObjectsButton = new Button(topForm, SWT.RADIO);
    myShareAllObjectsButton.setText(Messages.getString("EOEntity.shareAllObjects")); //$NON-NLS-1$
    myShareAllObjectsButton.addSelectionListener(new ShareAllObjectsListener());

    myShareFetchSpecsButton = new Button(topForm, SWT.RADIO);
    myShareFetchSpecsButton.setText(Messages.getString("EOEntity.shareFetchSpecs")); //$NON-NLS-1$
    myShareFetchSpecsButton.addSelectionListener(new ShareFetchSpecsListener());

    myFetchSpecsViewer = new TableViewer(topForm, SWT.BORDER | SWT.FLAT | SWT.MULTI | SWT.FULL_SELECTION);
    myFetchSpecsViewer.getTable().setHeaderVisible(true);
    myFetchSpecsViewer.getTable().setLinesVisible(true);
    TableUtils.createTableColumns(myFetchSpecsViewer, "EOFetchSpecification", EOFetchSpecsConstants.COLUMNS); //$NON-NLS-1$
    myFetchSpecsViewer.getTable().getColumns()[TableUtils.getColumnNumber(EOFetchSpecsConstants.COLUMNS, EOFetchSpecification.SHARES_OBJECTS)].setText(""); //$NON-NLS-1$
    myFetchSpecsViewer.setContentProvider(new EOFetchSpecsContentProvider());
    myFetchSpecsViewer.setLabelProvider(new EOFetchSpecsLabelProvider(EOFetchSpecsConstants.COLUMNS));
    myFetchSpecsViewer.setSorter(new TablePropertyViewerSorter(myFetchSpecsViewer, EOFetchSpecsConstants.COLUMNS));
    myFetchSpecsViewer.setColumnProperties(EOFetchSpecsConstants.COLUMNS);

    CellEditor[] cellEditors = new CellEditor[1];
    cellEditors[TableUtils.getColumnNumber(EOFetchSpecsConstants.COLUMNS, EOFetchSpecification.SHARES_OBJECTS)] = new CheckboxCellEditor(myFetchSpecsViewer.getTable());
    myFetchSpecsViewer.setCellModifier(new TablePropertyCellModifier(myFetchSpecsViewer));
    myFetchSpecsViewer.setCellEditors(cellEditors);
    GridData fetchSpecsLayoutData = new GridData(GridData.FILL_BOTH);
    fetchSpecsLayoutData.heightHint = 100;
    myFetchSpecsViewer.getTable().setLayoutData(fetchSpecsLayoutData);
  }

  public void setInput(IWorkbenchPart _part, ISelection _selection) {
    super.setInput(_part, _selection);
    disposeBindings();

    Object selectedObject = ((IStructuredSelection) _selection).getFirstElement();
    myEntity = (EOEntity) selectedObject;
    myBindingContext = BindingFactory.createContext();
    if (myEntity != null) {
      myEntity.addPropertyChangeListener(EOEntity.FETCH_SPECIFICATION, myFetchSpecListener);
      myEntity.addPropertyChangeListener(EOEntity.FETCH_SPECIFICATIONS, myFetchSpecListener);
      myFetchSpecsViewer.setInput(myEntity);
    }

    fetchSpecsChanged();
  }

  protected void disposeBindings() {
    if (myBindingContext != null) {
      myBindingContext.dispose();
    }
    if (myEntity != null) {
      myEntity.removePropertyChangeListener(EOEntity.FETCH_SPECIFICATION, myFetchSpecListener);
      myEntity.removePropertyChangeListener(EOEntity.FETCH_SPECIFICATIONS, myFetchSpecListener);
    }
  }

  public void dispose() {
    super.dispose();
    disposeBindings();
  }

  public EOEntity getEntity() {
    return myEntity;
  }

  protected void fetchSpecsChanged() {
    myFetchSpecsViewer.refresh();
    shareTypeChanged();
  }

  protected void fetchSpecChanged(EOFetchSpecification _fetchSpec) {
    myFetchSpecsViewer.refresh(_fetchSpec);
    shareTypeChanged();
  }

  protected void shareTypeChanged() {
    if (!myEntity.hasSharedObjects()) {
      if (!myShareNoObjectsButton.getSelection()) {
        myShareNoObjectsButton.setSelection(true);
      }
      myShareAllObjectsButton.setSelection(false);
      myShareFetchSpecsButton.setSelection(false);
      myFetchSpecsViewer.getTable().setEnabled(false);
    }
    else if (myEntity.isSharesAllObjectsOnly()) {
      if (!myShareAllObjectsButton.getSelection()) {
        myShareAllObjectsButton.setSelection(true);
      }
      myShareNoObjectsButton.setSelection(false);
      myShareFetchSpecsButton.setSelection(false);
      myFetchSpecsViewer.getTable().setEnabled(false);
    }
    else {
      if (!myShareFetchSpecsButton.getSelection()) {
        myShareFetchSpecsButton.setSelection(true);
      }
      myShareNoObjectsButton.setSelection(false);
      myShareAllObjectsButton.setSelection(false);
      myFetchSpecsViewer.getTable().setEnabled(true);
    }
    TableUtils.packTableColumns(myFetchSpecsViewer);
  }

  protected class FetchSpecChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent _event) {
      String propertyName = _event.getPropertyName();
      if (EOEntity.FETCH_SPECIFICATION == propertyName) {
        EOFetchSpecification fetchSpec = (EOFetchSpecification) _event.getNewValue();
        EOEntitySharedObjectsEditorSection.this.fetchSpecChanged(fetchSpec);

      }
      else if (EOEntity.FETCH_SPECIFICATIONS == propertyName) {
        EOEntitySharedObjectsEditorSection.this.fetchSpecsChanged();
      }
    }
  }

  protected class ShareNoObjectsListener implements SelectionListener {
    public void widgetDefaultSelected(SelectionEvent _e) {
      widgetSelected(_e);
    }

    public void widgetSelected(SelectionEvent _e) {
      EOEntitySharedObjectsEditorSection.this.getEntity().shareNoObjects();
    }
  }

  protected class ShareAllObjectsListener implements SelectionListener {
    public void widgetDefaultSelected(SelectionEvent _e) {
      widgetSelected(_e);
    }

    public void widgetSelected(SelectionEvent _e) {
      try {
        EOEntitySharedObjectsEditorSection.this.getEntity().shareAllObjects();
      }
      catch (DuplicateFetchSpecNameException e) {
        e.printStackTrace();
      }
    }
  }

  protected class ShareFetchSpecsListener implements SelectionListener {
    public void widgetDefaultSelected(SelectionEvent _e) {
      widgetSelected(_e);
    }

    public void widgetSelected(SelectionEvent _e) {
      EOEntitySharedObjectsEditorSection.this.shareTypeChanged();
    }
  }
}
