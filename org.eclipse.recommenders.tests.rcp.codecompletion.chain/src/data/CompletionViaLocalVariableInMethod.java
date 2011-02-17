package data;

import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionViaLocalVariableInMethod {

	public AtomicBoolean findMe = new AtomicBoolean();

	public static void method() {
		CompletionViaLocalVariableInMethod variable = new CompletionViaLocalVariableInMethod();
		AtomicBoolean c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

}
