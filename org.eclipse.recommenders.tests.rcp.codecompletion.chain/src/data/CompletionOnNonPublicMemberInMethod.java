package data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionOnNonPublicMemberInMethod {

	protected AtomicBoolean findMe1 = new AtomicBoolean();

	AtomicInteger findMe2 = new AtomicInteger();

	private AtomicLong findMe3 = new AtomicLong();

	public static void test_protected() {
		CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		AtomicBoolean c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe1
		 * variable name --> c
		 */
	}

	public static void test_default() {
		CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		AtomicInteger c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * expected completion --> useMe.findMe2
		 * variable name --> c
		 */
	}

	public static void test_private() {
		CompletionOnNonPublicMemberInMethod useMe = new CompletionOnNonPublicMemberInMethod();
		AtomicLong c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicLong
         * expected completion --> useMe.findMe3
		 * variable name --> c
		 */
	}

}
