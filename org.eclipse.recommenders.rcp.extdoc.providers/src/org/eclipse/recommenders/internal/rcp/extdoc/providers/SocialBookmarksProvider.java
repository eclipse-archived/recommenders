package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.ListingTable;
import org.eclipse.recommenders.rcp.extdoc.AbstractTitledProvider;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;

public final class SocialBookmarksProvider extends AbstractTitledProvider {

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
    }

    @Override
    protected boolean updateSelection(final IJavaElementSelection selection, final Composite composite) {
        new ProviderUiJob() {
            @Override
            public Composite run() {
                if (!composite.isDisposed()) {
                    updateDisplay(selection, composite);
                }
                return composite;
            }
        }.schedule();
        return true;
    }

    private void updateDisplay(final IJavaElementSelection selection, final Composite composite) {
        disposeChildren(composite);
        displayItems(selection, composite);
        displayAddControl(selection, composite);
    }

    private void displayItems(final IJavaElementSelection selection, final Composite composite) {
        SwtFactory.createLabel(composite, "The following resources on " + selection.getJavaElement().getElementName()
                + " have been provided by the community. You are welcome to add you own.");

        final ListingTable table = new ListingTable(composite, 3);
        for (int i = 0; i < 3; ++i) {
            table.startNewRow();
            final Hyperlink link = new Hyperlink(table, SWT.NONE);
            link.setForeground(SwtFactory.createColor(SWT.COLOR_BLUE));
            link.setText("Cool Link");
            link.setHref("http://google.com");
            table.addLabelItem("Bla Bla Bla this is a very nice link.", false, false, SWT.COLOR_BLACK);
        }
    }

    private void displayAddControl(final IJavaElementSelection selection, final Composite composite) {
        final Composite addComposite = SwtFactory.createGridComposite(composite, 4, 6, 0, 0, 0);
        SwtFactory.createLink(addComposite, "Click here to add a new bookmark.",
                ExtDocPlugin.getIcon("eview16/add.gif"), false, new MouseListener() {
                    @Override
                    public void mouseUp(final MouseEvent e) {
                        displayAddArea(selection, composite, addComposite);
                    }

                    @Override
                    public void mouseDown(final MouseEvent e) {
                    }

                    @Override
                    public void mouseDoubleClick(final MouseEvent e) {
                    }
                });
    }

    private void displayAddArea(final IJavaElementSelection selection, final Composite composite,
            final Composite addComposite) {
        disposeChildren(addComposite);
        final Text title = SwtFactory.createText(addComposite, "Link Title", 180);
        final Text description = SwtFactory.createText(addComposite, "Link Description", 256);
        final Text url = SwtFactory.createText(addComposite, "URL", 128);
        SwtFactory.createButton(addComposite, "Add", new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                addBookmark(title.getText(), description.getText(), url.getText(), selection, composite);
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });
        layout(composite);
    }

    private void addBookmark(final String text, final String description, final String url,
            final IJavaElementSelection selection, final Composite composite) {
        // server.setText(javaElement, text);
        updateDisplay(selection, composite);
        layout(composite);
    }

    private void layout(final Composite composite) {
        composite.layout(true);
        composite.getParent().getParent().layout(true);
    }
}
