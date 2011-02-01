package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnLocalWithObservedContructor extends Dialog {

    private Button b;

    @Override
	protected Control createDialogArea(final Composite parent) {
		final Button b = new Button(null, 0);
		// b.<^Space> -> pattern w/o contructor
		b.<^Space>
		return null;
	}

    private CompletionOnLocalWithObservedContructor() {
        super((IShellProvider) null);
    }
}
