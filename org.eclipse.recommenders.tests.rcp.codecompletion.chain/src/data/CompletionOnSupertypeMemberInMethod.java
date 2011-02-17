package data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionOnSupertypeMemberInMethod {

	public static class Subtype extends CompletionOnSupertypeMemberInMethod {
	}

	public AtomicBoolean findMe = new AtomicBoolean();

	public AtomicInteger findMe() {
		return new AtomicInteger();
	}

	public static void test_onAttribute() {
		Subtype useMe = new Subtype();
		AtomicBoolean c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe
		 * variable name --> c
		 */
	}

	public static void test_onMethod() {
		Subtype useMe = new Subtype();
		AtomicInteger c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * expected completion --> useMe.findMe()
		 * variable name --> c
		 */
	}
}
