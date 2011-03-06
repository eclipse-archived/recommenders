package completion.templates;

import org.eclipse.swt.widgets.Button;

public class CompletionOnNullVariable {
	
	CompletionOnNullVariable(){
		Button b = null;
		b.setEnabled(true);
		b.<^Space|dynamic.*218.*%>
	}

}
