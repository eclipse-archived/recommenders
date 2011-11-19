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

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;
import static org.eclipse.recommenders.utils.Tuple.newTuple;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;
import org.eclipse.recommenders.utils.Tuple;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class JavaSelectionDispatcher {

    private Multimap<Tuple<Class<?>, JavaSelectionLocation>, Tuple<Object, Method>> subscriptions = HashMultimap
            .create();

    private Set<Tuple<Method, JavaSelectionListener>> annotatedMethods;

    /**
     * Registers objects with methods that are annotated as
     * <tt>JavaSelectionListener</tt>. The provided
     * <tt>JavaSelectionLocation</tt> of the annotation and the type of the
     * <tt>IJavaElement</tt> defined in the method signature are used to filter
     * all <tt>JavaSelection</tt> events that are passed to the subscribed
     * methods.
     * <p>
     * Those methods have to follow a fixed signature: <tt>IJavaElement</tt> and
     * <tt>JavaSelection</tt> are the two expected parameters. It is ensured
     * that the provided objects comply to this convention but no further
     * validation is done by the dispatcher.
     * <p>
     * Through this convention it is possible to define overlapping
     * subscriptions or even the same subscription twice. All subscriptions are
     * called that match a fired event. The developer has to be aware that a
     * single event triggers all matching methods in a class.
     * <p>
     * The following code is an example method that is used to subscribe for all
     * <tt>JavaSelectionLocation</tt> and all <tt>IJavaElement</tt>:
     * 
     * <pre>
     * {@code @JavaSelectionListener
     * public void a(IJavaElement e, JavaSelection s) {
     *  ...
     * }
     * </pre>
     * 
     * Subscribes to a specific <tt>JavaSelectionLocation</tt> and only to
     * <tt>IMethod</tt>:
     * 
     * <pre>
     * {@code @JavaSelectionListener(JavaSelectionLocation.METHOD_DECLARATION)
     * public void a(IMethod m, JavaSelection s) {
     *  ...
     * }
     * </pre>
     * 
     * Subscribes to multiple specific <tt>JavaSelectionLocation</tt> and only
     * to <tt>IMethod</tt>:
     * 
     * <pre>
     * {@code @JavaSelectionListener({METHOD_DECLARATION, METHOD_BODY })
     * public void a(IMethod  m,  JavaSelection  s) {
     *  ...
     * }
     * </pre>
     * 
     * To register all these methods simply pass an instance of the class which
     * contains the methods to the dispatcher:
     * 
     * <pre>
     * {@code dispatcher.register(new ClassThatContainsAllAnnotatedMethods());}
     * </pre>
     * 
     * @param listener
     *            an object with annotated methods
     * @see JavaSelectionListener
     * @see JavaSelectionLocation
     * @see IJavaElement
     * @see JavaSelection
     */
    public void register(final Object listener) {
        ensureIsNotNull(listener);

        annotatedMethods = findAnnotatedMethods(listener.getClass(), listener);
        if (annotatedMethods.isEmpty()) {
            throwIllegalArgumentException("no listeners found");
        }

        for (Tuple<Method, JavaSelectionListener> t : annotatedMethods) {
            subscribe(t.getFirst(), t.getSecond(), listener);
        }
    }

    private Set<Tuple<Method, JavaSelectionListener>> findAnnotatedMethods(Class<?> clazz, Object listener) {

        Set<Tuple<Method, JavaSelectionListener>> methods = newHashSet();

        for (Method m : clazz.getDeclaredMethods()) {
            JavaSelectionListener annotation = m.getAnnotation(JavaSelection.JavaSelectionListener.class);
            if (annotation != null) {
                ensureIsPublicMethod(m);
                ensureCorrectMethodSignature(m);
                methods.add(newTuple(m, annotation));
            }
        }

        Class<?> parent = clazz.getSuperclass();
        if (!isNullOrObject(parent)) {
            methods.addAll(findAnnotatedMethods(parent, listener));
        }

        return methods;
    }

    private static boolean isNullOrObject(Class<?> parent) {
        return parent == null || Object.class.equals(parent);
    }

    private static void ensureIsPublicMethod(Method m) {
        ensureIsTrue(isPublic(m.getModifiers()), "cannot register non-public method: %s", m.getName());
    }

    private static void ensureCorrectMethodSignature(Method m) {
        final Class<?>[] params = m.getParameterTypes();
        if (params.length != 2) {
            throwIllegalArgumentException("two parameters expected: %s, %s", IJavaElement.class.getSimpleName(),
                    JavaSelection.class.getSimpleName());
        }

        if (!IJavaElement.class.isAssignableFrom(params[0])) {
            throwIllegalArgumentException("first parameter needs to be %s", IJavaElement.class.getName());
        }

        if (!JavaSelection.class.equals(params[1])) {
            throwIllegalArgumentException("second parameter needs to be %s", JavaSelection.class.getName());
        }
    }

    private void subscribe(Method m, JavaSelectionListener annotation, Object listener) {
        JavaSelectionLocation[] locs = annotation.value();
        Class<?> elementType = m.getParameterTypes()[0];

        Tuple<Object, Method> subscriber = newTuple(listener, m);

        if (locs.length == 0) {
            Tuple<Class<?>, JavaSelectionLocation> subscription = newTuple(elementType, null);
            subscribe(subscriber, subscription);
        } else {
            for (JavaSelectionLocation loc : locs) {
                Tuple<Class<?>, JavaSelectionLocation> subscription = newTuple(elementType, loc);
                subscribe(subscriber, subscription);
            }
        }
    }

    private void subscribe(Tuple<Object, Method> subscriber, Tuple<Class<?>, JavaSelectionLocation> subscription) {
        subscriptions.put(subscription, subscriber);
    }

    /**
     * Unregisters all subscriptions for the provided
     * <tt>JavaSelectionListener</tt>.
     */
    public void unregister(final Object listener) {
        ensureIsNotNull(listener);

        for (Iterator<Tuple<Object, Method>> it = subscriptions.values().iterator(); it.hasNext();) {
            Tuple<?, ?> next = it.next();
            if (next.getFirst().equals(listener)) {
                it.remove();
            }
        }
    }

    /**
     * Dispatches a provided <tt>JavaSelection</tt> and calls all registered
     * <tt>JavaSelectionListener</tt> that match the selection.
     */
    public void fire(final JavaSelection javaSelection) {
        ensureIsNotNull(javaSelection);
        Class<?> element = javaSelection.getElement().getClass();

        for (Tuple<Class<?>, JavaSelectionLocation> subscription : subscriptions.keySet()) {
            if (subscription.getFirst().isAssignableFrom(element)) {
                invokeIfLocationMatches(subscription, javaSelection);
            }
        }
    }

    private void invokeIfLocationMatches(Tuple<Class<?>, JavaSelectionLocation> subscription,
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
        return matchesAllLocations(subscribedLoc) || matchesFiredLocation(firedLoc, subscribedLoc);
    }

    private static boolean matchesFiredLocation(JavaSelectionLocation firedLoc, JavaSelectionLocation subscribedLoc) {
        return subscribedLoc.equals(firedLoc);
    }

    private static boolean matchesAllLocations(JavaSelectionLocation subscribedLoc) {
        return subscribedLoc == null;
    }

    private static void invoke(Tuple<Object, Method> subscriber, JavaSelection javaSelection) {
        final Object object = subscriber.getFirst();
        final Method method = subscriber.getSecond();

        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(object, javaSelection.getElement(), javaSelection);
        } catch (Exception e) {
            // should not happen after all tests above
            throwUnhandledException(e);
        }
    }
}