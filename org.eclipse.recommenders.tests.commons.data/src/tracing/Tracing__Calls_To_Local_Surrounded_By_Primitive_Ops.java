package tracing;

import acme.Composite;
import acme.GridData;

@SuppressWarnings("unused")
public class Tracing__Calls_To_Local_Surrounded_By_Primitive_Ops {

    private int style;
    private static int FLAT;
    private Composite defaultParent;
    Composite exception;

    void __test(final boolean condition) {
        loadExceptionText();
        int i = FLAT;
        i++;
        final GridData gd = new GridData(i);
        gd.exclude = true;
        exception.setLayoutData(gd);
    }

    private void loadExceptionText() {
    }
}
