package org.eclipse.recommenders.extdoc.rcp.selection2;

import javax.inject.Singleton;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public class JavaSelectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Wiring.class).asEagerSingleton();
        bind(JavaSelectionDispatcher.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    public static JavaSelectionListener provideSelectionListener(final IWorkbench wb,
            final JavaSelectionDispatcher dispatcher) {
        final JavaSelectionListener gate = new JavaSelectionListener(dispatcher);
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                final IWorkbenchWindow ww = wb.getActiveWorkbenchWindow();
                final ISelectionService service = (ISelectionService) ww.getService(ISelectionService.class);
                service.addPostSelectionListener(gate);
            }
        });
        return gate;
    }

    @SuppressWarnings("unused")
    private static class Wiring {
        @Inject
        public Wiring(final JavaSelectionListener gate) {
        }
    }
}
