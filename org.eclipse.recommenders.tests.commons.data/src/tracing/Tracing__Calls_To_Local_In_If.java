package tracing;


import acme.Button;


public class Tracing__Calls_To_Local_In_If
{

    void __test(final boolean condition)
    {
        final Button c = new Button();
        if (condition)
        {
            c.notify();
            c.equals(null);
        }
        c.equals(null);
        c.getClass();
    }
}
