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
import org.eclipse.recommenders.internal.stacktraces.rcp.model.StackTraceElement;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Visitor;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Throwable</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ThrowableImpl#getClassName <em>Class Name</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ThrowableImpl#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ThrowableImpl#getCause <em>Cause</em>}</li>
 *   <li>{@link org.eclipse.recommenders.internal.stacktraces.rcp.model.impl.ThrowableImpl#getStackTrace <em>Stack Trace</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ThrowableImpl extends MinimalEObjectImpl.Container implements org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable {
    /**
     * The default value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClassName()
     * @generated
     * @ordered
     */
    protected static final String CLASS_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClassName()
     * @generated
     * @ordered
     */
    protected String className = CLASS_NAME_EDEFAULT;

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
     * The cached value of the '{@link #getCause() <em>Cause</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCause()
     * @generated
     * @ordered
     */
    protected org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable cause;

    /**
     * The cached value of the '{@link #getStackTrace() <em>Stack Trace</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getStackTrace()
     * @generated
     * @ordered
     */
    protected EList<StackTraceElement> stackTrace;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ThrowableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.THROWABLE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getClassName() {
        return className;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setClassName(String newClassName) {
        String oldClassName = className;
        className = newClassName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.THROWABLE__CLASS_NAME, oldClassName, className));
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
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.THROWABLE__MESSAGE, oldMessage, message));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable getCause() {
        return cause;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCause(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable newCause, NotificationChain msgs) {
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable oldCause = cause;
        cause = newCause;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.THROWABLE__CAUSE, oldCause, newCause);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCause(org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable newCause) {
        if (newCause != cause) {
            NotificationChain msgs = null;
            if (cause != null)
                msgs = ((InternalEObject)cause).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.THROWABLE__CAUSE, null, msgs);
            if (newCause != null)
                msgs = ((InternalEObject)newCause).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.THROWABLE__CAUSE, null, msgs);
            msgs = basicSetCause(newCause, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.THROWABLE__CAUSE, newCause, newCause));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<StackTraceElement> getStackTrace() {
        if (stackTrace == null) {
            stackTrace = new EObjectContainmentEList<StackTraceElement>(StackTraceElement.class, this, ModelPackage.THROWABLE__STACK_TRACE);
        }
        return stackTrace;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void accept(final Visitor v) {
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _this = this;
        v.visit(_this);
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _this_1 = this;
        EList<StackTraceElement> _stackTrace = _this_1.getStackTrace();
        for (final StackTraceElement element : _stackTrace) {
            element.accept(v);
        }
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _this_2 = this;
        org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _cause = _this_2.getCause();
        boolean _notEquals = (!Objects.equal(_cause, null));
        if (_notEquals) {
            org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _this_3 = this;
            org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable _cause_1 = _this_3.getCause();
            _cause_1.accept(v);
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
            case ModelPackage.THROWABLE__CAUSE:
                return basicSetCause(null, msgs);
            case ModelPackage.THROWABLE__STACK_TRACE:
                return ((InternalEList<?>)getStackTrace()).basicRemove(otherEnd, msgs);
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
            case ModelPackage.THROWABLE__CLASS_NAME:
                return getClassName();
            case ModelPackage.THROWABLE__MESSAGE:
                return getMessage();
            case ModelPackage.THROWABLE__CAUSE:
                return getCause();
            case ModelPackage.THROWABLE__STACK_TRACE:
                return getStackTrace();
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
            case ModelPackage.THROWABLE__CLASS_NAME:
                setClassName((String)newValue);
                return;
            case ModelPackage.THROWABLE__MESSAGE:
                setMessage((String)newValue);
                return;
            case ModelPackage.THROWABLE__CAUSE:
                setCause((org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable)newValue);
                return;
            case ModelPackage.THROWABLE__STACK_TRACE:
                getStackTrace().clear();
                getStackTrace().addAll((Collection<? extends StackTraceElement>)newValue);
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
            case ModelPackage.THROWABLE__CLASS_NAME:
                setClassName(CLASS_NAME_EDEFAULT);
                return;
            case ModelPackage.THROWABLE__MESSAGE:
                setMessage(MESSAGE_EDEFAULT);
                return;
            case ModelPackage.THROWABLE__CAUSE:
                setCause((org.eclipse.recommenders.internal.stacktraces.rcp.model.Throwable)null);
                return;
            case ModelPackage.THROWABLE__STACK_TRACE:
                getStackTrace().clear();
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
            case ModelPackage.THROWABLE__CLASS_NAME:
                return CLASS_NAME_EDEFAULT == null ? className != null : !CLASS_NAME_EDEFAULT.equals(className);
            case ModelPackage.THROWABLE__MESSAGE:
                return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
            case ModelPackage.THROWABLE__CAUSE:
                return cause != null;
            case ModelPackage.THROWABLE__STACK_TRACE:
                return stackTrace != null && !stackTrace.isEmpty();
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
            case ModelPackage.THROWABLE___ACCEPT__VISITOR:
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
        result.append(" (className: ");
        result.append(className);
        result.append(", message: ");
        result.append(message);
        result.append(')');
        return result.toString();
    }

} //ThrowableImpl
