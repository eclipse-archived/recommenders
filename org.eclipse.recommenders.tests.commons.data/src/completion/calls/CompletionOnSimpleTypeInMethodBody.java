package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnSimpleTypeInMethodBody extends Dialog {

    @Override
	protected Control createDialogArea(final Composite parent) {
		// Button<^Space> -> give patterns and import button then
		// ensure Button is not imported as SWT or AWT Button before
		Button<^Space>
		return null;
	}

    private CompletionOnSimpleTypeInMethodBody() {
        super((IShellProvider) null);
    }
}