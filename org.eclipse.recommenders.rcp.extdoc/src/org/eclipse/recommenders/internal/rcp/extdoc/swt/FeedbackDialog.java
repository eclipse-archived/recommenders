/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import org.eclipse.recommenders.rcp.extdoc.AbstractDialog;
import org.eclipse.swt.widgets.Shell;

final class FeedbackDialog extends AbstractDialog {

    protected FeedbackDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void contentsCreated() {
    }

}
