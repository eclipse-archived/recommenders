package tracing;


import acme.Button;


public class Tracing__Calls_To_Local_With_Init
{

    void __test()
    {
        final Button c = new Button();
        c.notify();
        c.equals(null);
        c.getClass();
    }
}
