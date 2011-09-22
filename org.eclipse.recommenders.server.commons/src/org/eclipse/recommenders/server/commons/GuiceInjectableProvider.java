/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.server.commons;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

public class GuiceInjectableProvider implements InjectableProvider<Inject, Type> {

    private final Injector injector;

    @Inject
    public GuiceInjectableProvider(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public Injectable<?> getInjectable(final ComponentContext ctx, final Inject annotation, final Type type) {
        return new Injectable<Object>() {

            @Override
            public Object getValue() {
                final Annotation annotation = findAnnotation(ctx.getAnnotations());
                if (annotation == null) {
                    return injector.getInstance(Key.get(type));
                } else {
                    return injector.getInstance(Key.get(type, annotation));
                }
            }
        };
    }

    protected static Annotation findAnnotation(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (!annotation.annotationType().equals(Inject.class)) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Undefined;
    }

}
