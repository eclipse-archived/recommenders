package data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionViaStaticArrayInMethod {

	public AtomicBoolean findMe = new AtomicBoolean();

	public static CompletionViaStaticArrayInMethod useUs[] = {
			new CompletionViaStaticArrayInMethod(),
			new CompletionViaStaticArrayInMethod() };

	public static void method1() {
		final AtomicBoolean c = <^Space>
        /* calling context --> static
         * expected type --> AtomicBoolean
         * expected completion --> useUs[i].findMe
         * variable name --> c
         */
    }
}
