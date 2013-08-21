package org.eclipse.recommenders.examples.calls;

import java.io.File;
import java.util.List;

import org.eclipse.recommenders.calls.PoolingCallModelProvider;
import org.eclipse.recommenders.examples.calls.EclipseOrgCallRecommender.ObjectUsage;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ModelIndex;
import org.eclipse.recommenders.models.ModelRepository;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Zips;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class EclipseOrgCallRecommenderMain {

    static String remote = "http://download.eclipse.org/recommenders/models/kepler/";
    static File local = com.google.common.io.Files.createTempDir();

    public static void main(String[] args) throws Exception {

        // setup:
        System.out.println("Setting up recommender and model repository.");
        ModelRepository repository = new ModelRepository(new File(local, "repository"), remote);
        System.out.println("downloading model index from eclipse...");
        repository.resolve(ModelIndex.INDEX);

        File location = repository.getLocation(ModelIndex.INDEX).orNull();
        File indexdir = new File(local, "index");
        Zips.unzip(location, indexdir);
        ModelIndex index = new ModelIndex(indexdir);
        index.open();

        System.out.println("Creating pooling model provider...");
        PoolingCallModelProvider provider = new PoolingCallModelProvider(repository, index);
        provider.open();

        System.out.println("Creating demo(!) call recommender...");
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

    private static void waitUntilModelWasDownloaded(ModelRepository repository) throws Exception {
        System.out.println("Waiting for model download to finish...");
        // there is a blocking API and a non-blocking API. schedule methods are non-blocking method that return a
        // future.
        // However, for demo purpose, we wait until the download finished using resolve().
        ModelCoordinate model = new ModelCoordinate("jre", "jre", "call", "zip", "1.0.0");
        repository.resolve(model);
        File location = repository.getLocation(model).orNull();
        System.out.println("Model got downloaded to " + location + ".");
    }

    private static ObjectUsage createSampleQuery() {
        ObjectUsage query = ObjectUsage.newObjectUsageWithDefaults();
        query.type = VmTypeName.STRING;
        query.overridesFirst = VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z");
        return query;
    }
}
