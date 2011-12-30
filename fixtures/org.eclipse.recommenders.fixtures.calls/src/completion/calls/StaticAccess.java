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
package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

public class StaticAccess extends Dialog {

    protected StaticAccess(final Shell parentShell) {
        super(parentShell);
    }

    public void test() {
        // @start
         PlatformUI.<^Space|getWorkbench.*>
        // @end
        // PlatformUI.getWorkbench()
    }
}
