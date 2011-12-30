/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

//call chain 1 ok --> 1 element in chain, will not be exposed
public class CompletionOnLocal {
    public void method() {
        //@start
	    final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchHelpSystem c = wb.<^Space|getHelpSystem.*>
        //@end
        //final IWorkbench wb = PlatformUI.getWorkbench();
        //final IWorkbenchHelpSystem c = wb.getHelpSystem()
    }
}
