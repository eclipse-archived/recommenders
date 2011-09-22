/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.eclipse.recommenders.mining.calls.couch.CouchDbDataAccess;
import org.eclipse.recommenders.mining.calls.couch.ModelSpecsGenerator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

public class generate_model_specifications {

    private static class Parameters {
        @Option(name = "--in", usage = "--in http://localhost:5984/udc/ for example")
        private final String in = "http://localhost:5984/udc/";
    }

    private static Parameters params = new Parameters();

    public static void main(final String[] rawArgs) throws CmdLineException {
        parseArguments(rawArgs);

        final ClientConfiguration conf = new ClientConfiguration();
        conf.setBaseUrl(params.in);
        final ModelSpecsGenerator g = new ModelSpecsGenerator(new CouchDbDataAccess(new WebServiceClient(conf)));
        g.execute();
    }

    private static void parseArguments(final String[] rawArgs) throws CmdLineException {
        final CmdLineParser parser = new CmdLineParser(params);
        parser.setUsageWidth(80);
        try {
            parser.parseArgument(rawArgs);
        } catch (final CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println();
            System.err.printf("run it using: java -jar %s %s\n", generate_model_specifications.class,
                    parser.printExample(ExampleMode.ALL));
            System.exit(-1);
        }
    }
}
