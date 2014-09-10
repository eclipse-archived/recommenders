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
package org.eclipse.recommenders.internal.stacktraces.rcp.model.impl;

import com.google.common.base.Objects;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Bundle;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Visitor;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Error Report</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getAnonymousId <em>Anonymous Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getEventId <em>Event Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getComment <em>Comment</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getEclipseBuildId <em>Eclipse Build Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getEclipseProduct <em>Eclipse Product</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getJavaRuntimeVersion <em>Java Runtime Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getOsgiWs <em>Osgi Ws</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getOsgiOs <em>Osgi Os</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getOsgiOsVersion <em>Osgi Os Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getOsgiArch <em>Osgi Arch</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getPresentBundles <em>Present Bundles</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ErrorReportImpl#getStatus <em>Status</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ErrorReportImpl extends MinimalEObjectImpl.Container implements ErrorReport {
    /**
     * The default value of the '{@link #getAnonymousId() <em>Anonymous Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAnonymousId()
     * @generated
     * @ordered
     */
    protected static final UUID ANONYMOUS_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getAnonymousId() <em>Anonymous Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAnonymousId()
     * @generated
     * @ordered
     */
    protected UUID anonymousId = ANONYMOUS_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getEventId() <em>Event Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEventId()
     * @generated
     * @ordered
     */
    protected static final UUID EVENT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEventId() <em>Event Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEventId()
     * @generated
     * @ordered
     */
    protected UUID eventId = EVENT_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getEmail() <em>Email</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEmail()
     * @generated
     * @ordered
     */
    protected static final String EMAIL_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEmail() <em>Email</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEmail()
     * @generated
     * @ordered
     */
    protected String email = EMAIL_EDEFAULT;

    /**
     * The default value of the '{@link #getComment() <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getComment()
     * @generated
     * @ordered
     */
    protected static final String COMMENT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getComment() <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getComment()
     * @generated
     * @ordered
     */
    protected String comment = COMMENT_EDEFAULT;

    /**
     * The default value of the '{@link #getEclipseBuildId() <em>Eclipse Build Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEclipseBuildId()
     * @generated
     * @ordered
     */
    protected static final String ECLIPSE_BUILD_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEclipseBuildId() <em>Eclipse Build Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEclipseBuildId()
     * @generated
     * @ordered
     */
    protected String eclipseBuildId = ECLIPSE_BUILD_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getEclipseProduct() <em>Eclipse Product</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEclipseProduct()
     * @generated
     * @ordered
     */
    protected static final String ECLIPSE_PRODUCT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEclipseProduct() <em>Eclipse Product</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEclipseProduct()
     * @generated
     * @ordered
     */
    protected String eclipseProduct = ECLIPSE_PRODUCT_EDEFAULT;

    /**
     * The default value of the '{@link #getJavaRuntimeVersion() <em>Java Runtime Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJavaRuntimeVersion()
     * @generated
     * @ordered
     */
    protected static final String JAVA_RUNTIME_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getJavaRuntimeVersion() <em>Java Runtime Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJavaRuntimeVersion()
     * @generated
     * @ordered
     */
    protected String javaRuntimeVersion = JAVA_RUNTIME_VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getOsgiWs() <em>Osgi Ws</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiWs()
     * @generated
     * @ordered
     */
    protected static final String OSGI_WS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getOsgiWs() <em>Osgi Ws</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiWs()
     * @generated
     * @ordered
     */
    protected String osgiWs = OSGI_WS_EDEFAULT;

    /**
     * The default value of the '{@link #getOsgiOs() <em>Osgi Os</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiOs()
     * @generated
     * @ordered
     */
    protected static final String OSGI_OS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getOsgiOs() <em>Osgi Os</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiOs()
     * @generated
     * @ordered
     */
    protected String osgiOs = OSGI_OS_EDEFAULT;

    /**
     * The default value of the '{@link #getOsgiOsVersion() <em>Osgi Os Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiOsVersion()
     * @generated
     * @ordered
     */
    protected static final String OSGI_OS_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getOsgiOsVersion() <em>Osgi Os Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiOsVersion()
     * @generated
     * @ordered
     */
    protected String osgiOsVersion = OSGI_OS_VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getOsgiArch() <em>Osgi Arch</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiArch()
     * @generated
     * @ordered
     */
    protected static final String OSGI_ARCH_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getOsgiArch() <em>Osgi Arch</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOsgiArch()
     * @generated
     * @ordered
     */
    protected String osgiArch = OSGI_ARCH_EDEFAULT;

    /**
     * The cached value of the '{@link #getPresentBundles() <em>Present Bundles</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPresentBundles()
     * @generated
     * @ordered
     */
    protected EList<Bundle> presentBundles;

    /**
     * The cached value of the '{@link #getStatus() <em>Status</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getStatus()
     * @generated
     * @ordered
     */
    protected Status status;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ErrorReportImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.ERROR_REPORT;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public UUID getAnonymousId() {
        return anonymousId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAnonymousId(UUID newAnonymousId) {
        UUID oldAnonymousId = anonymousId;
        anonymousId = newAnonymousId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__ANONYMOUS_ID, oldAnonymousId, anonymousId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEventId(UUID newEventId) {
        UUID oldEventId = eventId;
        eventId = newEventId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__EVENT_ID, oldEventId, eventId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEmail() {
        return email;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEmail(String newEmail) {
        String oldEmail = email;
        email = newEmail;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__EMAIL, oldEmail, email));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getComment() {
        return comment;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setComment(String newComment) {
        String oldComment = comment;
        comment = newComment;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__COMMENT, oldComment, comment));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEclipseBuildId() {
        return eclipseBuildId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEclipseBuildId(String newEclipseBuildId) {
        String oldEclipseBuildId = eclipseBuildId;
        eclipseBuildId = newEclipseBuildId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__ECLIPSE_BUILD_ID, oldEclipseBuildId, eclipseBuildId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEclipseProduct() {
        return eclipseProduct;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEclipseProduct(String newEclipseProduct) {
        String oldEclipseProduct = eclipseProduct;
        eclipseProduct = newEclipseProduct;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__ECLIPSE_PRODUCT, oldEclipseProduct, eclipseProduct));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getJavaRuntimeVersion() {
        return javaRuntimeVersion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setJavaRuntimeVersion(String newJavaRuntimeVersion) {
        String oldJavaRuntimeVersion = javaRuntimeVersion;
        javaRuntimeVersion = newJavaRuntimeVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__JAVA_RUNTIME_VERSION, oldJavaRuntimeVersion, javaRuntimeVersion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getOsgiWs() {
        return osgiWs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOsgiWs(String newOsgiWs) {
        String oldOsgiWs = osgiWs;
        osgiWs = newOsgiWs;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__OSGI_WS, oldOsgiWs, osgiWs));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getOsgiOs() {
        return osgiOs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOsgiOs(String newOsgiOs) {
        String oldOsgiOs = osgiOs;
        osgiOs = newOsgiOs;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__OSGI_OS, oldOsgiOs, osgiOs));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getOsgiOsVersion() {
        return osgiOsVersion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOsgiOsVersion(String newOsgiOsVersion) {
        String oldOsgiOsVersion = osgiOsVersion;
        osgiOsVersion = newOsgiOsVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__OSGI_OS_VERSION, oldOsgiOsVersion, osgiOsVersion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getOsgiArch() {
        return osgiArch;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOsgiArch(String newOsgiArch) {
        String oldOsgiArch = osgiArch;
        osgiArch = newOsgiArch;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__OSGI_ARCH, oldOsgiArch, osgiArch));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<Bundle> getPresentBundles() {
        if (presentBundles == null) {
            presentBundles = new EObjectContainmentEList<Bundle>(Bundle.class, this, ModelPackage.ERROR_REPORT__PRESENT_BUNDLES);
        }
        return presentBundles;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Status getStatus() {
        return status;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetStatus(Status newStatus, NotificationChain msgs) {
        Status oldStatus = status;
        status = newStatus;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__STATUS, oldStatus, newStatus);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setStatus(Status newStatus) {
        if (newStatus != status) {
            NotificationChain msgs = null;
            if (status != null)
                msgs = ((InternalEObject)status).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.ERROR_REPORT__STATUS, null, msgs);
            if (newStatus != null)
                msgs = ((InternalEObject)newStatus).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.ERROR_REPORT__STATUS, null, msgs);
            msgs = basicSetStatus(newStatus, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ERROR_REPORT__STATUS, newStatus, newStatus));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void accept(final Visitor v) {
        v.visit(this);
        Status _status = this.getStatus();
        boolean _notEquals = (!Objects.equal(_status, null));
        if (_notEquals) {
            Status _status_1 = this.getStatus();
            _status_1.accept(v);
        }
        EList<Bundle> _presentBundles = this.getPresentBundles();
        for (final Bundle b : _presentBundles) {
            b.accept(v);
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case ModelPackage.ERROR_REPORT__PRESENT_BUNDLES:
                return ((InternalEList<?>)getPresentBundles()).basicRemove(otherEnd, msgs);
            case ModelPackage.ERROR_REPORT__STATUS:
                return basicSetStatus(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ModelPackage.ERROR_REPORT__ANONYMOUS_ID:
                return getAnonymousId();
            case ModelPackage.ERROR_REPORT__EVENT_ID:
                return getEventId();
            case ModelPackage.ERROR_REPORT__NAME:
                return getName();
            case ModelPackage.ERROR_REPORT__EMAIL:
                return getEmail();
            case ModelPackage.ERROR_REPORT__COMMENT:
                return getComment();
            case ModelPackage.ERROR_REPORT__ECLIPSE_BUILD_ID:
                return getEclipseBuildId();
            case ModelPackage.ERROR_REPORT__ECLIPSE_PRODUCT:
                return getEclipseProduct();
            case ModelPackage.ERROR_REPORT__JAVA_RUNTIME_VERSION:
                return getJavaRuntimeVersion();
            case ModelPackage.ERROR_REPORT__OSGI_WS:
                return getOsgiWs();
            case ModelPackage.ERROR_REPORT__OSGI_OS:
                return getOsgiOs();
            case ModelPackage.ERROR_REPORT__OSGI_OS_VERSION:
                return getOsgiOsVersion();
            case ModelPackage.ERROR_REPORT__OSGI_ARCH:
                return getOsgiArch();
            case ModelPackage.ERROR_REPORT__PRESENT_BUNDLES:
                return getPresentBundles();
            case ModelPackage.ERROR_REPORT__STATUS:
                return getStatus();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ModelPackage.ERROR_REPORT__ANONYMOUS_ID:
                setAnonymousId((UUID)newValue);
                return;
            case ModelPackage.ERROR_REPORT__EVENT_ID:
                setEventId((UUID)newValue);
                return;
            case ModelPackage.ERROR_REPORT__NAME:
                setName((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__EMAIL:
                setEmail((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__COMMENT:
                setComment((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__ECLIPSE_BUILD_ID:
                setEclipseBuildId((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__ECLIPSE_PRODUCT:
                setEclipseProduct((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__JAVA_RUNTIME_VERSION:
                setJavaRuntimeVersion((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_WS:
                setOsgiWs((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_OS:
                setOsgiOs((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_OS_VERSION:
                setOsgiOsVersion((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_ARCH:
                setOsgiArch((String)newValue);
                return;
            case ModelPackage.ERROR_REPORT__PRESENT_BUNDLES:
                getPresentBundles().clear();
                getPresentBundles().addAll((Collection<? extends Bundle>)newValue);
                return;
            case ModelPackage.ERROR_REPORT__STATUS:
                setStatus((Status)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ModelPackage.ERROR_REPORT__ANONYMOUS_ID:
                setAnonymousId(ANONYMOUS_ID_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__EVENT_ID:
                setEventId(EVENT_ID_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__NAME:
                setName(NAME_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__EMAIL:
                setEmail(EMAIL_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__COMMENT:
                setComment(COMMENT_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__ECLIPSE_BUILD_ID:
                setEclipseBuildId(ECLIPSE_BUILD_ID_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__ECLIPSE_PRODUCT:
                setEclipseProduct(ECLIPSE_PRODUCT_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__JAVA_RUNTIME_VERSION:
                setJavaRuntimeVersion(JAVA_RUNTIME_VERSION_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_WS:
                setOsgiWs(OSGI_WS_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_OS:
                setOsgiOs(OSGI_OS_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_OS_VERSION:
                setOsgiOsVersion(OSGI_OS_VERSION_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__OSGI_ARCH:
                setOsgiArch(OSGI_ARCH_EDEFAULT);
                return;
            case ModelPackage.ERROR_REPORT__PRESENT_BUNDLES:
                getPresentBundles().clear();
                return;
            case ModelPackage.ERROR_REPORT__STATUS:
                setStatus((Status)null);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ModelPackage.ERROR_REPORT__ANONYMOUS_ID:
                return ANONYMOUS_ID_EDEFAULT == null ? anonymousId != null : !ANONYMOUS_ID_EDEFAULT.equals(anonymousId);
            case ModelPackage.ERROR_REPORT__EVENT_ID:
                return EVENT_ID_EDEFAULT == null ? eventId != null : !EVENT_ID_EDEFAULT.equals(eventId);
            case ModelPackage.ERROR_REPORT__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case ModelPackage.ERROR_REPORT__EMAIL:
                return EMAIL_EDEFAULT == null ? email != null : !EMAIL_EDEFAULT.equals(email);
            case ModelPackage.ERROR_REPORT__COMMENT:
                return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
            case ModelPackage.ERROR_REPORT__ECLIPSE_BUILD_ID:
                return ECLIPSE_BUILD_ID_EDEFAULT == null ? eclipseBuildId != null : !ECLIPSE_BUILD_ID_EDEFAULT.equals(eclipseBuildId);
            case ModelPackage.ERROR_REPORT__ECLIPSE_PRODUCT:
                return ECLIPSE_PRODUCT_EDEFAULT == null ? eclipseProduct != null : !ECLIPSE_PRODUCT_EDEFAULT.equals(eclipseProduct);
            case ModelPackage.ERROR_REPORT__JAVA_RUNTIME_VERSION:
                return JAVA_RUNTIME_VERSION_EDEFAULT == null ? javaRuntimeVersion != null : !JAVA_RUNTIME_VERSION_EDEFAULT.equals(javaRuntimeVersion);
            case ModelPackage.ERROR_REPORT__OSGI_WS:
                return OSGI_WS_EDEFAULT == null ? osgiWs != null : !OSGI_WS_EDEFAULT.equals(osgiWs);
            case ModelPackage.ERROR_REPORT__OSGI_OS:
                return OSGI_OS_EDEFAULT == null ? osgiOs != null : !OSGI_OS_EDEFAULT.equals(osgiOs);
            case ModelPackage.ERROR_REPORT__OSGI_OS_VERSION:
                return OSGI_OS_VERSION_EDEFAULT == null ? osgiOsVersion != null : !OSGI_OS_VERSION_EDEFAULT.equals(osgiOsVersion);
            case ModelPackage.ERROR_REPORT__OSGI_ARCH:
                return OSGI_ARCH_EDEFAULT == null ? osgiArch != null : !OSGI_ARCH_EDEFAULT.equals(osgiArch);
            case ModelPackage.ERROR_REPORT__PRESENT_BUNDLES:
                return presentBundles != null && !presentBundles.isEmpty();
            case ModelPackage.ERROR_REPORT__STATUS:
                return status != null;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
        switch (operationID) {
            case ModelPackage.ERROR_REPORT___ACCEPT__VISITOR:
                accept((Visitor)arguments.get(0));
                return null;
        }
        return super.eInvoke(operationID, arguments);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (anonymousId: ");
        result.append(anonymousId);
        result.append(", eventId: ");
        result.append(eventId);
        result.append(", name: ");
        result.append(name);
        result.append(", email: ");
        result.append(email);
        result.append(", comment: ");
        result.append(comment);
        result.append(", eclipseBuildId: ");
        result.append(eclipseBuildId);
        result.append(", eclipseProduct: ");
        result.append(eclipseProduct);
        result.append(", javaRuntimeVersion: ");
        result.append(javaRuntimeVersion);
        result.append(", osgiWs: ");
        result.append(osgiWs);
        result.append(", osgiOs: ");
        result.append(osgiOs);
        result.append(", osgiOsVersion: ");
        result.append(osgiOsVersion);
        result.append(", osgiArch: ");
        result.append(osgiArch);
        result.append(')');
        return result.toString();
    }

} //ErrorReportImpl
