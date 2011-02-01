package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnSimpleTypeNameInMethodBodyWithImport extends Dialog {

    private Button b;

    @Override
	protected Control createDialogArea(final Composite parent) {
		// Button<^Space> -> expects patterns for SWT Button only since Button
		// has been qualified by an import already
		//
		Button<^Sapce>
		return null;
	}

    private CompletionOnSimpleTypeNameInMethodBodyWithImport() {
        super((IShellProvider) null);
    }
}
