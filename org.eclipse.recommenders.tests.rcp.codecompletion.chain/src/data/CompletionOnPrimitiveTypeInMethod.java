package data;

public class CompletionOnPrimitiveTypeInMethod {

	public int findMe = 5;

	public static void method() {
		CompletionOnPrimitiveTypeInMethod useMe = new CompletionOnPrimitiveTypeInMethod();
		int c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> int
         * expected completion --> useMe.findMe
		 * variable name --> c
		 */
	}

}
