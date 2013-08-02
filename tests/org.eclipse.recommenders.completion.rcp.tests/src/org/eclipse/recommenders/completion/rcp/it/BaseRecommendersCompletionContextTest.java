package org.eclipse.recommenders.completion.rcp.it;

import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.junit.Test;

public class BaseRecommendersCompletionContextTest {

    @Test
    public void smoketestCreateNamesFromKeys() {
        RecommendersCompletionContext.createTypeNamesFromSignatures(new char[][] {
                // primitive:
                "I".toCharArray(),
                //
                "[[I".toCharArray(),
                // normal type:
                "Ljava.lang.String;".toCharArray(),
                //
                "[Ljava.lang.String;".toCharArray(),
                // generics:
                "Lcom.codetrails.analysis.GenericWalaBasedArtifactAnalyzer;:TT".toCharArray(), });
    }
}
