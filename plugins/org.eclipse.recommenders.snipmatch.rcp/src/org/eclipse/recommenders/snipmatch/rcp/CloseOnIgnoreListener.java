/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
*/

package org.eclipse.recommenders.snipmatch.rcp;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * If a group of controls all use one instance of this class as their FocusListener,
 * then the listener's target shell will only close if all of the controls in that
 * group have lost focus for at least 200 ms. The effect is that the target shell will
 * close when it loses focus, but only if it loses focus to a control outside of the
 * group that uses this listener.
 */
public class CloseOnIgnoreListener implements FocusListener {
	
	private class TimedCloseThread extends Thread {

		private boolean canceled;
		
		public void cancel() {
			
			canceled = true;
		}
		
		@Override
		public void run() {
			
			try {
				sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!canceled) {
				
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						
						shell.close();
					}
				});
			}
		}
	}
	
	private Shell shell;
	private TimedCloseThread closeThread;
	
	public CloseOnIgnoreListener(Shell shell) {
		
		this.shell = shell;
	}

	@Override
	public void focusGained(FocusEvent e) {
		
		if (closeThread != null) {
			closeThread.cancel();
			closeThread = null;
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		
		if (closeThread != null) closeThread.cancel();
		closeThread = new TimedCloseThread();
		closeThread.start();
	}
}
