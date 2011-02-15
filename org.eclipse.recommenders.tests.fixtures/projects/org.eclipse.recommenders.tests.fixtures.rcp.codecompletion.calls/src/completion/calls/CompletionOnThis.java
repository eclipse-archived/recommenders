package completion.calls;

import org.eclipse.jface.wizard.Wizard;

public class CompletionOnThis extends Wizard {

	@Override
    public void addPages() {
         <^Space|addPage.*%>
    }

	@Override
	public boolean performFinish() {

		return false;
	}

}
