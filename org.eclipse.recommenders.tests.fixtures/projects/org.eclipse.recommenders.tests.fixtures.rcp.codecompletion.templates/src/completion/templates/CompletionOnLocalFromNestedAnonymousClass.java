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
package completion.templates;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CompletionOnLocalFromNestedAnonymousClass extends DialogPage {

	Text text;

	@Override
    public void createControl(final Composite parent) {
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
            	// The new variable created from return type has to be declared properly.
                text.<^Space|dynamic.*142.*%>
            }
        });
    }
}
