package tracing;


import acme.Button;


public class Tracing__Hierarchy_Subclass extends Tracing__Hierarchy_Rootclass
{

    @Override
    public void __test(final Button c)
    {
        super.__test(c);
        c.foo1();
        indirection1(c);
    }



    void indirection1(final Button c)
    {
        c.foo2();
        indirection2(c);
    }



    void indirection2(final Button x)
    {
        x.foo3();
    }
}
