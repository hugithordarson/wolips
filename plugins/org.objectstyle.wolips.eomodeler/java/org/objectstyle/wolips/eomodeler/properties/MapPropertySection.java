package org.objectstyle.wolips.eomodeler.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An advanced section that is intended to show the original table format properties view
 * provided by base Eclipse.
 * 
 * @author Anthony Hunter 
 */
public class MapPropertySection extends AbstractPropertySection {

  /**
   * The Property Sheet Page.
   */
  protected PropertySheetPage page;

  /**
   * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
   */
  public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
    super.createControls(parent, tabbedPropertySheetPage);
    Composite composite = getWidgetFactory().createFlatFormComposite(parent);
    page = new PropertySheetPage();

    page.createControl(composite);
    FormData data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    page.getControl().setLayoutData(data);
  }

  /**
   * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void setInput(IWorkbenchPart part, ISelection selection) {
    super.setInput(part, selection);
    page.selectionChanged(part, selection);
  }

  /**
   * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
   */
  public void dispose() {
    super.dispose();

    if (page != null) {
      page.dispose();
      page = null;
    }

  }

  /**
   * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
   */
  public void refresh() {
    page.refresh();
  }

  /**
   * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
   */
  public boolean shouldUseExtraSpace() {
    return true;
  }
}
