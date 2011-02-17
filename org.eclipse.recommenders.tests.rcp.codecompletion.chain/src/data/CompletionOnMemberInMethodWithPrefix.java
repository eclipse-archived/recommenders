package data;

import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionOnMemberInMethodWithPrefix {

	public AtomicBoolean findMe = new AtomicBoolean();

	public CompletionOnMemberInMethodWithPrefix getSubElement() {
		return new CompletionOnMemberInMethodWithPrefix();
	}

	public static void method1() {
		CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();
		AtomicBoolean c = use<^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> [use]Me.findMe
		 * variable name --> c
		 */
	}

	public static void method2() {
		CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();
		AtomicBoolean c = useMe.get<^Space>
		/*
		 * calling context --> static
		 * expected type --> AtomicBoolean
         * expected completion --> [get]SubElement().findMe
		 * variable name --> c
		 */
	}
}
