package tracing;


import acme.Composite;
import acme.GridData;


public class Tracing__Calls_To_Anonymous_Local_In_If
{

    private int style;
    private int FLAT;
    private Composite defaultParent;



    Composite __test(final boolean condition)
    {
        if (style == FLAT)
        {
            final Composite parent = new Composite(defaultParent, FLAT);
            parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            return parent;
        }
        return defaultParent;
    }
}
