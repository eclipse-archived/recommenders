package data;

import helper.FieldsWithDifferentVisibilities;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionOnNonPublicMembersOfOtherClassInMethod {

	public static FieldsWithDifferentVisibilities useMe = new FieldsWithDifferentVisibilities();

	public static void test_protected() {
		AtomicBoolean c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * NOT expected completion --> useMe.findMe1
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
