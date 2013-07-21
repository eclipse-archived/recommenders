package org.eclipse.recommenders.examples.calls;

import java.io.File;

import org.eclipse.recommenders.examples.calls.SingelZipCallRecommender.ObjectUsage;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class SingleZipCallRecommenderMain {
    private static final File MODELS = new File("jre-1.0.0-call.zip");

    public static void main(String[] args) throws Exception {
        SingelZipCallRecommender r = new SingelZipCallRecommender(MODELS);
        ObjectUsage query = createSampleQuery();

        for (Recommendation<IMethodName> rec : r.computeRecommendations(query)) {
            System.out.println(rec);
        }
    }

    private static ObjectUsage createSampleQuery() {
        ObjectUsage query = ObjectUsage.newObjectUsageWithDefaults();
        query.type = VmTypeName.STRING;
        query.overridesFirst = VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z");
        return query;
    }
}
