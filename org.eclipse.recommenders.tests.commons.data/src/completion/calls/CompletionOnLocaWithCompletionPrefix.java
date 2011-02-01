package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnLocaWithCompletionPrefix extends Dialog {

    private Button b;

    @Override
	protected Control createDialogArea(final Composite parent) {
		final Button b = new Button(null, 0);
		// b.sett<^Space> -> pattern w/o constructor but w/ setText
		//
		b.sett<^Space>
		return null;
	}

    private CompletionOnLocaWithCompletionPrefix() {
        super((IShellProvider) null);
    }
}
