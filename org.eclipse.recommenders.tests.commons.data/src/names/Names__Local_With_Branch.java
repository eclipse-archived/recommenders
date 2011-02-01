package names;


import acme.Button;


public class Names__Local_With_Branch
{

    void test(final boolean flag)
    {
        Button local = null;
        if (flag)
        {
            local = new Button();
        } else
        {
            local = new Button();
        }
        local.hashCode();
    }
}
