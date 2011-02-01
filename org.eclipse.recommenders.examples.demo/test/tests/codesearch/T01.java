package tests.codesearch;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class T01 extends ViewPart {
	public void setMessage(final String newMessage) {
		//
		// == How do I get an instance of IStatusLineManager? ==
		//
		final IStatusLineManager manager = null;
		manager.setMessage(newMessage);
	}

	@Override
	public void createPartControl(final Composite arg0) {
	}

	@Override
	public void setFocus() {

	}
}
