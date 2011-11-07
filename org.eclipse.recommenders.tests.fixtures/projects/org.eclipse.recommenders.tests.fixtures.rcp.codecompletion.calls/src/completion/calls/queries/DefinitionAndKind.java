/*******************************************************************************
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 ******************************************************************************/
package completion.calls.queries;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

import completion.calls.queries.helper.Helper;

public class DefinitionAndKind extends Dialog {

	Button a;

	public DefinitionAndKind(Shell parent) {
		super(parent);
	}

	public void usageOfField() {
//		 a. // FIELD - None
	}
	
	public void onInitFieldsAreConsideredNew() {
		a = new Button(null, 0);
//		a. // New - <init>		
	}

	public void usageOfParam(Button b) {
//		 b. // PARAMETER - None
	}

	public void usageOfNewObject() {
		Button c = new Button(null, 0);
//		 c. // NEW - Button.<init>(...)
		
		// TODO check difference for ASTBased (uncomment vs. typing)
	}

	public void usageOfMethodReturn() {
		Button d = Helper.createButton();
//		 d. // RETURN - Thingy.create()
	}

	public void unknownDefinition() {
		Button e;
//		 e. // UNKNOWN
	}

	public void selfCallAreAtmNotCorrectlyResolved() {
		// test also without explicit "this."
		// this. // THIS - None
//		this.
	}
}