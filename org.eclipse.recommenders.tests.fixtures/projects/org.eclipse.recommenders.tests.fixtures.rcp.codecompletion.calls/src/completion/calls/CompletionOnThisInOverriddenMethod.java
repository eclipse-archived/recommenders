/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package completion.calls;

import org.eclipse.jface.wizard.Wizard;

public class CompletionOnThisInOverriddenMethod extends Wizard {

    @Override
    public void addPages() {
        //@start 
        <^Space|addPage.*IWizardPage.*%>
        //@end
        //addPage()
    }

    @Override
    public boolean performFinish() {
        //@start
        this.<^Space|getContainer.*%>
        //@end
        //this.getContainer()
        return false;
    }
}
