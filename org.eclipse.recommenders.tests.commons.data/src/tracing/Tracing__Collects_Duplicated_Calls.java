package tracing;


import acme.Button;


public class Tracing__Collects_Duplicated_Calls
{

    void __test(final Button c)
    {
        c.hashCode();
        c.foo1();
        c.foo1();
        c.equals(null);
        c.equals(null);
    }
}
