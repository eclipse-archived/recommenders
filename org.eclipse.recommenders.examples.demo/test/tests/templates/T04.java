package tests.templates;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class T04 extends Dialog {

	private Button b;

	@Override
	protected Control createDialogArea(final Composite parent) {
		Button b = new Button(null, 0);
		// b.<^Space> -> pattern w/o contructor
		//
		b.
		return null;
	}

	protected T04(final IShellProvider parentShell) {
		super(parentShell);
	}
}
