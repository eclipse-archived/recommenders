package org.eclipse.recommenders.examples.calls;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.recommenders.calls.PoolingCallModelProvider;
import org.eclipse.recommenders.examples.calls.EclipseOrgCallRecommender.ObjectUsage;
import org.eclipse.recommenders.models.AetherModelRepository;
import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class EclipseOrgCallRecommenderMain {

    static String remote = "http://download.eclipse.org/recommenders/models/kepler/";
    static File local = com.google.common.io.Files.createTempDir();

    public static void main(String[] args) throws Exception {

        // setup:

        System.out.println("Setting up recommender and model repository (downloads search index the first time).");
        AetherModelRepository repository = new AetherModelRepository(local, remote);
        repository.open();
        PoolingCallModelProvider provider = new PoolingCallModelProvider(repository);
        provider.open();
        EclipseOrgCallRecommender recommender = new EclipseOrgCallRecommender(provider);

        // exercise:
        System.out.println("Query the recommender the first time (no model yet available).");
        ObjectUsage query = createSampleQuery();
        List<Recommendation<IMethodName>> recs = recommender.computeRecommendations(query);
        System.out.println("Try #1: num of recs: " + recs.size());

        waitUntilModelWasDownloaded(repository);
        // on second run the models are there...

        recs = recommender.computeRecommendations(query);
        System.out.println("try #2: num of recs: " + recs.size());
        for (Recommendation<IMethodName> rec : recs) {
            System.out.println("\t" + rec);
        }
    }

    private static void waitUntilModelWasDownloaded(AetherModelRepository repository) throws InterruptedException,
            ExecutionException {
        System.out.println("Waiting for model download to happen in background.");
        File file = repository.schedule(new ModelArchiveCoordinate("jre", "jre", "call", "zip", "1.0.0")).get();
        System.out.println("Model got downloaded to " + file + ".");
    }

    private static ObjectUsage createSampleQuery() {
        ObjectUsage query = ObjectUsage.newObjectUsageWithDefaults();
        query.type = VmTypeName.STRING;
        query.overridesFirst = VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z");
        return query;
    }
}
