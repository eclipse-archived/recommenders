package tracing;


import acme.Button;


public class Tracing__Calls_To_Field_With_Init
{

    private Button c;



    void __test()
    {
        c = new Button();
        c.notify();
        c.equals(null);
        c.getClass();
    }
}
