/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.codesearch;

import java.net.URI;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

public class SnippetSummary {

    public static final SnippetSummary NULL = new SnippetSummary();

    @SerializedName("_id")
    public String id;

    /**
     * <p>
     * The file that contains the sources of this code snippet. This URI may
     * point to the source file itself or to any container like zip files that
     * contain the source. however, note that this URI may not necessarily be an
     * URL.
     * </p>
     * <p>
     * Example schemes: http://, ftp://, ssh://, cvs://
     * </p>
     * 
     * 
     */
    public URI source;

    public SnippetType type;

    /**
     * The class name of the proposed snippet. Since we recommend methods or
     * classes only, this should never be null;
     */
    public ITypeName className;
    /**
     * The method's name that has been recommended. This field is set iff
     * document type is method.
     */
    public IMethodName methodName;

    /**
     * Which classes does this class extend?
     * 
     * <pre>
     * class C extends S { // extends ={S, super(S), ...} \ {java.lang.Object}
     * }
     * </pre>
     */
    public Set<ITypeName> extendedTypes = Sets.newHashSet();

    /**
     * Which interfaces does this class implement? Typically this does not
     * include all super interfaces but may depend on the analyzer used.
     * 
     * <pre>
     * class C implements I1, I2 { // extends ={I1, I2}
     * }
     * </pre>
     */
    public Set<ITypeName> implementedTypes = Sets.newHashSet();

    /**
     * Which field types have been declared in the class or selection?
     * 
     * <pre>
     * class c {
     *     private String s;
     *     private Map m;
     *     // --&gt; field-types={String, Map}
     * }
     * </pre>
     */
    public Set<ITypeName> fieldTypes = Sets.newHashSet();

    /**
     * Which methods have been overridden in the search context?
     * 
     * <pre>
     * class c extends S {
     *     &#064;Overrides
     *     public void m() { // --&gt; overridden-methods = {S.m()}
     *     }
     * }
     * </pre>
     */
    public Set<IMethodName> overriddenMethods = Sets.newHashSet();

    /**
     * Which types have been used inside the class/method/selection as
     * variables?
     * 
     * <pre>
     * class C extends S {
     * 
     *     public void m() {
     *       Button b = ...
     *       Text t = ...
     *       // --&gt; used-types={Button, Text}
     *       // note: 'S' is not in the list. 
     *     }
     * }
     * </pre>
     */
    public Set<ITypeName> usedTypes = Sets.newHashSet();

    /**
     * Which methods have been called in the search context?
     * 
     * <pre>
     * class c {
     *     public void m() {
     *         Button b = new Button(); // --&gt; called-methods = {new Button()}
     *     }
     * }
     * </pre>
     */
    public Set<IMethodName> calledMethods = Sets.newHashSet();

}
