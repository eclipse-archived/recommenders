package tracing;


import acme.Button;


public class Tracing__Recursive_Calls_To_This
{

    void __test(final Button s1, final Button s2, final Button s3)
    {
        s2.foo1();
        __test(s1, s2, s3);
    }
}
