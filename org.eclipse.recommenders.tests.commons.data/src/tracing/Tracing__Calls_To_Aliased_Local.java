package tracing;


import acme.Button;


public class Tracing__Calls_To_Aliased_Local
{

    public int __test(final Helper h)
    {
        Button uri1 = h.getButton();
        if (uri1 == null)
        {
            uri1 = new Button();
        }
        final Button uri2 = h.getButton();
        // if (uri2 == null)
        // {
        // uri2 = new Button();
        // }
        final int result = uri1.compareTo(uri2);
        return result;
    }
}
