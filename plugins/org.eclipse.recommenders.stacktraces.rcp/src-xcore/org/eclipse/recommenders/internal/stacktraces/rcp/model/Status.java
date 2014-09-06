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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Status</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getPluginId <em>Plugin Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getPluginVersion <em>Plugin Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getCode <em>Code</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getFingerprint <em>Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getException <em>Exception</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus()
 * @generated
 */
public interface Status extends EObject {
    /**
     * Returns the value of the '<em><b>Plugin Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Plugin Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Plugin Id</em>' attribute.
     * @see #setPluginId(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_PluginId()
     * @generated
     */
    String getPluginId();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getPluginId <em>Plugin Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Plugin Id</em>' attribute.
     * @see #getPluginId()
     * @generated
     */
    void setPluginId(String value);

    /**
     * Returns the value of the '<em><b>Plugin Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Plugin Version</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Plugin Version</em>' attribute.
     * @see #setPluginVersion(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_PluginVersion()
     * @generated
     */
    String getPluginVersion();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getPluginVersion <em>Plugin Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Plugin Version</em>' attribute.
     * @see #getPluginVersion()
     * @generated
     */
    void setPluginVersion(String value);

    /**
     * Returns the value of the '<em><b>Code</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Code</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Code</em>' attribute.
     * @see #setCode(int)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Code()
     * @generated
     */
    int getCode();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getCode <em>Code</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Code</em>' attribute.
     * @see #getCode()
     * @generated
     */
    void setCode(int value);

    /**
     * Returns the value of the '<em><b>Severity</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Severity</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Severity</em>' attribute.
     * @see #setSeverity(int)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Severity()
     * @generated
     */
    int getSeverity();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getSeverity <em>Severity</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Severity</em>' attribute.
     * @see #getSeverity()
     * @generated
     */
    void setSeverity(int value);

    /**
     * Returns the value of the '<em><b>Message</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Message</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Message</em>' attribute.
     * @see #setMessage(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Message()
     * @generated
     */
    String getMessage();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getMessage <em>Message</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Message</em>' attribute.
     * @see #getMessage()
     * @generated
     */
    void setMessage(String value);

    /**
     * Returns the value of the '<em><b>Fingerprint</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Fingerprint</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Fingerprint</em>' attribute.
     * @see #setFingerprint(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Fingerprint()
     * @generated
     */
    String getFingerprint();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getFingerprint <em>Fingerprint</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Fingerprint</em>' attribute.
     * @see #getFingerprint()
     * @generated
     */
    void setFingerprint(String value);

    /**
     * Returns the value of the '<em><b>Exception</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Exception</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Exception</em>' containment reference.
     * @see #setException(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Exception()
     * @generated
     */
    org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable getException();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status#getException <em>Exception</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Exception</em>' containment reference.
     * @see #getException()
     * @generated
     */
    void setException(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable value);

    /**
     * Returns the value of the '<em><b>Children</b></em>' containment reference list.
     * The list contents are of type {@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Status}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Children</em>' containment reference list.
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getStatus_Children()
     * @generated
     */
    EList<Status> getChildren();

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    void accept(Visitor v);

} // Status
