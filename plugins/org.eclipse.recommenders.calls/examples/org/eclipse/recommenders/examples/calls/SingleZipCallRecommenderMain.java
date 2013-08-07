package org.eclipse.recommenders.examples.calls;

import java.io.File;

import org.eclipse.recommenders.examples.calls.SingleZipCallRecommender.ObjectUsage;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class SingleZipCallRecommenderMain {
    // Download the model archive from
    // http://download.eclipse.org/recommenders/models/juno/jre/jre/1.0.0/jre-1.0.0-call.zip
    // and put it directly into the project folder.
    private static final File MODELS = new File("jre-1.0.0-call.zip");

    public static void main(String[] args) throws Exception {
        SingleZipCallRecommender r = new SingleZipCallRecommender(MODELS);
        ObjectUsage query = createSampleQuery();

        for (Recommendation<IMethodName> rec : r.computeRecommendations(query)) {
            System.out.println(rec);
        }
    }

    private static ObjectUsage createSampleQuery() {
        ObjectUsage query = ObjectUsage.newObjectUsageWithDefaults();
        query.type = VmTypeName.STRING;
        query.overrideFirst = VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z");
        return query;
    }
}
