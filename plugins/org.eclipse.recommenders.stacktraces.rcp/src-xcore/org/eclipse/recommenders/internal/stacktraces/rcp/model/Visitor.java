/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Visitor</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getVisitor()
 * @model
 * @generated
 */
public interface Visitor extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @model reportUnique="false"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel body=''"
     * @generated
     */
    void visit(ErrorReport report);

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @model statusUnique="false"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel body=''"
     * @generated
     */
    void visit(Status status);

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @model throwableUnique="false"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel body=''"
     * @generated
     */
    void visit(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable throwable);

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @model elementUnique="false"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel body=''"
     * @generated
     */
    void visit(StackTraceElement element);

} // Visitor
