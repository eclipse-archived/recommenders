package data;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnStaticAccessInMethodWithImport {

    public void method() {
        
        IWorkbenchHelpSystem c = PlatformUI.<^Space> 
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> c
         */
    }
    
}
