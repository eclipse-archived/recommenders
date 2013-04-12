/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.jface.viewers.StyledString.COUNTER_STYLER;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.math.stat.StatUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.sandbox.CompletionEvent.ProposalKind;
import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class StatisticsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final long MAX_TIME_IN_COMPLETION = TimeUnit.MINUTES.toMillis(3);
    private Composite container;
    private List<CompletionEvent> events;
    private StyledText styledText;
    private StyledString styledString;

    public StatisticsPreferencePage() {
        super("Completion Statistics");

    }

    private void setDescription() {
        String date = "the beginning of recording";
        if (events.size() > 0) {
            Date start = new Date(events.get(0).sessionStarted);
            date = format("%tF", start);
        }
        setDescription("Here is a summary of your code completion activity since " + date + ":");
    }

    @Override
    public void init(IWorkbench workbench) {
        loadEvents();
        setDescription();
    }

    @Override
    protected Control createContents(Composite parent) {
        createWidgets(parent);
        appendNumberOfCompletionEvents();
        appendTimeSpent();
        appendNumberOfKeystrokesSaved();
        appendNumberOfCompletionsByCompletionKind();
        appendNumberOfCompletionsByReceiverType();

        insertStyledText();
        return container;
    }

    private void createWidgets(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        styledText = new StyledText(container, SWT.READ_ONLY | SWT.WRAP);
        styledText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        styledString = new StyledString();
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 400;
        styledText.setLayoutData(data);
    }

    private void loadEvents() {
        File log = StatisticsSessionProcessor.getCompletionLogLocation();
        Gson gson = StatisticsSessionProcessor.getCompletionLogSerializer();
        events = Lists.newLinkedList();
        try {
            for (String json : Files.readLines(log, Charsets.UTF_8)) {
                CompletionEvent event = gson.fromJson(json, CompletionEvent.class);
                events.add(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendNumberOfCompletionEvents() {
        int total = 0;
        int selected = 0;
        for (CompletionEvent e : events) {
            total += e.numberOfProposals;
            selected += e.applied != null ? 1 : 0;
        }
        styledString.append("Number of times code completion triggered: ")
                .append(format("%,d", events.size()), COUNTER_STYLER).append("\n");
        int aborted = events.size() - selected;
        styledString.append("Number of proposals offered by code completion: ").append(total + "", COUNTER_STYLER)
                .append("\n");
        styledString.append("Number of aborted completions: ").append(aborted + "", COUNTER_STYLER).append("\n");
        styledString.append("Number of concluded completions: ").append(selected + "", COUNTER_STYLER).append("\n");
    }

    private void appendNumberOfKeystrokesSaved() {
        ArrayDoubleList strokes = new ArrayDoubleList();
        for (CompletionEvent e : events) {
            if (e.applied == null || e.completion == null) {
                continue;
            }
            int prefix = e.prefix == null ? 0 : e.prefix.length();
            int saved = Math.max(0, e.completion.length() - prefix);
            strokes.add(saved);
        }

        double total = StatUtils.sum(strokes.toArray());
        styledString.append("\nTotal number of keystrokes saved by using code completion: ")
                .append(format("%.0f", total), COUNTER_STYLER).append("\n");

        double mean = StatUtils.mean(strokes.toArray());
        styledString.append("Average number of keystrokes saved per completion: ").append(format("%.2f", mean),
                COUNTER_STYLER);
    }

    private void appendTimeSpent() {
        ArrayDoubleList spent = new ArrayDoubleList();

        for (CompletionEvent e : events) {
            long ms = e.sessionEnded - e.sessionStarted;
            if (ms > MAX_TIME_IN_COMPLETION) {
                ms = MAX_TIME_IN_COMPLETION;
            }
            spent.add(ms);
        }
        int total = (int) StatUtils.sum(spent.toArray());
        double mean = StatUtils.mean(spent.toArray());

        styledString
                .append("\nTotal time spent in completion window: ")
                .append(format("%d min, %d sec", MILLISECONDS.toMinutes(total),
                        MILLISECONDS.toSeconds(total) - MINUTES.toSeconds(MILLISECONDS.toMinutes(total))),
                        COUNTER_STYLER).append("\n");
        styledString.append("Average time spent in completion window per event: ")
                .append(format("%,.0f ms", mean), COUNTER_STYLER).append("\n");
    }

    private void appendNumberOfCompletionsByCompletionKind() {

        Bag<ProposalKind> b = TreeBag.newTreeBag();
        for (final ProposalKind kind : ProposalKind.values()) {
            Collection<CompletionEvent> byKind = Collections2.filter(events, new Predicate<CompletionEvent>() {

                @Override
                public boolean apply(CompletionEvent input) {
                    return kind == input.applied;
                }
            });
            if (byKind.size() > 0) {
                b.add(kind, byKind.size());
            }
        }

        styledString.append("\n\nMost frequently selected completion types were:\n");

        for (ProposalKind kind : b.topElements(20)) {
            styledString.append("   - completion on ").append(kind.toString().toLowerCase().replace('_', ' '))
                    .append(": ").append(b.count(kind) + "", COUNTER_STYLER).append("\n");
        }
    }

    private void appendNumberOfCompletionsByReceiverType() {
        styledString.append("\nCode completion was triggered most frequently on variables of these types:")
                .append("\n");
        Bag<ITypeName> b = TreeBag.newTreeBag();
        for (CompletionEvent e : events) {
            if (e.receiverType == null) {
                continue;
            }
            b.add(e.receiverType);
        }

        for (ITypeName type : b.topElements(20)) {
            styledString.append("   - ").append(Names.vm2srcQualifiedType(type)).append(": ")
                    .append(b.count(type) + "", COUNTER_STYLER).append("\n");
        }
    }

    private void insertStyledText() {
        styledText.setText(styledString.toString());
        styledText.setStyleRanges(styledString.getStyleRanges());
    }
}
