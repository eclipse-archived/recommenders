/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package helper;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FieldsWithDifferentVisibilities {
	protected AtomicBoolean findMe1 = new AtomicBoolean();

    AtomicInteger findMe2 = new AtomicInteger();

    private final AtomicLong findMe3 = new AtomicLong();
}
