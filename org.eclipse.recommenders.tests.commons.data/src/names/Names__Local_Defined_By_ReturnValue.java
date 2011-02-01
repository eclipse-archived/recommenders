package names;

import acme.Button;

@SuppressWarnings("unused")
public class Names__Local_Defined_By_ReturnValue {

    void __test() {
        final Button local = getButton();
    }

    private Button getButton() {
        return new Button();
    }
}
