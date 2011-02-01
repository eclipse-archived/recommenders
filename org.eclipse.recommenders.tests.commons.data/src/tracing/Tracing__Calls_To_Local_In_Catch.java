package tracing;

@SuppressWarnings("unused")
public class Tracing__Calls_To_Local_In_Catch {

    void __test() {
        final String c = new String("");
        try {
            c.split("");
            throw new IllegalArgumentException();
        } catch (final Exception e) {
        }
    }

    private void throwException() throws IllegalArgumentException {
        throw new IllegalAccessError("");
    }
}
