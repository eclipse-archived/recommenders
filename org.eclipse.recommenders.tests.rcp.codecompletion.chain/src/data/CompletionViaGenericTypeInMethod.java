package data;

import java.util.Arrays;
import java.util.Iterator;

public class CompletionViaGenericTypeInMethod {

	public static void method() {
		Iterator<CompletionViaGenericTypeInMethod> useMe = Arrays.asList(
				new CompletionViaGenericTypeInMethod()).iterator();
		CompletionViaGenericTypeInMethod c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> CompletionViaGenericTypeInMethod
		 * expected completion --> useMe.next()
		 * variable name --> c
		 */
	}
}
