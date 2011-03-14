package data;

//call chain 1 ok
public class CompletionOnArrayWithCastsSubtype {
	public Number[][][] findme;
	
	public static void method1() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer) obj.findme[i][i][i]
         */
	}
	
	public static void method2() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[]) obj.findme[i][i]
         */
	}
	
	public static void method3() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[][]) obj.findme[i]
         */
	}
	
	public static void method4() {
		final CompletionOnArrayWithCastsSubtype obj = new CompletionOnArrayWithCastsSubtype();
        final Integer[][][] c = <@Ignore^Space>
        /* calling context --> static
         * expected completion --> (Integer[][][]) obj.findme
         */
	}
}
