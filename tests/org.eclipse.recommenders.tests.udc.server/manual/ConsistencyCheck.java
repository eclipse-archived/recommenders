import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.eclipse.recommenders.commons.udc.LibraryIdentifier;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.internal.server.udc.CouchDBAccessService;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

public class ConsistencyCheck {

    public static void main(final String... args) throws IOException {

        final WebServiceClient c = new WebServiceClient(ClientConfiguration.create("http://213.133.100.41/udc/"));

        final CouchDBAccessService db = new CouchDBAccessService(
                ClientConfiguration.create("http://213.133.100.41:5984/udc/"));

        for (final LibraryIdentifier libid : db.getLibraryIdentifiers()) {
            final ModelSpecification spec = db.getModelSpecificationByFingerprint(libid.fingerprint);
            if (spec == null) {
                continue;
            }
            final String id = spec.getIdentifier();
            try {
                final String url = WebServiceClient.encode("model/size/" + id);
                final int s = c.createRequestBuilder(url).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .get(Integer.class);
                // System.out.println(spec.getIdentifier() + " worked");
                System.out.printf("\tModel size for %s is %d\n", id, s);
            } catch (final Exception e) {
                // System.out.println(id + "failed.\n Should it be deleted? [y/n]:");
                // final java.io.BufferedReader stdin = new java.io.BufferedReader(
                // new java.io.InputStreamReader(System.in));
                // final String line = stdin.readLine();
                // if (line.equalsIgnoreCase("y")) {
                // db.delete(spec._id, spec._rev);
                System.out.println(id + " deleted");
                // }
            }
        }
        // System.out.println(id + "failed.\n Should it be deleted? [y/n]:");
        // final java.io.BufferedReader stdin = new java.io.BufferedReader(new
        // java.io.InputStreamReader(
        // System.in));
        // final String line = stdin.readLine();
        // if (line.equalsIgnoreCase("y")) {
        // db.delete(spec._id, spec._rev);
        // System.out.println(id + " deleted");
        // }
        // } else {
        // db.delete(spec._id, spec._rev);
        // System.out.println(id + " deleted");
        // }
        // }

        // for (final LibraryIdentifier libid : db.getLibraryIdentifiers()) {
        // final ModelSpecification spec = db.getModelSpecificationByFingerprint(libid.fingerprint);
        // if (spec != null) {
        // final String id = spec.getIdentifier();
        // try {
        //
        // final String url = WebServiceClient.encode("model/size/" + id);
        // final int s = c.createRequestBuilder(url).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
        // .get(Integer.class);
        // // System.out.println(spec.getIdentifier() + " worked");
        // System.out.printf("\tModel size for %s is %d\n", id, s);
        // } catch (final Exception e) {
        // if (id.startsWith("org.eclipse")) {
        // continue;
        // // System.out.println(id + "failed.\n Should it be deleted? [y/n]:");
        // // final java.io.BufferedReader stdin = new java.io.BufferedReader(new
        // // java.io.InputStreamReader(
        // // System.in));
        // // final String line = stdin.readLine();
        // // if (line.equalsIgnoreCase("y")) {
        // // db.delete(spec._id, spec._rev);
        // // System.out.println(id + " deleted");
        // // }
        // } else {
        // db.delete(spec._id, spec._rev);
        // System.out.println(id + " deleted");
        // }
        // }
        // }
        // }
    }
}
