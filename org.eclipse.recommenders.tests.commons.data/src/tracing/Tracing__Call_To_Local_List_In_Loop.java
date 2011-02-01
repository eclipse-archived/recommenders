package tracing;


import java.util.LinkedList;
import java.util.List;

import acme.Button;


public class Tracing__Call_To_Local_List_In_Loop
{

    void __test()
    {
        final List<Button> buttons = new LinkedList<Button>();
        buttons.add(new Button());
        buttons.add(new Button());
        loop(buttons);
    }



    private void loop(final List<Button> buttons)
    {
        for (final Button b : buttons)
        {
            b.foo1();
        }
    }
}
