package tests.templates;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class T07 extends Dialog {

	@Override
	protected Control createDialogArea(final Composite parent) {
		// Button<^Space> -> give patterns and import button then
		// ensure Button is not imported as SWT or AWT Button before
		Button
		return null;
	}

	protected T07(final IShellProvider parentShell) {
		super(parentShell);
	}
}