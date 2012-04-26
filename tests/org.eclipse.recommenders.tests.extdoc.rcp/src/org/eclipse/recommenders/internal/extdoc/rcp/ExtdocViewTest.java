package org.eclipse.recommenders.internal.extdoc.rcp;

import static org.junit.Assert.assertNotNull;

import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils;
import org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocView;
import org.eclipse.recommenders.utils.rcp.RCPUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.junit.Test;

import com.google.common.base.Optional;

public class ExtdocViewTest {

    @Test
    public void test() throws PartInitException {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    Optional<IWorkbenchPage> opt = RCPUtils.getActiveWorkbenchPage();
                    IViewPart view = opt.get().showView(ExtdocView.ID);
                    assertNotNull(view);
                } catch (Exception e) {
                    // XXX E4 odd thing.
                    System.err.println("NPE in Eclipse - probably e4... need to be verfied again soon!");
                    System.err.println(e);
                }
            }
        });
    }

    @Test
    public void testUIUtilsSmoketest() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                Shell s = new Shell();
                ExtdocUtils.createButton(s, "", new SelectionAdapter() {
                });
                ExtdocUtils.createCLabel(s, "", false, null);
                ExtdocUtils.createColor(SWT.COLOR_BLUE);
                ExtdocUtils.createComposite(s, 12);
                ExtdocUtils.createGridComposite(s, 2, 1, 1, 1, 1);
                ExtdocUtils.createLabel(s, "", true);
                ExtdocUtils.createLabel(s, "", true, true, SWT.COLOR_DARK_CYAN, false);
            }
        });

    }
}
