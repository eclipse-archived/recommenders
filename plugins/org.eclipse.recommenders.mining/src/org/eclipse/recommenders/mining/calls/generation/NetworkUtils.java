/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.calls.generation;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind.PARAMETER;
import static org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind.UNKNOWN;
import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.DUMMY_METHOD;
import static org.eclipse.recommenders.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.utils.Checks.ensureIsGreaterOrEqualTo;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;
import static org.eclipse.recommenders.utils.Throws.throwIllegalStateException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.util.MathUtils;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;

public class NetworkUtils {

	private static final double MAX_PROBABILTY_DELTA = 0.1;
	public static final int P_ROUNDING_PRECISION = 5;
	public static final double P_MIN = 0.00001;
	public static final double P_MAX = 1.0 - P_MIN;
	public static final String NODE_ID_CALL_GROUPS = "call groups";
	public static final String NODE_ID_CALLING_CONTEXT = "calling context";

	public static final String STATE_TRUE = "True";
	public static final String STATE_FALSE = "False";

	public static String STATE_DUMMY_CTX = DUMMY_METHOD.getIdentifier();

	public static String STATE_DUMMY_DEF = VmMethodName.get("L_dummy.dummy()V").getIdentifier();
	public static String STATE_DUMMY_GRP = "pattern dummy";

	public static String createDefinitionState(final DefinitionSite def) {
		switch (def.kind) {
		case FIELD:
			return createFieldDefinitionState();
		case THIS:
			return createThisDefinitionState();
		case METHOD_RETURN:
			return createReturnDefinitionState(def.definedByMethod);
		case NEW:
			return createNewDefinitionState(def.definedByMethod);
		case PARAMETER:
			return createParamDefinitionState(def.definedByMethod);
		case UNKNOWN:
			return createUnknownDefinitionState(def.definedByMethod);
		default:
			throw throwIllegalStateException("unknown definition site");
		}
	}

	public static String createFieldDefinitionState() {
		return "field#<>";
	}

	private static String createThisDefinitionState() {
		return "this#<>";
	}

	public static String createReturnDefinitionState(final IMethodName definedByMethod) {
		return format("return#%s", definedByMethod.getIdentifier());
	}

	public static String createNewDefinitionState(final IMethodName definedByMethod) {
		return format("new#%s", definedByMethod.getIdentifier());
	}

	public static String createParamDefinitionState(final IMethodName definedByMethod) {
		return format("param#%s", definedByMethod);
	}

	private static String createUnknownDefinitionState(final IMethodName definedByMethod) {
		return "unknown#<>";
	}

	/**
	 * Computes the sum of all values, determines the delta to "1.0" and corrects this by adding or removing the delta
	 * to the any of the fields (starting from the <b>last</b> index).
	 * 
	 * @throws RuntimeException
	 *             if delta passes a max threshold
	 */
	public static void scaleMaximalValue(final double[] values) {
		ensureAllProbabilitiesInValidRange(values);
		final double sum = StatUtils.sum(values);
		final double delta = 1.0 - sum;

		if (isDeltaEqualToZero(delta)) {
			return;
		}
		if (isDeltaTooHigh(delta)) {
			throwIllegalArgumentException("sum of values is too far away from '1.0': %6.6f", sum);
		}
		for (int i = values.length; i-- > 0;) {
			double d = values[i];
			d += delta;
			d = round(d, 5);
			if (isInMinMaxRange(d)) {
				values[i] = d;
				// sanity check: did we actually fix the problem?
				ensureSumIsOne(values);
				return;
			}
		}
		throwIllegalArgumentException(
				"failed to scale double array. Delta '%1.5f' couldn't be added to any other value in '%s' AND keeping min/max constraints intact.",
				delta, Arrays.toString(values));
	}

	public static void ensureAllProbabilitiesInValidRange(final double[] values) {
		ensureIsNotNull(values);
		ensureIsGreaterOrEqualTo(values.length, 1, "zero length arrays not allowed.");

		for (int i = values.length; i-- > 0;) {
			if (!isInMinMaxRange(values[i])) {
				throwIllegalArgumentException("index '%d' has invalid value of '%1.6f'.", i, values[i]);
			}
		}
	}

