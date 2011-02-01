package org.eclipse.recommenders.examples.demo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

/**
 * Demo outline:
 * <ol>
 * <li>intelligent calls code completion</li>
 * <li>dynamic code templates</li>
 * <li>extended javadoc</li>
 * <li>call-chain completion</li>
 * <ol>
 */
public class MyDialog extends Dialog {

	private Text swtTextWidget;

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = createContainer(parent);
		swtTextWidget.setLayoutData(new RowData());

		final Button b = new Button(parent, 0);
		b.addSelectionListener(null);
		b.setSelection(true);
		b.setText(null);
		final IWorkbenchHelpSystem help = PlatformUI.getWorkbench()
				.getHelpSystem();
 
		return container;
	}

	private Composite createContainer(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(null);
		return container;
	}

	protected MyDialog(final IShellProvider parentShell) {
		super(parentShell);
	}
}
