package tracing;


import acme.Button;


public class Tracing__Calls_To_Private_Return_Value_With_Init
{

    public void __test()
    {
        final Button s = ret();
        s.foo1();
        s.hashCode();
    }



    private Button ret()
    {
        final Button c = new Button();
        return c;
    }
}
