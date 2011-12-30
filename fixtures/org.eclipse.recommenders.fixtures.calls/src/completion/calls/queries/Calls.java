package completion.calls.queries;

import org.eclipse.swt.widgets.Button;

public class Calls {

	Button b;

	public void initsAreNotRegisteredAsCalls() {
		b = new Button(null, 0);
	}

	public void allCallsAreRegistered() {
		b = new Button(null, 0);
		b.setAlignment(1);
		b.setEnabled(true);
		// b.
	}
}
