package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION_EXTENDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import helper.JavaSelectionListenerSpy;
import helper.SpyImplementation;

import java.security.InvalidParameterException;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionDispatcher;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation;
import org.junit.Before;
import org.junit.Test;

public class JavaSelectionDispatcherTest {

    public JavaSelectionDispatcher sut;
    private JavaSelectionListenerSpy listener;
    private JavaSelection wanted;
    private JavaSelection wanted2;
    private JavaSelection unwanted;
    private JavaSelection unwanted2;
    private JavaSelection unwanted3;
    private JavaSelection unwanted4;

    @Before
    public void setup() {
        sut = new JavaSelectionDispatcher();
    }

    @Test
    public void listenerCanRegisterForallElementsAndAllLocations() {

        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        wanted2 = mockJavaSelection(IMethod.class, METHOD_DECLARATION);
        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void allElementsAndAllLocations(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);
        sut.fire(wanted2);

        listener.verifyContains(wanted);
        listener.verifyContains(wanted2);
    }

    @Test
    public void listenerCanRegisterForAllElementsAndSpecificLocation() {

        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        unwanted = mockJavaSelection(IType.class, TYPE_DECLARATION_EXTENDS);

        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(TYPE_DECLARATION)
            public void allElementsAndSpecificLocation(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);
        sut.fire(unwanted);

        listener.verifyContains(wanted);
        listener.verifyNotContains(unwanted);
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndAllLocations() {

        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        unwanted = mockJavaSelection(IMethod.class, METHOD_DECLARATION);

        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void specificElementsAndAllLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);
        sut.fire(unwanted);

        listener.verifyContains(wanted);
        listener.verifyNotContains(unwanted);
    }

    @Test
    public void listenerCanRegisterForSpecificElementsAndSpecificLocations() {

        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        unwanted = mockJavaSelection(IType.class, METHOD_DECLARATION);
        unwanted2 = mockJavaSelection(IMethod.class, TYPE_DECLARATION);
        unwanted3 = mockJavaSelection(IMethod.class, METHOD_DECLARATION);

        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(TYPE_DECLARATION)
            public void specificElementsAndSpecificLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);
        sut.fire(unwanted);
        sut.fire(unwanted2);
        sut.fire(unwanted3);

        listener.verifyContains(wanted);
        listener.verifyNotContains(unwanted);
        listener.verifyNotContains(unwanted2);
        listener.verifyNotContains(unwanted3);
    }

    @Test
    public void listenerCanRegisterForSpecificElementAndMultipleLocations() {

        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        wanted2 = mockJavaSelection(IType.class, TYPE_DECLARATION_EXTENDS);

        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener({ TYPE_DECLARATION, TYPE_DECLARATION_EXTENDS })
            public void allElementsAndSpecificLocation(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);
        sut.fire(wanted2);

        listener.verifyContains(wanted);
        listener.verifyContains(wanted2);
    }

    @Test
    public void listenersCanRegisterMoreThanOnceForRelatedClasses() {
        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener(TYPE_DECLARATION)
            public void specificElementsAndSpecificLocations(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }

            @JavaSelectionListener(TYPE_DECLARATION)
            public void specificElementsAndSpecificLocations(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }
        };

        sut.register(listener);
        sut.fire(wanted);

        listener.verifyContains(wanted, 2);
    }

    @Test
    public void listenersCanBeRegisteredInSubclasses() {
        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        listener = new SpyImplementation() {
            @JavaSelectionListener
            public void anotherListener(IType type, JavaSelection s) {
                recordEvent(s);
            }
        };
        sut.register(listener);
        sut.fire(wanted);

        listener.verifyContains(wanted, 2);
    }

    @Test
    public void listenersCanBeUnregistered() {
        wanted = mockJavaSelection(IType.class, TYPE_DECLARATION);
        listener = new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void anotherListener(IType type, JavaSelection s) {
                recordEvent(s);
            }
        };
        sut.register(listener);
        sut.unregister(listener);
        sut.fire(wanted);

        listener.verifyNotContains(wanted);
    }

    @Test(expected = InvalidParameterException.class)
    public void registeringListenersWithoutAnnotationsThrowsException() {
        sut.register(new JavaSelectionListenerSpy());
    }

    @Test(expected = InvalidParameterException.class)
    public void registeringListenersWithIncompleteParametersThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m() {
            }
        });
    }

    @Test(expected = InvalidParameterException.class)
    public void registeringListenersWithoutIJavaElementThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m(String s, JavaSelection selection) {
            }
        });
    }

    @Test(expected = InvalidParameterException.class)
    public void registeringListenersWithoutJavaSelectionThrowsException() {
        sut.register(new JavaSelectionListenerSpy() {
            @JavaSelectionListener
            public void m(IJavaElement element, String s) {
            }
        });
    }

    public static JavaSelection mockJavaSelection(Class<? extends IJavaElement> clazz, JavaSelectionLocation location) {
        IJavaElement element = mock(clazz);
        JavaSelection selection = mock(JavaSelection.class);
        when(selection.getElement()).thenReturn(element);
        when(selection.getLocation()).thenReturn(location);
        return selection;
    }
}
