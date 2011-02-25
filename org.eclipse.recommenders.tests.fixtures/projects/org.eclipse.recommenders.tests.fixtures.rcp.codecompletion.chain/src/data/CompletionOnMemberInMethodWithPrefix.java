/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionOnMemberInMethodWithPrefix {

    public AtomicBoolean findMe = new AtomicBoolean();

    public CompletionOnMemberInMethodWithPrefix getSubElement() {
        return new CompletionOnMemberInMethodWithPrefix();
    }

    public static void method2() {
        //@start
		final CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();
		final AtomicBoolean c = useMe.get<^Space|getSubElement.+findMe.*>
		//@end
		//final CompletionOnMemberInMethodWithPrefix useMe = new CompletionOnMemberInMethodWithPrefix();
		//final AtomicBoolean c = useMe.getSubElement().findMe
	}
}
