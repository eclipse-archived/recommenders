package data;

import java.util.concurrent.atomic.AtomicInteger;

public class CompletionOnArrayMemberAccessInMethod {

	public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };

	public static void method1() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger c = <^Space>
        /* calling context --> static
         * expected type --> AtomicInteger
         * expected completion --> obj.findUs[i]
         * variable name --> c
         */
    }

	public static void method2() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger[] c = <^Space>
        /* calling context --> static
         * expected type --> AtomicInteger[]
         * expected completion --> obj.findUs
         * variable name --> c
         */
    }

	public static void method3() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger[][] c = <^Space>
        /* calling context --> static
         * expected type --> AtomicInteger[][]
         * NOT expected completion --> obj.findUs
         * variable name --> c
         */
    }
}
