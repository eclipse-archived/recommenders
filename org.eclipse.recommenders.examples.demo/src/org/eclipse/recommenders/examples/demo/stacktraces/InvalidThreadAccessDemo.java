package org.eclipse.recommenders.examples.demo.stacktraces;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class InvalidThreadAccessDemo {

    // console open?
    public static void main(final String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setSize(200, 200);
        shell.open();

        doWork(new ProgressBar(shell, SWT.NONE));

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void doWork(final ProgressBar progressBar) {
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    doSomething();
                    progressBar.setSelection(i * 10);
                }
            }

        });
        thread.start();
    }

    protected static void doSomething() {
        //
    }
}