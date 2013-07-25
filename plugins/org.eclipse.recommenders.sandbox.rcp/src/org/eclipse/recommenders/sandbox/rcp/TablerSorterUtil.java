/**
 * Copyright (c) 2013 Timur Achmetow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Timur Achmetow - initial API and implementation
 */
package org.eclipse.recommenders.sandbox.rcp;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.sandbox.rcp.CompletionEvent.ProposalKind;
import org.eclipse.recommenders.sandbox.rcp.StatisticsDialog.ProposalLabelProvider;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class TablerSorterUtil {

    public final void setCompletionTypeSorter(final TableViewer viewer, final TableViewerColumn column) {
        new ColumnViewerSorter(viewer, column) {
            @Override
            protected int doCompare(Viewer viewer, Object object1, Object object2) {
                ProposalKind proposal1 = (ProposalKind) object1;
                ProposalKind proposal2 = (ProposalKind) object2;
                return proposal1.toString().compareToIgnoreCase(proposal2.toString());
            }
        };
    }

    public final void setUsedCompletionSorter(final TableViewer viewer, final TableViewerColumn column,
            final Multimap<ProposalKind, CompletionEvent> multiMap) {
        new ColumnViewerSorter(viewer, column) {
            @Override
            protected int doCompare(Viewer viewer, Object object1, Object object2) {
                int listSize1 = multiMap.get((ProposalKind) object1).size();
                int listSize2 = multiMap.get((ProposalKind) object2).size();
                return new Integer(listSize1).compareTo(listSize2);
            }
        };
    }

    public final void setLastUsedSorter(final TableViewer viewer, final TableViewerColumn column,
            final Multimap<ProposalKind, CompletionEvent> multiMap) {
        new ColumnViewerSorter(viewer, column) {
            @Override
            protected int doCompare(Viewer viewer, Object object1, Object object2) {

                StatisticsDialog statsPage = new StatisticsDialog();
                ProposalLabelProvider proposalProvider = statsPage.new ProposalLabelProvider(multiMap);
                Long session1 = proposalProvider.getLastSessionStartedFor((ProposalKind) object1);
                Long session2 = proposalProvider.getLastSessionStartedFor((ProposalKind) object2);
                return session1.compareTo(session2);
            }
        };
    }

    public final void setTypeSorter(final TableViewer viewer, final TableViewerColumn column) {
        new ColumnViewerSorter(viewer, column) {
            @Override
            protected int doCompare(Viewer viewer, Object e1, Object e2) {
                String type1 = Names.vm2srcQualifiedType((ITypeName) e1);
                String type2 = Names.vm2srcQualifiedType((ITypeName) e2);
                return type1.compareToIgnoreCase(type2);
            }
        };
    }

    public final void setCountSorter(final TableViewer viewer, final TableViewerColumn column,
            final Multiset<ITypeName> b) {
        new ColumnViewerSorter(viewer, column) {
            @Override
            protected int doCompare(Viewer viewer, Object e1, Object e2) {
                Integer count1 = new Integer(b.count(e1));
                Integer count2 = new Integer(b.count(e2));
                return count1.compareTo(count2);
            }
        };
    }
}
