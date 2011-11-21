package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import static java.util.Arrays.asList;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION_EXTENDS;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.ANNOTATION_IN_METHOD_DECLARATION;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.METHOD_IN_METHOD_BODY;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.METHOD_IN_METHOD_DECLARATION;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_BODY;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_DECLARATION_PARAMS;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_METHOD_DECLARATION_THROWS;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION_EXTENDS;
import static org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionTestUtils.TYPE_IN_TYPE_DECLARATION_IMPLEMENTS;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionDispatcher;
import org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.JavaSelectionListenerSpy;
import org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper.SpyImplementation;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

@SuppressWarnings("unused")
public class JavaSelectionDispatcherTest {

    public static final List<JavaSelection> POOL = asList(TYPE_IN_TYPE_DECLARATION, TYPE_IN_TYPE_DECLARATION_EXTENDS,
            TYPE_IN_TYPE_DECLARATION_IMPLEMENTS, TYPE_IN_METHOD_BODY, TYPE_IN_METHOD_DECLARATION_PARAMS,
            TYPE_IN_METHOD_DECLARATION_THROWS, ANNOTATION_IN_METHOD_DECLARATION, METHOD_IN_METHOD_DECLARATION,
            METHOD_IN_METHOD_BODY);

    public JavaSelectionDispatcher sut;
    private JavaSelectionListenerSpy spy;

    @Before
    public void setup() {
        sut = new JavaSelectionDispatcher();
    }

    @Test
    public void listenerCanBeRegistered() {
        spy = new SpyImplementation();
        List<JavaSelection> wanted = asList(TYPE_IN_TYPE_DECLARATION);
        registerFireAndVerify(wanted);
    }

    public void listenersCanBeRegisteredAsAnonymousClasses() {
        final boolean[] listenerWasCalled = { false };
        sut.register(new Object() {
            @JavaSelectionListener
            public void m(IJavaElement e, JavaSelection s) {
                listenerWasCalled[0] = true;
            }
        });
        sut.fire(TYPE_IN_METHOD_BODY);
        assertTrue(listenerWasCalled[0]);
    }

    @Test
    public void listenerCanRegisterForAllElementsAndAllLocations() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void allElementsAndAllLocations(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        List<JavaSelection> wanted = POOL;

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenerCanRegisterForAllElementsAndSpecificLocation() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(METHOD_DECLARATION)
            public void allElementsAndSpecificLocation(final IJavaElement e, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        List<JavaSelection> wanted = asList(ANNOTATION_IN_METHOD_DECLARATION, METHOD_IN_METHOD_DECLARATION);

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndAllLocations() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void specificElementsAndAllLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        List<JavaSelection> wanted = asList(TYPE_IN_TYPE_DECLARATION, TYPE_IN_TYPE_DECLARATION_EXTENDS,
                TYPE_IN_TYPE_DECLARATION_IMPLEMENTS, TYPE_IN_METHOD_BODY, TYPE_IN_METHOD_DECLARATION_PARAMS,
                TYPE_IN_METHOD_DECLARATION_THROWS);

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndSpecificLocations() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(TYPE_DECLARATION)
            @Subscribe
            public void specificElementsAndSpecificLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        List<JavaSelection> wanted = asList(TYPE_IN_TYPE_DECLARATION);

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenerCanRegisterForSpecificElementAndMultipleLocations() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener({ TYPE_DECLARATION, TYPE_DECLARATION_EXTENDS })
            public void allElementsAndSpecificLocation(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        List<JavaSelection> wanted = asList(TYPE_IN_TYPE_DECLARATION, TYPE_IN_TYPE_DECLARATION_EXTENDS);

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenersCanRegisterMoreThanOnceForRelatedClasses() {

        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(TYPE_DECLARATION)
            public void specificElementsAndSpecificLocations(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }

            @JavaSelectionListener(TYPE_DECLARATION)
            public void specificElementsAndSpecificLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(spy);
        sut.fire(TYPE_IN_TYPE_DECLARATION);

        spy.verifyContains(TYPE_IN_TYPE_DECLARATION, 2);
    }

    @Test
    public void listenersCanBeRegisteredInSubclasses() {

        // anonymous subclass of an existing listener with subscriptions
        spy = new SpyImplementation() {
            @JavaSelectionListener(METHOD_DECLARATION)
            public void anotherListener(IMethod m, JavaSelection s) {
                recordEvent(s);
            }
        };

        List<JavaSelection> wanted = asList(TYPE_IN_TYPE_DECLARATION, METHOD_IN_METHOD_DECLARATION);

        registerFireAndVerify(wanted);
    }

    @Test
    public void listenersCanBeUnregistered() {
        spy = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void anotherListener(IJavaElement e, JavaSelection s) {
                recordEvent(s);
            }
        };
        sut.register(spy);
        sut.unregister(spy);
        sut.fire(TYPE_IN_TYPE_DECLARATION);

        spy.verifyNotContains(TYPE_IN_TYPE_DECLARATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutAnnotationsThrowsException() {
        sut.register(new JavaSelectionListenerSpy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringPrivateMethodsThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            private void aPrivateMEthod(IJavaElement e, JavaSelection s) {
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithIncompleteParametersThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m() {
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutIJavaElementThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m(String s, JavaSelection selection) {
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void registeringListenersWithoutJavaSelectionThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m(IJavaElement element, String s) {
            }
        });
    }

    private void registerFireAndVerify(List<JavaSelection> wanted) {
        sut.register(spy);

        for (JavaSelection event : POOL) {
            sut.fire(event);
        }

        for (JavaSelection event : POOL) {
            if (wanted.contains(event)) {
                spy.verifyContains(event);
            } else {
                spy.verifyNotContains(event);
            }
        }
    }
}