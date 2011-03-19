package scenarios;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class GetStatusLineInView extends ViewPart {
	
	public void reportStatusMessage(final String message) {
		final IStatusLineManager statusLine = null;
		statusLine.setMessage(message);
	}
	@Override
	public void createPartControl(final Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}