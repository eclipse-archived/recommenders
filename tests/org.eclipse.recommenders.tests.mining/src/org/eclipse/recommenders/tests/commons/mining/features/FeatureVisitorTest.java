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
package org.eclipse.recommenders.tests.commons.mining.features;

import org.eclipse.recommenders.commons.mining.features.CallFeature;
import org.eclipse.recommenders.commons.mining.features.ContextFeature;
import org.eclipse.recommenders.commons.mining.features.DefinitionFeature;
import org.eclipse.recommenders.commons.mining.features.FeatureVisitor;
import org.eclipse.recommenders.commons.mining.features.KindFeature;
import org.eclipse.recommenders.commons.mining.features.TypeFeature;
import org.junit.Test;

public class FeatureVisitorTest {

	@Test
	public void assertAllVisitMethodsExist() {
		FeatureVisitor visitor = new FeatureVisitor() {
		};

		visitor.visit(new TypeFeature(null));
		visitor.visit(new ContextFeature(null));
		visitor.visit(new KindFeature(null));
		visitor.visit(new DefinitionFeature(null));
		visitor.visit(new CallFeature(null));
	}
}
