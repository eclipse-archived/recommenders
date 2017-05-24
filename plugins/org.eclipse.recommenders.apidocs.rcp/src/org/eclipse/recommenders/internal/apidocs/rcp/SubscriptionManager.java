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
package org.eclipse.recommenders.internal.apidocs.rcp;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Pair.newPair;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation;
import org.eclipse.recommenders.utils.Pair;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Optional;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class SubscriptionManager {

    private final Multimap<Subscription, Pair<ApidocProvider, Method>> subscriptions = LinkedHashMultimap.create();

    @Inject
    public SubscriptionManager(final List<ApidocProvider> providers) {
        ensureIsNotNull(providers);
        for (final ApidocProvider p : providers) {
            register(p);
        }
    }

    private void register(final ApidocProvider provider) {

        final Set<Pair<Method, JavaSelectionSubscriber>> annotatedMethods = findAnnotatedMethods(provider);

        if (annotatedMethods.isEmpty()) {
            throwIllegalArgumentException("no listeners found"); //$NON-NLS-1$
        }

        for (final Pair<Method, JavaSelectionSubscriber> t : annotatedMethods) {
            addSubscription(provider, t.getFirst(), t.getSecond());
        }
    }

    private Set<Pair<Method, JavaSelectionSubscriber>> findAnnotatedMethods(final ApidocProvider provider) {

        final Set<Pair<Method, JavaSelectionSubscriber>> methods = new LinkedHashSet<>();

        final Class<?> clazz = provider.getClass();
        for (final Method m : clazz.getMethods()) {
            final JavaSelectionSubscriber annotation = m.getAnnotation(JavaSelectionSubscriber.class);
            if (annotation != null) {
                ensureCorrectMethodSignature(m);
                methods.add(newPair(m, annotation));
            }
        }

        return methods;
    }

    private static void ensureCorrectMethodSignature(final Method m) {
        final Class<?>[] params = m.getParameterTypes();
        ensureParameterLengthIsThree(params, m);
        ensureFirstParameterTypeIsJavaElement(params, m);
        ensureSecondParameterTypeIsJavaSelectionEvent(params, m);
        ensureThirdParameterTypeIsComposite(params, m);
    }

    private static void ensureParameterLengthIsThree(final Class<?>[] params, final Method m) {
        if (params.length != 3) {
            throwIllegalArgumentException("error in %s: at least 3 parameters expected", m.toGenericString()); //$NON-NLS-1$
        }
    }

    private static void ensureFirstParameterTypeIsJavaElement(final Class<?>[] params, final Method m) {
        if (!IJavaElement.class.isAssignableFrom(params[0])) {
            throwIllegalArgumentException("error in %s: first parameter needs to be %s or a subclass", //$NON-NLS-1$
                    m.toGenericString(), IJavaElement.class.getName());
        }
    }

    private static void ensureSecondParameterTypeIsJavaSelectionEvent(final Class<?>[] params, final Method m) {
        if (!JavaElementSelectionEvent.class.isAssignableFrom(params[1])) {
            throwIllegalArgumentException("error in %s: second parameter needs to be %s or a subclass", //$NON-NLS-1$
                    m.toGenericString(), JavaElementSelectionEvent.class.getName());
        }
    }

    private static void ensureThirdParameterTypeIsComposite(final Class<?>[] params, final Method m) {
        if (!Composite.class.isAssignableFrom(params[2])) {
            throwIllegalArgumentException("error in %s: third parameter needs to be %s or a subclass", //$NON-NLS-1$
                    m.toGenericString(), Composite.class.getName());
        }
    }

    private void addSubscription(final ApidocProvider provider, final Method method,
            final JavaSelectionSubscriber annotation) {
        final JavaElementSelectionLocation[] locs = annotation.value();
        final Class<?> javaElementType = method.getParameterTypes()[0];

        final Pair<ApidocProvider, Method> subscriber = newPair(provider, method);

        if (locs.length == 0) {
            final Subscription subscription = Subscription.create(javaElementType, null);
            subscriptions.put(subscription, subscriber);
        } else {
            for (final JavaElementSelectionLocation loc : locs) {
                final Subscription subscription = Subscription.create(javaElementType, loc);
                subscriptions.put(subscription, subscriber);
            }
        }
    }

    /**
     * Returns a method of the given provider that is subscribed for the selection event - or <em>absent</em> if none is
     * found. If the subscription of multiple methods overlaps, no guarantee is given which method is returned
     */
    public Optional<Method> findSubscribedMethod(final ApidocProvider provider,
            final JavaElementSelectionEvent selection) {
        for (final Subscription s : subscriptions.keySet()) {
            if (s.isInterestedIn(selection)) {
                for (final Pair<ApidocProvider, Method> t : subscriptions.get(s)) {
                    if (provider.equals(t.getFirst())) {
                        return of(t.getSecond());
                    }
                }
            }
        }
        return absent();
    }

    public static class Subscription {
        private Class<?> interestedJavaElementClass;
        private JavaElementSelectionLocation interestedLocation;

        public static Subscription create(final Class<?> clazz, final JavaElementSelectionLocation loc) {
            final Subscription subscription = new Subscription();
            subscription.interestedJavaElementClass = clazz;
            subscription.interestedLocation = loc;
            return subscription;
        }

        public boolean isInterestedIn(final JavaElementSelectionEvent selection) {
            return isInterestedIn(selection.getElement()) && isInterestedIn(selection.getLocation());
        }

        private boolean isInterestedIn(final IJavaElement element) {
            return interestedJavaElementClass.isAssignableFrom(element.getClass());
        }

        private boolean isInterestedIn(final JavaElementSelectionLocation firedLoc) {
            return matchesAllLocations() || matchesFiredLocation(firedLoc);
        }

        private boolean matchesFiredLocation(final JavaElementSelectionLocation firedLoc) {
            return interestedLocation.equals(firedLoc);
        }

        private boolean matchesAllLocations() {
            return interestedLocation == null;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }

}
