/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
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
            	try {
	                for (int i = 0; i < 10; i++) {
	                    doSomething();
	                    progressBar.setSelection(i * 10);
	                }
            	}
            	catch(Exception e) {
            		throw new RuntimeException(e);
            	}
            }

        });
        thread.start();
    }

    protected static void doSomething() {
        //
    }
}