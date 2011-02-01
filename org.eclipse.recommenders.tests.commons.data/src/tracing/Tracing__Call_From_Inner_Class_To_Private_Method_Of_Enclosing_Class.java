package tracing;


import acme.Button;


public class Tracing__Call_From_Inner_Class_To_Private_Method_Of_Enclosing_Class
{

    public class InnerClass
    {

        void __test()
        {
            callToPrivateMethodOfEnclosingClass();
        }
    }



    private final Button b = new Button();



    private void callToPrivateMethodOfEnclosingClass()
    {
        b.foo1();
    }
}
