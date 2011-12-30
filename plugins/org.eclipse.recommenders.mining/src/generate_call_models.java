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
import org.eclipse.recommenders.mining.calls.Algorithm;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.couch.CouchGuiceModule;
import org.eclipse.recommenders.mining.calls.data.zip.ZipGuiceModule;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class generate_call_models {

	static AlgorithmParameters arguments;

	public static void main(final String[] rawArgs) throws CmdLineException {
		arguments = parseArguments(rawArgs);

		final Module module = determineGuiceConfiguration(arguments);
		final Injector injector = Guice.createInjector(module);
		final Algorithm algorithm = injector.getInstance(Algorithm.class);
		algorithm.run();
	}

	public static AlgorithmParameters parseArguments(final String[] rawArgs) throws CmdLineException {
		AlgorithmParameters arguments = new AlgorithmParameters();
		final CmdLineParser parser = new CmdLineParser(arguments);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(rawArgs);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			System.err.println();
			System.err.printf("run it using: java -jar %s %s\n", generate_call_models.class,
					parser.printExample(ExampleMode.ALL));
			System.exit(-1);
		}
		return arguments;
	}

	private static AbstractModule determineGuiceConfiguration(final AlgorithmParameters arguments) {
		return arguments.startsInWithHttp() ? new CouchGuiceModule(arguments) : new ZipGuiceModule(arguments);
	}
}
