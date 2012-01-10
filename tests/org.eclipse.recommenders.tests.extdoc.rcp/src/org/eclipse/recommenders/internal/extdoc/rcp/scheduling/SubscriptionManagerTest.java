/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.scheduling;

import static java.util.Arrays.asList;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.ANNOTATION_IN_METHOD_DECLARATION;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.METHOD_IN_METHOD_BODY;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.METHOD_IN_METHOD_DECLARATION;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_BODY;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_DECLARATION_PARAMS;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_DECLARATION_THROWS;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION_EXTENDS;
import static org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION_IMPLEMENTS;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.TYPE_DECLARATION;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.TYPE_DECLARATION_EXTENDS;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.ProviderImplementation;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper.SubscriptionVerifier;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@SuppressWarnings("unused")
public class SubscriptionManagerTest {

    public static final List<JavaSelectionEvent> POOL = asList(TYPE_IN_TYPE_DECLARATION,
            TYPE_IN_TYPE_DECLARATION_EXTENDS, TYPE_IN_TYPE_DECLARATION_IMPLEMENTS, TYPE_IN_METHOD_BODY,
            TYPE_IN_METHOD_DECLARATION_PARAMS, TYPE_IN_METHOD_DECLARATION_THROWS, ANNOTATION_IN_METHOD_DECLARATION,
            METHOD_IN_METHOD_DECLARATION, METHOD_IN_METHOD_BODY);

    public List<ExtdocProvider> providers;
    public ExtdocProvider provider;

    public SubscriptionManager sut;
    public SubscriptionVerifier verifier;

    @Before
    public void setup() {
        providers = Lists.newArrayList();
        verifier = new SubscriptionVerifier();
    }

    private void setupAndPerformAllSelections() {
        if (provider != null) {
            providers.add(provider);
        }
        sut = new SubscriptionManager(providers);

        for (JavaSelectionEvent selection : POOL) {
            for (ExtdocProvider provider : providers) {
                Optional<Method> optMethod = sut.findSubscribedMethod(provider, selection);
                if (optMethod.isPresent()) {
                    String methodName = optMethod.get().getName();
                    verifier.addResult(selection, provider, methodName);
                }
            }
        }
    }

    private void assertSubscription(final JavaSelectionEvent selection, final String expectedMethodName) {
        verifier.assertSubscription(selection, provider, expectedMethodName);
    }

    @Test
    public void listenerCanBeRegisteredForAllElementsAndAllLocations() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m1(final IJavaElement e, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        for (JavaSelectionEvent selection : POOL) {
            assertSubscription(selection, "m1");
        }
    }

    @Test
    public void listenerCanRegisterForAllElementsAndSpecificLocation() {

        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber(METHOD_DECLARATION)
            public Status m1(final IJavaElement e, final JavaSelectionEvent selection, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(ANNOTATION_IN_METHOD_DECLARATION, "m1");
        assertSubscription(METHOD_IN_METHOD_DECLARATION, "m1");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndAllLocations() {

        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m1(final IType type, final JavaSelectionEvent selection, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(TYPE_IN_TYPE_DECLARATION, "m1");
        assertSubscription(TYPE_IN_TYPE_DECLARATION_EXTENDS, "m1");
        assertSubscription(TYPE_IN_TYPE_DECLARATION_IMPLEMENTS, "m1");
        assertSubscription(TYPE_IN_METHOD_BODY, "m1");
        assertSubscription(TYPE_IN_METHOD_DECLARATION_PARAMS, "m1");
        assertSubscription(TYPE_IN_METHOD_DECLARATION_THROWS, "m1");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndSpecificLocations() {

        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber(TYPE_DECLARATION)
            public Status m1(final IType type, final JavaSelectionEvent selection, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(TYPE_IN_TYPE_DECLARATION, "m1");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void listenerCanRegisterForSpecificElementAndMultipleLocations() {

        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber({ TYPE_DECLARATION, TYPE_DECLARATION_EXTENDS })
            public Status m1(final IType type, final JavaSelectionEvent selection, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(TYPE_IN_TYPE_DECLARATION, "m1");
        assertSubscription(TYPE_IN_TYPE_DECLARATION_EXTENDS, "m1");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void listenersCanHaveMultipleMethods() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m1(final IMethod e, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }

            @JavaSelectionSubscriber(METHOD_BODY)
            public Status m2(final IType e, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(METHOD_IN_METHOD_DECLARATION, "m1");
        assertSubscription(METHOD_IN_METHOD_BODY, "m1");
        assertSubscription(TYPE_IN_METHOD_BODY, "m2");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void listenersCanBeRegisteredInSubclasses() {

        // anonymous subclass of an existing listener with subscriptions
        provider = new ProviderImplementation() {
            @JavaSelectionSubscriber(METHOD_DECLARATION)
            public Status anotherListener(final IMethod m, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };

        setupAndPerformAllSelections();

        assertSubscription(TYPE_IN_TYPE_DECLARATION, "methodInSuperclass");
        assertSubscription(METHOD_IN_METHOD_DECLARATION, "anotherListener");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void multipleProvidersCanBeUsed() {

        ExtdocProvider p1 = new ExtdocProvider() {
            @JavaSelectionSubscriber(TYPE_DECLARATION)
            public Status m1(final IType t, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };
        providers.add(p1);

        ExtdocProvider p2 = new ExtdocProvider() {
            @JavaSelectionSubscriber(TYPE_DECLARATION)
            public Status m2(final IType t, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };
        providers.add(p2);

        setupAndPerformAllSelections();

        verifier.assertSubscription(TYPE_IN_TYPE_DECLARATION, p1, "m1");
        verifier.assertSubscription(TYPE_IN_TYPE_DECLARATION, p2, "m2");
        verifier.assertNoMoreSubscriptions();
    }

    @Test
    public void overridenMethodsAreNotCalledTwice() {

        // anonymous subclass of an existing listener with subscriptions
        provider = new ProviderImplementation() {
            @Override
            @JavaSelectionSubscriber(TYPE_DECLARATION)
            public Status methodInSuperclass(final IType type, final JavaSelectionEvent selection,
                    final Composite parent) {
                return null;
            }
        };

        // would throw an exception if method would be called a second time
        setupAndPerformAllSelections();

        assertSubscription(TYPE_IN_TYPE_DECLARATION, "methodInSuperclass");
        verifier.assertNoMoreSubscriptions();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutAnnotationsThrowsException() {
        provider = new ExtdocProvider() {
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringPrivateMethodsThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            private Status m1(final IJavaElement e, final JavaSelectionEvent s, final Composite parent) {
                return null;
            }
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutStatusThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public void m() {
            }
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithIncompleteParametersThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m() {
                return null;
            }
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutIJavaElementThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m(final String s, final JavaSelectionEvent selection, final Composite parent) {
                return null;
            }
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutJavaSelectionThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m(final IJavaElement element, final String s, final Composite parent) {
                return null;
            }
        };
        setupAndPerformAllSelections();
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutCompositeThrowsException() {
        provider = new ExtdocProvider() {
            @JavaSelectionSubscriber
            public Status m(final IJavaElement element, final JavaSelectionEvent selection, final String s) {
                return null;
            }
        };
        setupAndPerformAllSelections();
    }
}