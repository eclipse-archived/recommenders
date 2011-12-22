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
package org.eclipse.recommenders.extdoc.rcp.scheduling;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Sets.newHashSet;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;
import static org.eclipse.recommenders.utils.Tuple.newTuple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class SubscriptionManager {

    private Multimap<Subscription, Tuple<Provider, Method>> subscriptions = HashMultimap.create();

    @Inject
    public SubscriptionManager(List<Provider> providers) {
        ensureIsNotNull(providers);
        for (Provider p : providers) {
            register(p);
        }
    }

    private void register(final Provider provider) {

        Set<Tuple<Method, JavaSelectionListener>> annotatedMethods = findAnnotatedMethods(provider);

        if (annotatedMethods.isEmpty()) {
            throwIllegalArgumentException("no listeners found");
        }

        for (Tuple<Method, JavaSelectionListener> t : annotatedMethods) {
            addSubscription(provider, t.getFirst(), t.getSecond());
        }
    }

    private Set<Tuple<Method, JavaSelectionListener>> findAnnotatedMethods(Provider provider) {

        Set<Tuple<Method, JavaSelectionListener>> methods = newHashSet();

        // TODO Review: test that overridden methods are not called twice

        Class<?> clazz = provider.getClass();
        for (Method m : clazz.getMethods()) {
            JavaSelectionListener annotation = m.getAnnotation(JavaSelectionListener.class);
            if (annotation != null) {
                ensureCorrectMethodParameters(m);
                methods.add(newTuple(m, annotation));
            }
        }

        return methods;
    }

    private static void ensureCorrectMethodParameters(Method m) {
        final Class<?>[] params = m.getParameterTypes();
        ensureParameterLengthIsThree(params, m);
        ensureFirstParameterTypeIsJavaElement(params, m);
        ensureSecondParameterTypeIsJavaSelectionEvent(params, m);
        ensureThirdParameterTypeIsComposite(params, m);
    }

    private static void ensureParameterLengthIsThree(final Class<?>[] params, Method m) {
        if (params.length != 3) {
            throwIllegalArgumentException("error in %s: at least 3 parameters expected", m.toGenericString());
        }
    }

    private static void ensureFirstParameterTypeIsJavaElement(final Class<?>[] params, Method m) {
        if (!IJavaElement.class.isAssignableFrom(params[0])) {
            throwIllegalArgumentException("error in %s: first parameter needs to be %s or a subclass",
                    m.toGenericString(), IJavaElement.class.getName());
        }
    }

    private static void ensureSecondParameterTypeIsJavaSelectionEvent(final Class<?>[] params, Method m) {
        if (!JavaSelectionEvent.class.isAssignableFrom(params[1])) {
            throwIllegalArgumentException("error in %s: second parameter needs to be %s or a subclass",
                    m.toGenericString(), JavaSelectionEvent.class.getName());
        }
    }

    private static void ensureThirdParameterTypeIsComposite(final Class<?>[] params, Method m) {
        if (!Composite.class.isAssignableFrom(params[2])) {
            throwIllegalArgumentException("error in %s: third parameter needs to be %s or a subclass",
                    m.toGenericString(), Composite.class.getName());
        }
    }

    private void addSubscription(Provider provider, Method method, JavaSelectionListener annotation) {
        JavaSelectionLocation[] locs = annotation.value();
        Class<?> javaElementType = method.getParameterTypes()[0];

        Tuple<Provider, Method> subscriber = newTuple(provider, method);

        if (locs.length == 0) {
            Subscription subscription = Subscription.create(javaElementType, null);
            subscriptions.put(subscription, subscriber);
        } else {
            for (JavaSelectionLocation loc : locs) {
                Subscription subscription = Subscription.create(javaElementType, loc);
                subscriptions.put(subscription, subscriber);
            }
        }
    }

    /**
     * returns the first matching method for a given provider and selection
     * event - or <em>absent</em> if none is found
     */
    public Optional<Method> findFirstSubscribedMethod(Provider provider, JavaSelectionEvent selection) {
        for (Subscription s : subscriptions.keySet()) {

            if (s.isInterestedIn(selection)) {
                for (Tuple<Provider, Method> t : subscriptions.get(s)) {
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
        private JavaSelectionLocation interestedLocation;

        public static Subscription create(Class<?> clazz, JavaSelectionLocation loc) {
            Subscription subscription = new Subscription();
            subscription.interestedJavaElementClass = clazz;
            subscription.interestedLocation = loc;
            return subscription;
        }

        public boolean isInterestedIn(JavaSelectionEvent selection) {
            return isInterestedIn(selection.getElement()) && isInterestedIn(selection.getLocation());
        }

        private boolean isInterestedIn(IJavaElement element) {
            return interestedJavaElementClass.isAssignableFrom(element.getClass());
        }

        private boolean isInterestedIn(JavaSelectionLocation firedLoc) {
            return matchesAllLocations() || matchesFiredLocation(firedLoc);
        }

        private boolean matchesFiredLocation(JavaSelectionLocation firedLoc) {
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
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface JavaSelectionListener {

        JavaSelectionLocation[] value() default {};

    }
}