package data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompletionWithCastInMethod {

	public InputStream findMe = new ByteArrayInputStream(new byte[] { 0, 1, 2,
			3 });

	public Collection<String> findMe2 = new ArrayList<String>();

	public static void test_castToSubClass() {
		CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		ByteArrayInputStream c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> ByteArrayInputStream
         * expected completion --> (ByteArrayInputStream) useMe.findMe
		 * variable name --> c
		 */
	}

	public static void test_castToInterface() {
		CompletionWithCastInMethod useMe = new CompletionWithCastInMethod();
		List<String> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<String>
         * expected completion --> (List) useMe.findMe2
		 * variable name --> c
		 */
	}
}
