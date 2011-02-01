package tracing;


import acme.Button;


public class Tracing__Calls_To_Field_Without_Init
{

    private Button c;



    void __test()
    {
        c.notify();
        c.equals(null);
        c.getClass();
    }
}
