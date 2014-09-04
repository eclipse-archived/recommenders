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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Status;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Visitor;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Status</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getPluginId <em>Plugin Id</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getPluginVersion <em>Plugin Version</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getCode <em>Code</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getFingerprint <em>Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getException <em>Exception</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.StatusImpl#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class StatusImpl extends MinimalEObjectImpl.Container implements Status {
    /**
     * The default value of the '{@link #getPluginId() <em>Plugin Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPluginId()
     * @generated
     * @ordered
     */
    protected static final String PLUGIN_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPluginId() <em>Plugin Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPluginId()
     * @generated
     * @ordered
     */
    protected String pluginId = PLUGIN_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getPluginVersion() <em>Plugin Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPluginVersion()
     * @generated
     * @ordered
     */
    protected static final String PLUGIN_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPluginVersion() <em>Plugin Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPluginVersion()
     * @generated
     * @ordered
     */
    protected String pluginVersion = PLUGIN_VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getCode() <em>Code</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCode()
     * @generated
     * @ordered
     */
    protected static final int CODE_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getCode() <em>Code</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCode()
     * @generated
     * @ordered
     */
    protected int code = CODE_EDEFAULT;

    /**
     * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSeverity()
     * @generated
     * @ordered
     */
    protected static final int SEVERITY_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSeverity()
     * @generated
     * @ordered
     */
    protected int severity = SEVERITY_EDEFAULT;

    /**
     * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMessage()
     * @generated
     * @ordered
     */
    protected static final String MESSAGE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMessage()
     * @generated
     * @ordered
     */
    protected String message = MESSAGE_EDEFAULT;

    /**
     * The default value of the '{@link #getFingerprint() <em>Fingerprint</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFingerprint()
     * @generated
     * @ordered
     */
    protected static final String FINGERPRINT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getFingerprint() <em>Fingerprint</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFingerprint()
     * @generated
     * @ordered
     */
    protected String fingerprint = FINGERPRINT_EDEFAULT;

    /**
     * The cached value of the '{@link #getException() <em>Exception</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getException()
     * @generated
     * @ordered
     */
    protected org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable exception;

    /**
     * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getChildren()
     * @generated
     * @ordered
     */
    protected EList<Status> children;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected StatusImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.STATUS;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPluginId(String newPluginId) {
        String oldPluginId = pluginId;
        pluginId = newPluginId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__PLUGIN_ID, oldPluginId, pluginId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPluginVersion(String newPluginVersion) {
        String oldPluginVersion = pluginVersion;
        pluginVersion = newPluginVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__PLUGIN_VERSION, oldPluginVersion, pluginVersion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public int getCode() {
        return code;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCode(int newCode) {
        int oldCode = code;
        code = newCode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__CODE, oldCode, code));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSeverity(int newSeverity) {
        int oldSeverity = severity;
        severity = newSeverity;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__SEVERITY, oldSeverity, severity));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getMessage() {
        return message;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMessage(String newMessage) {
        String oldMessage = message;
        message = newMessage;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__MESSAGE, oldMessage, message));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setFingerprint(String newFingerprint) {
        String oldFingerprint = fingerprint;
        fingerprint = newFingerprint;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__FINGERPRINT, oldFingerprint, fingerprint));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable getException() {
        return exception;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetException(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable newException, NotificationChain msgs) {
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable oldException = exception;
        exception = newException;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__EXCEPTION, oldException, newException);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setException(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable newException) {
        if (newException != exception) {
            NotificationChain msgs = null;
            if (exception != null)
                msgs = ((InternalEObject)exception).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.STATUS__EXCEPTION, null, msgs);
            if (newException != null)
                msgs = ((InternalEObject)newException).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.STATUS__EXCEPTION, null, msgs);
            msgs = basicSetException(newException, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.STATUS__EXCEPTION, newException, newException));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<Status> getChildren() {
        if (children == null) {
            children = new EObjectContainmentEList<Status>(Status.class, this, ModelPackage.STATUS__CHILDREN);
        }
        return children;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void accept(final Visitor v) {
        Status _this = this;
        v.visit(_this);
        Status _this_1 = this;
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _exception = _this_1.getException();
        _exception.accept(v);
        Status _this_2 = this;
        EList<Status> _children = _this_2.getChildren();
        for (final Status child : _children) {
            child.accept(v);
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
            case ModelPackage.STATUS__EXCEPTION:
                return basicSetException(null, msgs);
            case ModelPackage.STATUS__CHILDREN:
                return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
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
            case ModelPackage.STATUS__PLUGIN_ID:
                return getPluginId();
            case ModelPackage.STATUS__PLUGIN_VERSION:
                return getPluginVersion();
            case ModelPackage.STATUS__CODE:
                return getCode();
            case ModelPackage.STATUS__SEVERITY:
                return getSeverity();
            case ModelPackage.STATUS__MESSAGE:
                return getMessage();
            case ModelPackage.STATUS__FINGERPRINT:
                return getFingerprint();
            case ModelPackage.STATUS__EXCEPTION:
                return getException();
            case ModelPackage.STATUS__CHILDREN:
                return getChildren();
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
            case ModelPackage.STATUS__PLUGIN_ID:
                setPluginId((String)newValue);
                return;
            case ModelPackage.STATUS__PLUGIN_VERSION:
                setPluginVersion((String)newValue);
                return;
            case ModelPackage.STATUS__CODE:
                setCode((Integer)newValue);
                return;
            case ModelPackage.STATUS__SEVERITY:
                setSeverity((Integer)newValue);
                return;
            case ModelPackage.STATUS__MESSAGE:
                setMessage((String)newValue);
                return;
            case ModelPackage.STATUS__FINGERPRINT:
                setFingerprint((String)newValue);
                return;
            case ModelPackage.STATUS__EXCEPTION:
                setException((org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable)newValue);
                return;
            case ModelPackage.STATUS__CHILDREN:
                getChildren().clear();
                getChildren().addAll((Collection<? extends Status>)newValue);
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
            case ModelPackage.STATUS__PLUGIN_ID:
                setPluginId(PLUGIN_ID_EDEFAULT);
                return;
            case ModelPackage.STATUS__PLUGIN_VERSION:
                setPluginVersion(PLUGIN_VERSION_EDEFAULT);
                return;
            case ModelPackage.STATUS__CODE:
                setCode(CODE_EDEFAULT);
                return;
            case ModelPackage.STATUS__SEVERITY:
                setSeverity(SEVERITY_EDEFAULT);
                return;
            case ModelPackage.STATUS__MESSAGE:
                setMessage(MESSAGE_EDEFAULT);
                return;
            case ModelPackage.STATUS__FINGERPRINT:
                setFingerprint(FINGERPRINT_EDEFAULT);
                return;
            case ModelPackage.STATUS__EXCEPTION:
                setException((org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable)null);
                return;
            case ModelPackage.STATUS__CHILDREN:
                getChildren().clear();
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
            case ModelPackage.STATUS__PLUGIN_ID:
                return PLUGIN_ID_EDEFAULT == null ? pluginId != null : !PLUGIN_ID_EDEFAULT.equals(pluginId);
            case ModelPackage.STATUS__PLUGIN_VERSION:
                return PLUGIN_VERSION_EDEFAULT == null ? pluginVersion != null : !PLUGIN_VERSION_EDEFAULT.equals(pluginVersion);
            case ModelPackage.STATUS__CODE:
                return code != CODE_EDEFAULT;
            case ModelPackage.STATUS__SEVERITY:
                return severity != SEVERITY_EDEFAULT;
            case ModelPackage.STATUS__MESSAGE:
                return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
            case ModelPackage.STATUS__FINGERPRINT:
                return FINGERPRINT_EDEFAULT == null ? fingerprint != null : !FINGERPRINT_EDEFAULT.equals(fingerprint);
            case ModelPackage.STATUS__EXCEPTION:
                return exception != null;
            case ModelPackage.STATUS__CHILDREN:
                return children != null && !children.isEmpty();
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
            case ModelPackage.STATUS___ACCEPT__VISITOR:
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
        result.append(" (pluginId: ");
        result.append(pluginId);
        result.append(", pluginVersion: ");
        result.append(pluginVersion);
        result.append(", code: ");
        result.append(code);
        result.append(", severity: ");
        result.append(severity);
        result.append(", message: ");
        result.append(message);
        result.append(", fingerprint: ");
        result.append(fingerprint);
        result.append(')');
        return result.toString();
    }

} //StatusImpl
