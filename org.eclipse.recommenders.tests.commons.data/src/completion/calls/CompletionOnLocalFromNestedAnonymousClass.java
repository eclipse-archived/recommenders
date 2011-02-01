package completion.calls;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CompletionOnLocalFromNestedAnonymousClass extends DialogPage {

    Text text;

    @Override
    public void createControl(final Composite parent) {
        text.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                text.<^Space>
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                text.<^Space>
            }
        });
    }
}
