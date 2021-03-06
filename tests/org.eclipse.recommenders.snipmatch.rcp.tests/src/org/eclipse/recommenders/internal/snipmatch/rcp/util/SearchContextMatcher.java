package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.snipmatch.ISearchContext;
import org.eclipse.recommenders.snipmatch.Location;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public final class SearchContextMatcher extends TypeSafeMatcher<ISearchContext> {

    private final String searchTerm;
    private final Location location;
    private final Set<ProjectCoordinate> dependencies;

    private SearchContextMatcher(String searchTerm, Location location, Set<ProjectCoordinate> dependencies) {
        this.searchTerm = requireNonNull(searchTerm);
        this.location = requireNonNull(location);
        this.dependencies = requireNonNull(dependencies);
    }

    public static SearchContextMatcher context(String searchTerm, Location location,
            Set<ProjectCoordinate> dependencies) {
        return new SearchContextMatcher(searchTerm, location, dependencies);
    }

    @Override
    public boolean matchesSafely(ISearchContext context) {
        if (!context.getSearchText().equals(searchTerm)) {
            return false;
        }

        if (context.getLocation() != location) {
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(getDescription(searchTerm, location, dependencies));
    }

    @Override
    public void describeMismatchSafely(ISearchContext context, Description description) {
        description
                .appendText(getDescription(context.getSearchText(), context.getLocation(), context.getDependencies()));
    }

    private String getDescription(String searchTerm, Location location, Set<ProjectCoordinate> dependencies) {
        return format("ISearchContext with searchtext '%s', location '%s', dependencies '%s'", searchTerm,
                location.name(), dependencies);
    }
}
