package classselector;

import org.eclipse.swt.widgets.Button;

public abstract class AppUsingClassNotInClasspath {

    @SuppressWarnings("unused")
    public void __test() {
        final Button button = new Button(null, 0);
    }
}
