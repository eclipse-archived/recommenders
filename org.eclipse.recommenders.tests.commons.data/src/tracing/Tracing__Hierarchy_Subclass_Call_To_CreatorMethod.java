package tracing;


import acme.Button;


public class Tracing__Hierarchy_Subclass_Call_To_CreatorMethod extends Tracing__Hierarchy_Rootclass
{

    public void __test()
    {
        Button createNewButton = createNewButton();
        createNewButton.foo1();
    }
}
