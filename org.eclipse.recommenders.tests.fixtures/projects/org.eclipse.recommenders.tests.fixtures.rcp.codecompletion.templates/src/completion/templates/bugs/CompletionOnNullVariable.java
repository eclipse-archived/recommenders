package completion.templates.bugs;

import org.eclipse.swt.widgets.Button;

public class CompletionOnNullVariable {
	
	CompletionOnNullVariable(){
		Button b = null;
		b.setEnabled(true);
		// Should only contain patterns with constructor.
		b.<^Space>
	}

}
