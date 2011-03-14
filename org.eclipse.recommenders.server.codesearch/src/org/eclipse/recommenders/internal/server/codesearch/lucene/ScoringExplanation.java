package org.eclipse.recommenders.internal.server.codesearch.lucene;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.lucene.search.Explanation;

import com.google.common.collect.Lists;

public class ScoringExplanation extends Explanation {

    private static final long serialVersionUID = 1L;

    public static class FeatureScoreExplanation {
        public static FeatureScoreExplanation create(final String scorerName, final float score, final float weight) {
            final FeatureScoreExplanation res = new FeatureScoreExplanation();
            res.scorerName = scorerName;
            res.score = score;
            res.weight = weight;
            return res;
        }

        public String scorerName;
        public float score;
        public float weight;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    /**
     * The total score achieved by the scored code snippet
     */
    public float score;

    public int luceneDocumentId;

    /**
     * The detailed summary how each sub-scorer contributed to this score;
     */
    public List<FeatureScoreExplanation> scores = Lists.newLinkedList();

    public void addSubScore(final String scorerName, final float score, final float weight) {
        final FeatureScoreExplanation e = FeatureScoreExplanation.create(scorerName, score, weight);
        scores.add(e);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
