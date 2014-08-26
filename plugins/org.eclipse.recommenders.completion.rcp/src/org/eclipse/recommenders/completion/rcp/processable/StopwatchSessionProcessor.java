/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;

import com.google.common.base.Stopwatch;

public class StopwatchSessionProcessor extends SessionProcessor {

    private Stopwatch watch = Stopwatch.createUnstarted();
    private SessionProcessor delegate;

    public StopwatchSessionProcessor(SessionProcessor delegate) {
        this.delegate = delegate;
    }

    public SessionProcessor getDelegate() {
        return delegate;
    }

    @Override
    public boolean startSession(IRecommendersCompletionContext context) {
        watch.reset();
        watch.start();
        try {
            return delegate.startSession(context);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        try {
            watch.start();
            delegate.process(proposal);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void endSession(List<ICompletionProposal> proposals) {
        try {
            watch.start();
            delegate.endSession(proposals);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void aboutToShow(List<ICompletionProposal> proposals) {
        try {
            watch.start();
            delegate.aboutToShow(proposals);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void selected(ICompletionProposal proposal) {
        try {
            watch.start();
            delegate.selected(proposal);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void applied(ICompletionProposal proposal) {
        try {
            watch.start();
            delegate.applied(proposal);
        } finally {
            watch.stop();
        }
    }

    @Override
    public void aboutToClose() {
        try {
            watch.start();
            delegate.aboutToClose();
        } finally {
            watch.stop();
        }
    }

    public long elapsed() {
        return elapsed(TimeUnit.MILLISECONDS);
    }

    public long elapsed(TimeUnit timeUnit) {
        return watch.elapsed(timeUnit);
    }

}
