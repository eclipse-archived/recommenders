package tracing;


import acme.Button;


public class Tracing__SimpleIfWithDelegate
{

    public void __test(final boolean flag)
    {
        final Button b = new Button();
        b.foo1();
        if (flag)
        {
            delegate(b);
        }
        b.foo3();
    }



    private void delegate(final Button button)
    {
        button.foo2();
    }
}
