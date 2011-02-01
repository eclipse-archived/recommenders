package tracing;


import acme.Button;


public class Tracing__Calls_To_Private_Return_Value_Is_Unitialized_Field
{

    private Button c;



    public void __test()
    {
        final Button s = getC();
        s.getClass();
        s.foo1();
    }



    private Button getC()
    {
        return c;
    }
}
