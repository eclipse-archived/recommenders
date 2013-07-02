package org.eclipse.recommenders.utils;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.utils.Recommendation.newRecommendation;
import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

@SuppressWarnings("unchecked")
public class RecommendationsTest {

    Recommendation<String> r1 = newRecommendation("abc", 0.2);
    Recommendation<String> r2 = newRecommendation("cba", 0.1);
    Recommendation<String> r3 = newRecommendation("cba", 0.3);

    @Test
    public void testSortByRelevance() {
        List<Recommendation<String>> input = newArrayList(r2, r1);
        List<Recommendation<String>> expected = newArrayList(r1, r2);
        List<Recommendation<String>> actual = Recommendations.top(input, 100);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSortByRelevance2() {
        List<Recommendation<String>> input = newArrayList(r1, r3);
        List<Recommendation<String>> expected = newArrayList(r3, r1);
        List<Recommendation<String>> actual = Recommendations.top(input, 100);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSortByString() {
        List<Recommendation<String>> input = newArrayList(r2, r1);
        List<Recommendation<String>> expected = newArrayList(r1, r2);
        List<Recommendation<String>> actual = Recommendations.sortByName(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFilterVoid() {
        Recommendation<IMethodName> proc = newRecommendation(VmMethodName.get("LMy.m()V"), 0.2);
        Recommendation<IMethodName> func = newRecommendation(VmMethodName.get("LMy.m()I"), 0.1);
        Recommendation<IMethodName> ctor = newRecommendation(VmMethodName.get("LMy.<init>()V"), 0.1);
        assertTrue(ctor.getProposal().isInit());

        List<Recommendation<IMethodName>> input = newArrayList(ctor, func, proc);
        Iterable<Recommendation<IMethodName>> actual = Recommendations.filterVoid(input);
        assertTrue(Iterables.contains(actual, func));
        assertFalse(Iterables.contains(actual, ctor));
        assertFalse(Iterables.contains(actual, proc));
    }
}
