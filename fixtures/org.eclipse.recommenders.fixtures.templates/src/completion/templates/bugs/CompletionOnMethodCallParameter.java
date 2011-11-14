/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package completion.templates.bugs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class CompletionOnMethodCallParameter {

	CompletionOnMethodCallParameter() {
		Button button = new Button(null, SWT.NONE);
		System.err.println("This shouldn't give any templates: "+<^Space>);
		System.err.println(button.);
	}
}
