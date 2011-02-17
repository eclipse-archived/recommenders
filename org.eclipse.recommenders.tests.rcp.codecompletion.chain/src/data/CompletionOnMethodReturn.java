package data;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnMethodReturn {
	public void method() {
        
        IWorkbenchHelpSystem c = getPlatform().<^Space>
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> c
         */
    }

	private IWorkbench getPlatform() {
		return PlatformUI.getWorkbench();
	}
}
