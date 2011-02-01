package tests.callchain;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class T04 extends Dialog {

    protected T04(final IShellProvider parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        //final IWorkbenchHelpSystem help = PlatformUI.<^Space>
        //
        final IWorkbenchHelpSystem help = PlatformUI.
        return null;
    }
}
