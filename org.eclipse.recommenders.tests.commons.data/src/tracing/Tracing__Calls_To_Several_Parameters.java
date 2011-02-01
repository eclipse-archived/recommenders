package tracing;


import acme.Button;


public class Tracing__Calls_To_Several_Parameters
{

    void __test(final Button s1, final Button s2, final Button s3)
    {
        s1.equals(null);
        //
        s2.foo1();
        s2.hashCode();
        //
        s3.equals(null);
        s3.notify();
        s3.toString();
    }
}
