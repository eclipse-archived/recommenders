/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.server.lucene;

import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createCallsTerm;
import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createExtendsTerms;
import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createFieldTerms;
import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createImplementsTerms;
import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createOverrideTerms;
import static org.eclipse.recommenders.internal.codesearch.server.lucene.LuceneQueryUtil.createUsesTerms;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

public class FeatureScorers {

    public static class CallsHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public CallsHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class CallsInverseHitratioFeatureScorer extends GenericInverseTermHitratioFeatureScorer {

        public CallsInverseHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class CallsInverseRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public CallsInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class CallsRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public CallsRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class CallsTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public CallsTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class CallsWhatElseFeatureScorer extends GenericWhatElseFeatureScorer {

        public CallsWhatElseFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createCallsTerm(query.getRequest().query.calledMethods));
        }
    }

    public static class FieldsHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public FieldsHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createFieldTerms(query.getRequest().query.fieldTypes));
        }
    }

    public static class FieldsInverseRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public FieldsInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createFieldTerms(query.getRequest().query.fieldTypes));
        }
    }

    public static class FieldsRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public FieldsRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createFieldTerms(query.getRequest().query.fieldTypes));
        }
    }

    public static class FieldsTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public FieldsTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createFieldTerms(query.getRequest().query.fieldTypes));
        }
    }

    public static class InterfacesHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public InterfacesHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createImplementsTerms(query.getRequest().query.implementedTypes));
        }
    }

    public static class InterfacesInverseRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public InterfacesInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createImplementsTerms(query.getRequest().query.implementedTypes));
        }
    }

    public static class InterfacesRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public InterfacesRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createImplementsTerms(query.getRequest().query.implementedTypes));
        }
    }

    public static class InterfacesTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public InterfacesTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createImplementsTerms(query.getRequest().query.implementedTypes));
        }
    }

    public static class OverridesHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public OverridesHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createOverrideTerms(query.getRequest().query.overriddenMethods));
        }
    }

    public static class OverridesInverseRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public OverridesInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createOverrideTerms(query.getRequest().query.overriddenMethods));
        }
    }

    public static class OverridesRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public OverridesRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createOverrideTerms(query.getRequest().query.overriddenMethods));
        }
    }

    public static class OverridesTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public OverridesTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createOverrideTerms(query.getRequest().query.overriddenMethods));
        }
    }

    public static class SuperclassHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public SuperclassHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createExtendsTerms(query.getRequest().query.extendedTypes));
        }
    }

    public static class SuperclassInverseRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public SuperclassInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createExtendsTerms(query.getRequest().query.extendedTypes));
        }
    }

    public static class SuperclassRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public SuperclassRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createExtendsTerms(query.getRequest().query.extendedTypes));
        }
    }

    public static class SuperclassTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public SuperclassTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createExtendsTerms(query.getRequest().query.extendedTypes));
        }
    }

    public static class UsesHitratioFeatureScorer extends GenericTermHitratioFeatureScorer {

        public UsesHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

    public static class UsesInverseHitratioFeatureScorer extends GenericInverseTermHitratioFeatureScorer {

        public UsesInverseHitratioFeatureScorer(final IndexReader reader, final CodesearchQuery query)
                throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

    public static class UsesInverseRatioFeatureScorer extends GenericInverseTermRatioFeatureScorer {

        public UsesInverseRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

    public static class UsesRatioFeatureScorer extends GenericTermRatioFeatureScorer {

        public UsesRatioFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

    public static class UsesTFIDFFeatureScorer extends GenericTFIDFFeatureScorer {

        public UsesTFIDFFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

    public static class UsesWhatElseFeatureScorer extends GenericWhatElseFeatureScorer {

        public UsesWhatElseFeatureScorer(final IndexReader reader, final CodesearchQuery query) throws IOException {
            super(reader, createUsesTerms(query.getRequest().query.usedTypes));
        }
    }

}
