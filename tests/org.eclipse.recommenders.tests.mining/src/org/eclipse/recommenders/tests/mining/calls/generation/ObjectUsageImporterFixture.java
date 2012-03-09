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
package org.eclipse.recommenders.tests.mining.calls.generation;

import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.NO_METHOD;

import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.Sets;

public class ObjectUsageImporterFixture {

	public static final ITypeName TYPE = VmTypeName.get("LType");
	public static final IMethodName CONTEXT = VmMethodName.get("LType.context()V");
	public static final IMethodName SUPER_CONTEXT = VmMethodName.get("LType.superContext()V");
	public static final DefinitionSite.Kind KIND_NEW = DefinitionSite.Kind.NEW;
	public static final DefinitionSite.Kind KIND_RETURN = DefinitionSite.Kind.METHOD_RETURN;
	public static final DefinitionSite.Kind KIND_PARAM = DefinitionSite.Kind.PARAMETER;
	public static final DefinitionSite.Kind KIND_FIELD = DefinitionSite.Kind.FIELD;
	public static final IMethodName DEFINITION = VmMethodName.get("LType.definition()V");
	public static final IMethodName NO_DEFINITION = VmMethodName.get(NO_METHOD.toString());
	public static final IMethodName INIT_CALL = VmMethodName.get("LType.<init>()V");
	public static final IMethodName CALL1 = VmMethodName.get("LType.call1()V");
	public static final IMethodName CALL2 = VmMethodName.get("LSub1Type.call2()V");
	public static final IMethodName CALL3 = VmMethodName.get("LSub2Type.call3()V");

	public static final IMethodName CALL2_REBASED = VmMethodName.get("LType.call2()V");
	public static final IMethodName CALL3_REBASED = VmMethodName.get("LType.call3()V");

	private static final boolean WITHOUT_INIT = false;
	private static final boolean WITH_INIT = true;

	public ObjectUsage getReturnUsage() {
		ObjectUsage objectUsage = createBasicUsage();

		objectUsage.kind = Kind.METHOD_RETURN;
		objectUsage.definition = DEFINITION;

		addCalls(objectUsage, WITHOUT_INIT);

		return objectUsage;
	}

	public ObjectUsage getNewUsage() {
		ObjectUsage objectUsage = createBasicUsage();

		objectUsage.kind = Kind.NEW;
		objectUsage.definition = INIT_CALL;

		addCalls(objectUsage, WITH_INIT);

		return objectUsage;
	}

	public ObjectUsage getParameterUsage() {
		ObjectUsage objectUsage = createBasicUsage();

		objectUsage.kind = Kind.PARAMETER;
		objectUsage.definition = NO_DEFINITION;

		addCalls(objectUsage, WITHOUT_INIT);

		return objectUsage;
	}

	public ObjectUsage getFieldUsage() {
		ObjectUsage objectUsage = createBasicUsage();

		objectUsage.kind = Kind.FIELD;
		objectUsage.definition = NO_DEFINITION;

		addCalls(objectUsage, WITHOUT_INIT);

		return objectUsage;
	}

	public ObjectUsage getUsageWithoutContextAndDefinition() {
		ObjectUsage usage = getReturnUsage();
		usage.contextFirst = null;
		usage.contextSuper = null;
		usage.definition = null;
		return usage;
	}

	private static void addCalls(ObjectUsage objectUsage, boolean withInit) {
		objectUsage.calls = Sets.newHashSet();
		objectUsage.calls.add(CALL1);
		objectUsage.calls.add(CALL2);

		if (withInit) {
			objectUsage.calls.add(INIT_CALL);
		}
	}

	private static ObjectUsage createBasicUsage() {
		ObjectUsage objectUsage = new ObjectUsage();

		objectUsage.type = TYPE;
		objectUsage.contextFirst = CONTEXT;
		objectUsage.contextSuper = SUPER_CONTEXT;

		return objectUsage;
	}
}
