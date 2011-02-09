package data;

import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnStaticAccessInMethodWithOutImport {
    public void method() {
        
        IWorkbenchHelpSystem c = PlatformUI.<^Space> 
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> c
         */
    }
}
