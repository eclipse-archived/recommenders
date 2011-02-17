package data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class CompletionOnSupertypeInMethod {

    public ByteArrayInputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 });

    public static void method() {
		CompletionOnSupertypeInMethod useMe = new CompletionOnSupertypeInMethod();
		InputStream c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> InputStream
         * expected completion --> useMe.findMe
		 * variable name --> c
		 */
	}
}
