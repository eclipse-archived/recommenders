package org.eclipse.recommenders.internal.completion.rcp;

import org.junit.Test;

public class BaseRecommendersCompletionContextTest {

    @Test
    public void smoketestCreateNamesFromKeys() {
        BaseRecommendersCompletionContext.createTypeNamesFromKeys(new char[][] {
                // primitive:
                "I".toCharArray(),
                //
                "[[I".toCharArray(),
                // normal type:
                "Ljava/lang/String;".toCharArray(),
                //
                "[Ljava/lang/String;".toCharArray(),
                // generics:
                "Lcom/codetrails/analysis/GenericWalaBasedArtifactAnalyzer;:TT".toCharArray(), });
    }
}
