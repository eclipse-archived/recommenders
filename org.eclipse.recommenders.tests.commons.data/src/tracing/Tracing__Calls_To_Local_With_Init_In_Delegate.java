package tracing;


import acme.Button;


public class Tracing__Calls_To_Local_With_Init_In_Delegate
{

    private void delegate()
    {
        final Button c = new Button();
        c.notify();
        c.equals(null);
        c.getClass();
    }



    void __test()
    {
        delegate();
    }
}
