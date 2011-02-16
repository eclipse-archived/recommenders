package completion.templates.bugs;

import org.eclipse.swt.widgets.Button;

public class CompletionOnNullVariable {
	
	CompletionOnNullVariable(){
		Button b = null;
		// Should only contain patterns with constructor.
		b.<^Space>
	}

}
