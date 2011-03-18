package data;

import java.util.concurrent.atomic.AtomicInteger;

//call chain 1 ok
public class CompletionOnArrayWithCastsSupertype {
	public Integer[][][] findme;
	
	public static void method1() {
		final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
        final Number c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> obj.findme[i][i][i]
         */
	}
	
	public static void method2() {
		final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
        final Number[] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> obj.findme[i][i]
         */
	}
	
	public static void method3() {
		final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
        final Number[][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> obj.findme[i]
         */
	}
	
	public static void method4() {
		final CompletionOnArrayWithCastsSupertype obj = new CompletionOnArrayWithCastsSupertype();
        final Number[][][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> obj.findme
         */
	}
	
}
