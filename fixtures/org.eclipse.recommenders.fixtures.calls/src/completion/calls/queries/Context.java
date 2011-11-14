package completion.calls.queries;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

import completion.calls.queries.helper.AbstractMouseListener;

public class Context extends Dialog {

	Button a;

	public Context(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getText() {
		// a. // contextFirst -> Dialog.getText(...)
		return null;
	}

	@Override
	public void setText(String string) {
		a.addMouseListener(new AbstractMouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// ctxFirst: MouseListener.mouseDoubleClick
				// a.
			}
		});
	}
}