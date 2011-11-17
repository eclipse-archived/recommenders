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
package org.eclipse.recommenders.extdoc.rcp.selection2;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;
import org.eclipse.recommenders.utils.Tuple;

public class JavaSelectionDispatcher {

    private Map<Tuple<Class<? extends IJavaElement>, JavaSelectionLocation>, Set<Tuple<Object, Method>>> subscriptions = newHashMap();

    private Tuple<Object, Method> subscriber;
    private Tuple<Class<? extends IJavaElement>, JavaSelectionLocation> subscription;

    private Set<Tuple<Method, JavaSelectionListener>> annotatedMethods;

    public void register(final Object listener) {
        annotatedMethods = findAnnotatedMethods(listener.getClass(), listener);
        if (annotatedMethods.isEmpty()) {
            throw new InvalidParameterException("no listeners found");
        }

        for (Tuple<Method, JavaSelectionListener> t : annotatedMethods) {
            subscribe(t.getFirst(), t.getSecond(), listener);
        }
    }

    public void unregister(final Object listener) {
        for (Set<Tuple<Object, Method>> subscribers : subscriptions.values()) {
            Set<Tuple<Object, Method>> deletions = newHashSet();
            for (Tuple<Object, Method> subscriber : subscribers) {
                if (subscriber.getFirst().equals(listener)) {
                    deletions.add(subscriber);
                }
            }
            subscribers.removeAll(deletions);
        }
    }

    private Set<Tuple<Method, JavaSelectionListener>> findAnnotatedMethods(Class<?> clazz, Object listener) {

        Set<Tuple<Method, JavaSelectionListener>> methods = newHashSet();

        for (Method m : clazz.getDeclaredMethods()) {
            JavaSelectionListener annotation = m.getAnnotation(JavaSelection.JavaSelectionListener.class);
            if (annotation != null) {
                ensureCorrectMethodSignature(m);
                methods.add(Tuple.create(m, annotation));
            }
        }

        Class<?> parent = clazz.getSuperclass();
        boolean isNullOrObject = parent == null || Object.class.equals(parent);
        if (!isNullOrObject) {
            methods.addAll(findAnnotatedMethods(parent, listener));
        }

        return methods;
    }

    private static void ensureCorrectMethodSignature(Method m) {
        final Class<?>[] params = m.getParameterTypes();
        if (params.length != 2) {
            String msg = format("two parameters expected: %s, %s", IJavaElement.class.getSimpleName(),
                    JavaSelection.class.getSimpleName());
            throw new InvalidParameterException(msg);
        }

        if (!IJavaElement.class.isAssignableFrom(params[0])) {
            throw new InvalidParameterException("first parameter needs to be " + IJavaElement.class.getName());
        }

        if (!JavaSelection.class.equals(params[1])) {
            throw new InvalidParameterException("second parameter needs to be " + JavaSelection.class.getName());
        }
    }

    private void subscribe(Method m, JavaSelectionListener annotation, Object listener) {

        subscriber = Tuple.create(listener, m);

        JavaSelectionLocation[] locs = annotation.value();
        if (locs.length == 0) {
            subscription = Tuple.create(getElement(m), null);
            subscribe(subscriber, subscription);
        } else {
            for (JavaSelectionLocation loc : locs) {
                subscription = Tuple.create(getElement(m), loc);
                subscribe(subscriber, subscription);
            }
        }
    }

    private void subscribe(Tuple<Object, Method> subscriber,
            Tuple<Class<? extends IJavaElement>, JavaSelectionLocation> subscription) {
        Set<Tuple<Object, Method>> set = subscriptions.get(subscription);
        if (set == null) {
            set = newHashSet();
            subscriptions.put(subscription, set);
        }
        set.add(subscriber);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends IJavaElement> getElement(Method m) {
        return (Class<? extends IJavaElement>) m.getParameterTypes()[0];
    }

    public void fire(final JavaSelection javaSelection) {
        Class<? extends IJavaElement> element = javaSelection.getElement().getClass();

        for (Tuple<Class<? extends IJavaElement>, JavaSelectionLocation> subscription : subscriptions.keySet()) {
            if (subscription.getFirst().isAssignableFrom(element)) {
                invokeIfLocationMatches(subscription, javaSelection);
            }
        }
    }

    private void invokeIfLocationMatches(Tuple<Class<? extends IJavaElement>, JavaSelectionLocation> subscription,
            JavaSelection javaSelection) {
        JavaSelectionLocation firedLoc = javaSelection.getLocation();
        JavaSelectionLocation subscribedLoc = subscription.getSecond();
        if (locationsMatch(firedLoc, subscribedLoc)) {
            for (Tuple<Object, Method> subscriber : subscriptions.get(subscription)) {
                invoke(subscriber, javaSelection);
            }
        }
    }

    private static boolean locationsMatch(JavaSelectionLocation firedLoc, JavaSelectionLocation subscribedLoc) {
        boolean matchesAll = subscribedLoc == null;
        boolean specificMatch = subscribedLoc != null && subscribedLoc.equals(firedLoc);
        return matchesAll || specificMatch;
    }

    private static void invoke(Tuple<Object, Method> subscriber, JavaSelection javaSelection) {
        Object object = subscriber.getFirst();
        Method method = subscriber.getSecond();

        try {
            method.setAccessible(true); // jvm bug 4819108
            method.invoke(object, javaSelection.getElement(), javaSelection);
        } catch (Exception e) {
            // should not happen after all tests above
            throw new RuntimeException(e);
        }
    }
}