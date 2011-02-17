package data;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnThisAndLocal {
    public void method() {
	    final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchHelpSystem c = <^Space>
    }
}
