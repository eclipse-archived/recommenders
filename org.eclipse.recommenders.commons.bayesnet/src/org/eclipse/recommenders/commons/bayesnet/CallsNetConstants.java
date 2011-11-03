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
package org.eclipse.recommenders.commons.bayesnet;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

public class CallsNetConstants {
	public static final String NODE_ID_PATTERNS = "patterns";
	public static final String NODE_ID_CONTEXT = "contexts";
	public static final String NODE_ID_KIND = "kinds";
	public static final String NODE_ID_DEFINITION = "definitions";
	public static final String NODE_ID_CALL_PREFIX = "call:";

	public static final String STATE_TRUE = "true";
	public static final String STATE_FALSE = "false";
}