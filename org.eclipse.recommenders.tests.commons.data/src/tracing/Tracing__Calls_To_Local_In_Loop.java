package tracing;


import acme.Button;


public class Tracing__Calls_To_Local_In_Loop
{

    public void __test()
    {
        final Button b = new Button();
        for (int i = 5; i-- > 0;)
        {
            b.foo1();
        }
        b.foo2();
    }
}
