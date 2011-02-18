/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package completion.overrides;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

public class CompletionOnUncompilableClass extends Dialog {

    protected CompletionOnUncompilableClass(final Shell parentShell) {
        super(parentShell);
    }
    
    // @Ignore-start
    <@Ignore("Not implemented yet")^Space|createDialogArea.*%>
    // @end
    // @Override
    // protected Control createDialogArea(Composite parent) {
    // // TODO Auto-generated method stub
    // return super.createDialogArea(parent);
    // }

}
