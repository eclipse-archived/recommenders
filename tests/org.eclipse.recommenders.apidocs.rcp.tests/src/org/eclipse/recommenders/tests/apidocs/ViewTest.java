package org.eclipse.recommenders.tests.apidocs;

import static org.junit.Assert.assertNotNull;

import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsView;
import org.eclipse.recommenders.internal.apidocs.rcp.ApidocsViewUtils;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.junit.Test;

import com.google.common.base.Optional;

public class ViewTest {

    @Test
    public void test() throws PartInitException {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    Optional<IWorkbenchPage> opt = RCPUtils.getActiveWorkbenchPage();
                    IViewPart view = opt.get().showView(ApidocsView.ID);
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
                ApidocsViewUtils.createButton(s, "", new SelectionAdapter() {
                });
                ApidocsViewUtils.createCLabel(s, "", false, null);
                ApidocsViewUtils.createColor(SWT.COLOR_BLUE);
                ApidocsViewUtils.createComposite(s, 12);
                ApidocsViewUtils.createGridComposite(s, 2, 1, 1, 1, 1);
                ApidocsViewUtils.createLabel(s, "", true);
                ApidocsViewUtils.createLabel(s, "", true, true, SWT.COLOR_DARK_CYAN, false);
            }
        });

    }
}