	public static boolean isInMinMaxRange(final double value) {
		if (value < P_MIN) {
			return false;
		}
		if (value > P_MAX) {
			return false;
		}
		return true;
	}

	private static boolean isDeltaEqualToZero(final double delta) {
		return delta == 0d;
	}

	private static boolean isDeltaTooHigh(final double delta) {
		final boolean res = abs(delta) > MAX_PROBABILTY_DELTA;
		return res;
	}

	public static double round(final double value, final int precision) {
		final double res = MathUtils.round(value, precision, BigDecimal.ROUND_HALF_EVEN);
		return res;

	}

	private static void ensureSumIsOne(final double[] values) {
		final double sum = StatUtils.sum(values);
		final double delta = abs(1 - sum);
		if (delta > 0.0001) {
			throwIllegalStateException("failed to round double array properly");
		}
	}

	public static void workaroundRecomputeMissingDefintionSite(final ObjectInstanceKey obj,
			final IMethodName firstDeclarationOfEnclosingMethod) {
		if (obj.definitionSite == null) {
			if (obj.isThis()) {
				obj.definitionSite = DefinitionSite.newSite(Kind.THIS);
			} else if (obj.kind == ObjectInstanceKey.Kind.PARAMETER) {
				obj.definitionSite = DefinitionSite
						.newSite(PARAMETER, null, -1, firstDeclarationOfEnclosingMethod);
			} else {
				final Set<IMethodName> invokedMethods = obj.getInvokedMethods();
				final IMethodName constructorCall = findConstructorCall(invokedMethods);
				if (constructorCall != null) {
					obj.definitionSite = DefinitionSite.newSite(Kind.NEW, null, -1, constructorCall);
				}
			}

			if (obj.definitionSite == null) {
				obj.definitionSite = DefinitionSite.newSite(UNKNOWN);
			}
		}
	}

	private static IMethodName findConstructorCall(final Set<IMethodName> invokedMethods) {
		for (final IMethodName m : invokedMethods) {
			if (m.isInit()) {
				return m;
			}
		}
		return null;
	}

	public static void ensureDummyContextAtIndex0(final IMethodName[] callingContexts) {
		ensureEquals(DUMMY_METHOD, callingContexts[0], "dummy ctx is not first ctx");
	}

	public static void ensureCorrectNumberOfProbabilities(final Node node) {
		int numberOfProbabilities = node.getStates().length;
		for (final Node parent : node.getParents()) {
			numberOfProbabilities *= parent.getStates().length;
		}
		ensureEquals(numberOfProbabilities, node.getProbabilities().length, "incomplete probability definition");
	}

	public static void ensureMinimumTwoStates(final Node node) {
		ensureMinimumTwoStates(node.getStates());
	}

	public static void ensureMinimumTwoStates(final String[] states) {
		ensureIsGreaterOrEqualTo(states.length, 2, "less than 2 states: %s", Arrays.toString(states));
	}

	public static double[] createPriorProbabilitiesForContextNodeAssumingDummyStateAtFirstIndex(final int length) {
		final double[] res = new double[length];
		Arrays.fill(res, 1, res.length, P_MIN);
		final double sum = StatUtils.sum(res);
		res[0] = 1 - sum;
		return res;
	}

	/**
	 * Returns a/b. Returns P_MIN if b==0;
	 */
	public static double safeDivMaxMin(final int a, final int b) {
		// TODO does this make sense? Wouldn't P_MAX be "more" correct?
		if (b == 0) {
			return P_MIN;
		}

		final double res = a / (double) b;
		if (res > P_MAX) {
			return P_MAX;
		}
		if (res < P_MIN) {
			return P_MIN;
		}
		return res;
	}

	public static double getProbabilityInMinMaxRange(double probability) {
		if (probability > P_MAX) {
			return P_MAX;
		}
		if (probability < P_MIN) {
			return P_MIN;
		}
		return probability;
	}
}
