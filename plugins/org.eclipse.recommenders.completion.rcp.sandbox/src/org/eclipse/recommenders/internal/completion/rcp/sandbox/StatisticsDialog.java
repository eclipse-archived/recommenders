/**
 * Copyright (c) 2013 Timur Achmetow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Timur Achmetow - Initial API and implementation
 */
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import static com.google.common.base.Predicates.not;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.math.stat.StatUtils.mean;
import static org.apache.commons.math.stat.StatUtils.sum;
import static org.eclipse.jface.viewers.StyledString.COUNTER_STYLER;
import static org.eclipse.recommenders.internal.completion.rcp.sandbox.TableSorters.setCompletionTypeSorter;
import static org.eclipse.recommenders.internal.completion.rcp.sandbox.TableSorters.setCountSorter;
import static org.eclipse.recommenders.internal.completion.rcp.sandbox.TableSorters.setLastUsedSorter;
import static org.eclipse.recommenders.internal.completion.rcp.sandbox.TableSorters.setTypeSorter;
import static org.eclipse.recommenders.internal.completion.rcp.sandbox.TableSorters.setUsedCompletionSorter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.recommenders.internal.completion.rcp.sandbox.CompletionEvent.ProposalKind;
import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class StatisticsDialog extends TitleAreaDialog {

    private static final long MAX_TIME_IN_COMPLETION = TimeUnit.MINUTES.toMillis(2);

    private Collection<CompletionEvent> okayEvents;
    private Collection<CompletionEvent> appliedEvents;
    private Collection<CompletionEvent> abortedEvents;

    private Composite container;
    private StyledText styledText;
    private StyledString styledString;

    private final class BuggyEventsPredicate implements Predicate<CompletionEvent> {
        @Override
        public boolean apply(CompletionEvent input) {
            return input.numberOfProposals < 1 || input.sessionEnded < input.sessionStarted;
        }
    }

    private final class HasAppliedProposalPredicate implements Predicate<CompletionEvent> {
        @Override
        public boolean apply(CompletionEvent e) {
            return e.applied != null;
        }
    }

    public StatisticsDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        loadEvents();
    }

    public StatisticsDialog() {
        super(null);
    }

    @Override
    protected Control createContents(Composite parent) {
        super.createContents(parent);
        getShell().setText("Statistics Dialog");
        getShell().setSize(550, 725);
        setMessage(getDescriptionText(), IMessageProvider.INFORMATION);
        return parent;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent.setLayout(new GridLayout());
        createWidgets(parent);
        appendNumberOfCompletionEvents();
        appendNumberOfKeystrokesSaved();
        appendTimeSpent();

        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
        GridData data = new GridData(GridData.FILL_BOTH);
        sashForm.setLayoutData(data);

        showCompletionKindInViewer(sashForm);
        showReceiverTypeInViewer(sashForm);

        sashForm.setWeights(new int[] { 50, 50 });
        insertStyledText();
        return parent;
    }

    private void createWidgets(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        styledText = new StyledText(container, SWT.READ_ONLY | SWT.WRAP);
        styledText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        styledString = new StyledString();
    }

    private void appendNumberOfCompletionEvents() {
        int total = 0;
        for (CompletionEvent e : okayEvents) {
            total += e.numberOfProposals;
        }
        int completedInPercent = calculatePercentData(appliedEvents);
        styledString.append("Number of times code completion triggered: ")
                .append(format(addTabs(3) + "%,d", okayEvents.size()), COUNTER_STYLER).append("\n");
        int abortedInPercent = calculatePercentData(abortedEvents);

        styledString.append("Number of concluded completions: ")
                .append(addTabs(7) + appliedEvents.size() + " (" + completedInPercent + "%)", COUNTER_STYLER)
                .append("\n");
        styledString.append("Number of aborted completions: ")
                .append(addTabs(8) + abortedEvents.size() + " (" + abortedInPercent + "%)", COUNTER_STYLER)
                .append("\n");
        styledString.append("Number of proposals offered by code completion: ")
                .append(addTabs(1) + total + "", COUNTER_STYLER).append("\n");
    }

    private String addTabs(int count) {
        return StringUtils.repeat("\t", count);
    }

    private int calculatePercentData(Collection<CompletionEvent> list) {
        if (okayEvents.size() == 0) {
            return okayEvents.size();
        }
        double division = list.size() / (double) okayEvents.size() * 100;
        return (int) Math.round(division);
    }

    private void appendNumberOfKeystrokesSaved() {
        ArrayDoubleList strokes = new ArrayDoubleList();
        for (CompletionEvent e : appliedEvents) {
            int prefix = e.prefix == null ? 0 : e.prefix.length();
            int completionLength = e.completion == null ? 0 : e.completion.length();
            int saved = Math.max(0, completionLength - prefix);
            strokes.add(saved);
        }

        double total = sum(strokes.toArray());
        styledString.append("\nKeystrokes saved by using code completion");
        styledString.append("\n   - total number: ").append(format(addTabs(3) + "%.0f", total), COUNTER_STYLER);
        double mean = mean(strokes.toArray());
        styledString.append("\n   - average number: ").append(format(addTabs(1) + "%.2f", mean), COUNTER_STYLER);
        styledString.append("\n");
    }

    private void appendTimeSpent() {
        ArrayDoubleList spentApplied = computeTimeSpentInCompletion(appliedEvents);
        long totalApplied = round(sum(spentApplied.toArray()));
        long meanApplied = round(mean(spentApplied.toArray()));

        ArrayDoubleList spentAborted = computeTimeSpentInCompletion(abortedEvents);
        long totalAborted = round(sum(spentAborted.toArray()));
        long meanAborted = round(mean(spentAborted.toArray()));

        styledString
                .append("\nTotal Time spent in completion window on ")
                //
                .append("\n   - concluded sessions:    ")
                .append(addTabs(1) + toTimeString(totalApplied), COUNTER_STYLER)
                //
                .append("\n   - aborted sessions:      ")
                .append(addTabs(2) + toTimeString(totalAborted), COUNTER_STYLER);

        styledString
                .append("\n\nAverage time spent in completion window per")
                //
                .append("\n   - concluded session:    ")
                .append(format(addTabs(1) + "%,d ms", meanApplied), COUNTER_STYLER)
                //
                .append("\n   - aborted session:     ")
                .append(format(addTabs(2) + "%,d ms", meanAborted), COUNTER_STYLER);
    }

    private String toTimeString(long time) {
        return format("%d min, %d sec", MILLISECONDS.toMinutes(time),
                MILLISECONDS.toSeconds(time) - MINUTES.toSeconds(MILLISECONDS.toMinutes(time)));
    }

    private ArrayDoubleList computeTimeSpentInCompletion(Collection<CompletionEvent> events) {
        ArrayDoubleList spent = new ArrayDoubleList();
        for (CompletionEvent e : events) {
            long ms = e.sessionEnded - e.sessionStarted;
            if (ms > MAX_TIME_IN_COMPLETION) {
                ms = MAX_TIME_IN_COMPLETION;
            }
            spent.add(ms);
        }
        return spent;
    }

    private void showCompletionKindInViewer(Composite parent) {
        Bag<ProposalKind> proposalKindBag = TreeBag.newTreeBag();
        final Multimap<ProposalKind, CompletionEvent> multiMap = ArrayListMultimap.create();

        for (final ProposalKind kind : ProposalKind.values()) {
            Collection<CompletionEvent> byKind = Collections2.filter(okayEvents, new Predicate<CompletionEvent>() {
                @Override
                public boolean apply(CompletionEvent input) {
                    if (kind == input.applied) {
                        if (!multiMap.containsEntry(kind, input)) {
                            multiMap.put(kind, input);
                        }
                        return true;
                    }
                    return false;
                }
            });
            if (byKind.size() > 0) {
                proposalKindBag.add(kind, byKind.size());
            }
        }

        final Composite newComp = createWrapperComposite(parent);
        new Label(newComp, SWT.NONE).setText("Most frequently selected completion types were:");
        final Composite comp = new Composite(newComp, SWT.NONE);
        final TableColumnLayout layout = createTableColumnLayout(comp);

        final TableViewer viewer = createTableViewer(comp);
        TableViewerColumn completionTypeColumn = createColumn("Completion Type", viewer, 150, layout, 50);
        setCompletionTypeSorter(viewer, completionTypeColumn);
        TableViewerColumn usedCompletionColumn = createColumn("Used", viewer, 60, layout, 15);
        setUsedCompletionSorter(viewer, usedCompletionColumn, multiMap);
        TableViewerColumn lastUsedColumn = createColumn("Last used", viewer, 110, layout, 35);
        setLastUsedSorter(viewer, lastUsedColumn, multiMap);
        usedCompletionColumn.getColumn().getParent().setSortColumn(usedCompletionColumn.getColumn());
        usedCompletionColumn.getColumn().getParent().setSortDirection(SWT.DOWN);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new ProposalLabelProvider(multiMap));
        viewer.setInput(proposalKindBag.topElements(30));
    }

    private void showReceiverTypeInViewer(Composite parent) {
        final Bag<ITypeName> b = TreeBag.newTreeBag();
        for (CompletionEvent e : okayEvents) {
            if (e.receiverType == null) {
                continue;
            }
            b.add(e.receiverType);
        }

        final Composite newComp = createWrapperComposite(parent);
        new Label(newComp, SWT.NONE)
                .setText("Code completion was triggered most frequently on variables of these types:");

        final Composite comp = new Composite(newComp, SWT.NONE);
        final TableColumnLayout layout = createTableColumnLayout(comp);

        final TableViewer viewer = createTableViewer(comp);
        TableViewerColumn typeColumn = createColumn("Type", viewer, 450, layout, 60);
        setTypeSorter(viewer, typeColumn);
        TableViewerColumn countColumn = createColumn("Count", viewer, 100, layout, 30);
        setCountSorter(viewer, countColumn, b);
        countColumn.getColumn().getParent().setSortColumn(countColumn.getColumn());
        countColumn.getColumn().getParent().setSortDirection(SWT.DOWN);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TypeNameLabelProvider(b));
        viewer.setInput(b.topElements(30));
    }

    private Composite createWrapperComposite(Composite parent) {
        final Composite newComp = new Composite(parent, SWT.NONE);
        newComp.setLayout(new GridLayout());
        newComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        return newComp;
    }

    private void insertStyledText() {
        styledText.setText(styledString.toString());
        styledText.setStyleRanges(styledString.getStyleRanges());
    }

    private TableColumnLayout createTableColumnLayout(final Composite comp) {
        final TableColumnLayout layout = new TableColumnLayout();
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(layout);
        return layout;
    }

    private TableViewer createTableViewer(Composite parent) {
        final TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
                | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        return viewer;
    }

    private TableViewerColumn createColumn(String header, TableViewer viewer, int width, TableColumnLayout layout,
            int weight) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(header);
        column.getColumn().setToolTipText(header);
        column.getColumn().setMoveable(true);
        column.getColumn().setAlignment(SWT.CENTER);
        column.getColumn().setResizable(true);
        column.getColumn().setWidth(width);
        layout.setColumnData(column.getColumn(), new ColumnWeightData(weight));
        return column;
    }

    @Override
    protected final void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private String getDescriptionText() {
        String date = "the beginning of recording";
        if (okayEvents.size() > 0) {
            Date start = new Date(Iterables.getFirst(okayEvents, null).sessionStarted);
            date = format("%tF", start);
        }
        return "Here is a summary of your code completion activity since " + date;
    }

    private void loadEvents() {
        File log = StatisticsSessionProcessor.getCompletionLogLocation();
        Gson gson = StatisticsSessionProcessor.getCompletionLogSerializer();
        LinkedList<CompletionEvent> events = Lists.newLinkedList();
        try {
            for (String json : Files.readLines(log, Charsets.UTF_8)) {
                CompletionEvent event = gson.fromJson(json, CompletionEvent.class);
                events.add(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        okayEvents = Collections2.filter(events, not(new BuggyEventsPredicate()));
        appliedEvents = Collections2.filter(okayEvents, new HasAppliedProposalPredicate());
        abortedEvents = Collections2.filter(okayEvents, not(new HasAppliedProposalPredicate()));
    }

    public class TypeNameLabelProvider extends CellLabelProvider {
        private final Bag<ITypeName> bag;

        public TypeNameLabelProvider(Bag<ITypeName> b) {
            super();
            this.bag = b;
        }

        @Override
        public void update(ViewerCell cell) {
            String cellText = null;
            ITypeName type = (ITypeName) cell.getElement();

            switch (cell.getColumnIndex()) {
            case 0:
                cellText = Names.vm2srcQualifiedType(type);
                break;
            case 1:
                cellText = Integer.toString(bag.count(type));
                break;
            }

            if (cellText != null) {
                cell.setText(cellText);
            }
        }
    }

    public class ProposalLabelProvider extends CellLabelProvider {
        private final Multimap<ProposalKind, CompletionEvent> multiMap;

        public ProposalLabelProvider(Multimap<ProposalKind, CompletionEvent> multiMap) {
            super();
            this.multiMap = multiMap;
        }

        @Override
        public void update(ViewerCell cell) {
            String cellText = null;
            ProposalKind proposal = (ProposalKind) cell.getElement();

            switch (cell.getColumnIndex()) {
            case 0:
                cellText = proposal.toString().toLowerCase().replace('_', ' ');
                break;
            case 1:
                cellText = Integer.toString(multiMap.get(proposal).size());
                break;
            case 2:
                Date past = new Date(getLastSessionStartedFor(proposal));
                cellText = new DateFormatter().formatUnit(past, new Date());
                break;
            }

            if (cellText != null) {
                cell.setText(cellText);
            }
        }

        public Long getLastSessionStartedFor(ProposalKind proposal) {
            Collection<CompletionEvent> collection = multiMap.get(proposal);
            TreeSet<Long> sessionSet = new TreeSet<Long>();
            for (CompletionEvent completionEvent : collection) {
                sessionSet.add(completionEvent.sessionEnded);
            }
            return sessionSet.last();
        }
    }
}
