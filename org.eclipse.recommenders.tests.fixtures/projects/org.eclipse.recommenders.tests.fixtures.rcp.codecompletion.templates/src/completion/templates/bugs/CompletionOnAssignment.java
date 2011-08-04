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
package completion.templates.bugs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CompletionOnAssignment extends Dialog {

	protected CompletionOnAssignment(final Shell parentShell) {
		super(parentShell);
	}

	@Override
    protected Control createDialogArea(final Composite parent) {
        // TODO: link to Button b in context
		final Button b = <^Space|new Button.*%>
        return null;
    }

	private void noCompletionExpected(){
		final Button b = new Button(null, 0);
		final String s = b.<@Ignore^Space>;
	}
	
	private void assignmentToNewVariable(){
		Button b<^Space| = new Button.*%>
	}
}
