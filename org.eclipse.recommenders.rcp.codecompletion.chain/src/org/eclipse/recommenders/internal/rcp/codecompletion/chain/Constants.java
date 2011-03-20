/**
 * Copyright (c) 2010 Gary Fritz, and Andreas Kaluza.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA 
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain;

public class Constants {
  /**
   * This static class contains only the settings, which only affect the
   * internal computation algorithm.
   */
  public static class AlgorithmSettings {
    /**
     * This setting sets the maximal computation time of the algorithm. The
     * maximal plug-in computation time is equal to
     * {@link Constants.ProposalSettings#MAX_PROPOSAL_COMPUTATION_TIME_IN_MS} +
     * {@link Constants.AlgorithmSettings#EXECUTOR_ALIVE_TIME_IN_MS}.
     */
    public static final int EXECUTOR_ALIVE_TIME_IN_MS = 400000;

    /**
     * This setting sets the maximal depth of every chain.
     * 
     */
    public static final int MIN_CHAIN_DEPTH = 2;

    /*
     * This setting sets the maximal depth of every chain.
     */
    public static final int MAX_CHAIN_DEPTH = 4;
  }

  /**
   * This static class contains only the settings, which only affect the
   * proposal generation
   */
  public static class ProposalSettings {
    /**
     * This setting sets the maximal proposal entries in the proposal box
     */
    public final static int MAX_PROPOSAL_COUNT = 20;

    /**
     * This setting sets the maximal proposal computation time. The maximal
     * plug-in computation time is equal to
     * {@link Constants.ProposalSettings#MAX_PROPOSAL_COMPUTATION_TIME_IN_MS} +
     * {@link Constants.AlgorithmSettings#EXECUTOR_ALIVE_TIME_IN_MS}.
     */
    public final static long MAX_PROPOSAL_COMPUTATION_TIME_IN_MS = 1000;

    /**
     * This setting sets the image path for the image, which is displayed in the
     * proposal box.
     */
    public static final String IMAGE_PATH = "/icons/view16/chain.gif";

    /**
     * This setting sets the plug-in ID, which is needed to display the image in
     * the proposal box.
     * 
     * REVIEW: Is there another way to retrieve this id from the manifest.mf?
     */
    public static final String PLUGIN_ID = "org.eclipse.recommenders.rcp.codecompletion.chain";
  }

  public static final boolean DEBUG = false;
}
