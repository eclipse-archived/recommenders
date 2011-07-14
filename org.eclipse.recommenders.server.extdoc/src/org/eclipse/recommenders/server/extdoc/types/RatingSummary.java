package org.eclipse.recommenders.server.extdoc.types;

import org.eclipse.recommenders.rcp.extdoc.features.IRating;
import org.eclipse.recommenders.rcp.extdoc.features.IRatingSummary;

public final class RatingSummary implements IRatingSummary {

    private int sum;
    private int count;
    private IRating userRating;

    public static IRatingSummary create(final int sum, final int count) {
        final RatingSummary summary = new RatingSummary();
        summary.sum = sum;
        summary.count = count;
        return summary;
    }

    @Override
    public int getAverage() {
        return count == 0 ? 0 : sum / count;
    }

    @Override
    public void addUserRating(final IRating userRating) {
        setUserRating(userRating);
        ++count;
        sum += userRating.getRating();
    }

    @Override
    public IRating getUserRating() {
        return userRating;
    }

    public void setUserRating(final IRating userRating) {
        this.userRating = userRating;
    }

}