package data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompletionOnGenericTypeInMethod {

	public List<String> findMe = new ArrayList<String>();

	public static void test_exactGenericType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<String> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<String>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_exactButWrongGenericType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<Integer> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<Integer>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_anonymousGenericType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<?> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<?>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_genericSuperType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<? extends Serializable> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? extends Serializable>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_wrongGenericSuperType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<? extends File> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? extends File>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_genericSubType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<? super String> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? super String>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

	public static void test_wrongGenericSubType() {
		CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		List<? super Serializable> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? super Serializable>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}
}
