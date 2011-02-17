package data;

import helper.FieldsWithDifferentVisibilities;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionOnNonPublicMembersOfSuperClassInMethod extends
		FieldsWithDifferentVisibilities {

	public static CompletionOnNonPublicMembersOfSuperClassInMethod useMe = new CompletionOnNonPublicMembersOfSuperClassInMethod();

	public static void test_protected() {
		AtomicBoolean c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> useMe.findMe1
		 * variable name --> c
		 */
	}

	public static void test_default() {
		AtomicInteger c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicInteger
         * NOT expected completion --> useMe.findMe2
		 * variable name --> c
		 */
	}

	public static void test_private() {
		AtomicLong c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicLong
         * NOT expected completion --> useMe.findMe3
		 * variable name --> c
		 */
	}
}
