package data;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnIncompleteDeclaration {
	public void method() {
        
        IWorkbenchHelpSystem<^Space>
    }

	private IWorkbench getPlatform() {
		return PlatformUI.getWorkbench();
	}
}
