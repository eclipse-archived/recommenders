package org.eclipse.recommenders.tests.completion.rcp;

import java.util.Comparator;

import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;

public final class ProposalComparator implements Comparator<Tuple<IMethodName, Double>> {
    @Override
    public int compare(Tuple<IMethodName, Double> o1, Tuple<IMethodName, Double> o2) {
        if (o1.getSecond() != o2.getSecond()) {
            double diff = o1.getSecond() - o2.getSecond();
            return diff > 0 ? 1 : -1;
        }
        return o1.getFirst().toString().compareTo(o2.getFirst().toString());
    }
}
