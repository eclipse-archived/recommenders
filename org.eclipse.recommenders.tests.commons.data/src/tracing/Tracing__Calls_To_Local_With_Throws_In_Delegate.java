package tracing;


import acme.Button;


public class Tracing__Calls_To_Local_With_Throws_In_Delegate
{

    public void __test()
    {
        final Button s = new Button();
        throwsException();
        s.foo1();
    }



    private void throwsException()
    {
        throw new IllegalStateException();
    }
}
