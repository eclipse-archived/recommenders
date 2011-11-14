package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionDispatcher;
import org.junit.Test;

public class JavaSelectionDispatcherTest {

    private class EventSpy {
        protected List<JavaSelection> events = newArrayList();

        public List<JavaSelection> get() {
            return events;
        }

        protected void recordEvent(final JavaSelection s) {
            events.add(s);
        }

        public void verifyNumberOfEvents(final int count) {
            // ..
        }
    }

    JavaSelectionDispatcher sut = new JavaSelectionDispatcher();

    @Test
    public void test() {

        sut.register(new EventSpy() {
            @JavaSelectionListener
            void doit(final IType type, final JavaSelection selection) {
                recordEvent(selection);
            }

            @JavaSelectionListener
            void doit(final IJavaElement type, final JavaSelection selection) {
                recordEvent(selection);
            }
        });
    }

}
