package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnUninitializedAndQualifiedFieldName extends Dialog {

    private Button b;

    @Override
	protected Control createDialogArea(final Composite parent) {
		b.<^Space>
		return null;
	}

    protected CompletionOnUninitializedAndQualifiedFieldName() {
        super((IShellProvider) null);
    }
}
