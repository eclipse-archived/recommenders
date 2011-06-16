/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.server.extdoc;

import com.sun.jersey.api.client.GenericType;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.server.extdoc.types.MethodOverrideExamples;

public final class ExamplesServer extends AbstractRatingsServer {

    public MethodOverrideExamples getMethodOverrideExamples(final IMethodName method) {
        return Server.getProviderContent(MethodOverrideExamples.class.getSimpleName(), "method",
                method.getIdentifier(), new GenericType<GenericResultObjectView<MethodOverrideExamples>>() {
                });
    }

}
