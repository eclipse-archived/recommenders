package tests.templates;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class T03 extends Dialog {

	private Button b;

	@Override
	protected Control createDialogArea(final Composite parent) {
		// Button<^Space> -> expects patterns for SWT Button only since Button
		// has been qualified by an import already
		//
		
		return null;
	}

	protected T03(final IShellProvider parentShell) {
		super(parentShell);
	}
}
