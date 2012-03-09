/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.mining.calls.generation;

import org.eclipse.recommenders.commons.mining.Observation;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;

public class ObjectUsageImporter {

	private Observation observation;

	public Observation transform(ObjectUsage objectUsage) {
		observation = new Observation();

		observation.setType(objectUsage.type);

		Checks.ensureIsNotNull(objectUsage.contextFirst);
		observation.setContext(objectUsage.contextFirst);

		for (IMethodName call : objectUsage.calls) {
			if (!call.isInit()) {
				IMethodName rebasedCall = VmMethodName.rebase(objectUsage.type, call);
				observation.addCall(rebasedCall);
			}
		}

		Checks.ensureIsNotNull(objectUsage.kind);
		observation.setKind(objectUsage.kind);

		Checks.ensureIsNotNull(objectUsage.definition);
		observation.setDefinition(objectUsage.definition);

		return observation;
	}
}