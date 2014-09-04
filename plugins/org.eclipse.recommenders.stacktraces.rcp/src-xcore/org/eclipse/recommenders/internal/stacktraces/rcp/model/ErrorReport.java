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

import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Error Report</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getAnonymousId <em>Anonymous Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEventId <em>Event Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEmail <em>Email</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getComment <em>Comment</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEclipseBuildId <em>Eclipse Build Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEclipseProduct <em>Eclipse Product</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getJavaRuntimeVersion <em>Java Runtime Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiWs <em>Osgi Ws</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiOs <em>Osgi Os</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiOsVersion <em>Osgi Os Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiArch <em>Osgi Arch</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getPresentBundles <em>Present Bundles</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getStatus <em>Status</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport()
 * @model
 * @generated
 */
public interface ErrorReport extends EObject {
    /**
     * Returns the value of the '<em><b>Anonymous Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Anonymous Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Anonymous Id</em>' attribute.
     * @see #setAnonymousId(UUID)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_AnonymousId()
     * @model unique="false" dataType="org.eclipse.recommenders.internal.stacktraces.rcp.model.UUID"
     * @generated
     */
    UUID getAnonymousId();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getAnonymousId <em>Anonymous Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Anonymous Id</em>' attribute.
     * @see #getAnonymousId()
     * @generated
     */
    void setAnonymousId(UUID value);

    /**
     * Returns the value of the '<em><b>Event Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Event Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Event Id</em>' attribute.
     * @see #setEventId(UUID)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_EventId()
     * @model unique="false" dataType="org.eclipse.recommenders.internal.stacktraces.rcp.model.UUID"
     * @generated
     */
    UUID getEventId();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEventId <em>Event Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Event Id</em>' attribute.
     * @see #getEventId()
     * @generated
     */
    void setEventId(UUID value);

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_Name()
     * @model unique="false"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Email</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Email</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Email</em>' attribute.
     * @see #setEmail(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_Email()
     * @model unique="false"
     * @generated
     */
    String getEmail();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEmail <em>Email</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Email</em>' attribute.
     * @see #getEmail()
     * @generated
     */
    void setEmail(String value);

    /**
     * Returns the value of the '<em><b>Comment</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comment</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Comment</em>' attribute.
     * @see #setComment(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_Comment()
     * @model unique="false"
     * @generated
     */
    String getComment();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getComment <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Comment</em>' attribute.
     * @see #getComment()
     * @generated
     */
    void setComment(String value);

    /**
     * Returns the value of the '<em><b>Eclipse Build Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Eclipse Build Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Eclipse Build Id</em>' attribute.
     * @see #setEclipseBuildId(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_EclipseBuildId()
     * @model unique="false"
     * @generated
     */
    String getEclipseBuildId();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEclipseBuildId <em>Eclipse Build Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Eclipse Build Id</em>' attribute.
     * @see #getEclipseBuildId()
     * @generated
     */
    void setEclipseBuildId(String value);

    /**
     * Returns the value of the '<em><b>Eclipse Product</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Eclipse Product</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Eclipse Product</em>' attribute.
     * @see #setEclipseProduct(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_EclipseProduct()
     * @model unique="false"
     * @generated
     */
    String getEclipseProduct();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getEclipseProduct <em>Eclipse Product</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Eclipse Product</em>' attribute.
     * @see #getEclipseProduct()
     * @generated
     */
    void setEclipseProduct(String value);

    /**
     * Returns the value of the '<em><b>Java Runtime Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Java Runtime Version</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Java Runtime Version</em>' attribute.
     * @see #setJavaRuntimeVersion(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_JavaRuntimeVersion()
     * @model unique="false"
     * @generated
     */
    String getJavaRuntimeVersion();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getJavaRuntimeVersion <em>Java Runtime Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Java Runtime Version</em>' attribute.
     * @see #getJavaRuntimeVersion()
     * @generated
     */
    void setJavaRuntimeVersion(String value);

    /**
     * Returns the value of the '<em><b>Osgi Ws</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Osgi Ws</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Osgi Ws</em>' attribute.
     * @see #setOsgiWs(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_OsgiWs()
     * @model unique="false"
     * @generated
     */
    String getOsgiWs();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiWs <em>Osgi Ws</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Osgi Ws</em>' attribute.
     * @see #getOsgiWs()
     * @generated
     */
    void setOsgiWs(String value);

    /**
     * Returns the value of the '<em><b>Osgi Os</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Osgi Os</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Osgi Os</em>' attribute.
     * @see #setOsgiOs(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_OsgiOs()
     * @model unique="false"
     * @generated
     */
    String getOsgiOs();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiOs <em>Osgi Os</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Osgi Os</em>' attribute.
     * @see #getOsgiOs()
     * @generated
     */
    void setOsgiOs(String value);

    /**
     * Returns the value of the '<em><b>Osgi Os Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Osgi Os Version</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Osgi Os Version</em>' attribute.
     * @see #setOsgiOsVersion(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_OsgiOsVersion()
     * @model unique="false"
     * @generated
     */
    String getOsgiOsVersion();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiOsVersion <em>Osgi Os Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Osgi Os Version</em>' attribute.
     * @see #getOsgiOsVersion()
     * @generated
     */
    void setOsgiOsVersion(String value);

    /**
     * Returns the value of the '<em><b>Osgi Arch</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Osgi Arch</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Osgi Arch</em>' attribute.
     * @see #setOsgiArch(String)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_OsgiArch()
     * @model unique="false"
     * @generated
     */
    String getOsgiArch();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getOsgiArch <em>Osgi Arch</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Osgi Arch</em>' attribute.
     * @see #getOsgiArch()
     * @generated
     */
    void setOsgiArch(String value);

    /**
     * Returns the value of the '<em><b>Present Bundles</b></em>' containment reference list.
     * The list contents are of type {@link org.eclipse.recommenders.internal.stacktraces.rcp.model.Bundle}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Present Bundles</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Present Bundles</em>' containment reference list.
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_PresentBundles()
     * @model containment="true"
     * @generated
     */
    EList<Bundle> getPresentBundles();

    /**
     * Returns the value of the '<em><b>Status</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Status</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Status</em>' containment reference.
     * @see #setStatus(Status)
     * @see org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage#getErrorReport_Status()
     * @model containment="true"
     * @generated
     */
    Status getStatus();

    /**
     * Sets the value of the '{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport#getStatus <em>Status</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Status</em>' containment reference.
     * @see #getStatus()
     * @generated
     */
    void setStatus(Status value);

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @model vUnique="false"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel body='<%org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport%> _this = this;\nv.visit(_this);\n<%org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport%> _this_1 = this;\n<%org.eclipse.recommenders.internal.stacktraces.rcp.model.Status%> _status = _this_1.getStatus();\n_status.accept(v);'"
     * @generated
     */
    void accept(Visitor v);

} // ErrorReport
