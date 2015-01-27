/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.RememberSendAction.NONE;
import static org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction.ASK;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelFactory;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.RememberSendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);
    private static Settings settings;

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences s = DefaultScope.INSTANCE.getNode(PLUGIN_ID);
        s.put(PROP_SERVER, SERVER_URL);
        s.put(PROP_NAME, "");
        s.put(PROP_EMAIL, "");
        s.putBoolean(PROP_SKIP_SIMILAR_ERRORS, true);
        s.putBoolean(PROP_CONFIGURED, false);
        s.putLong(PROP_REMEMBER_SETTING_PERIOD_START, 0L);
        s.put(PROP_WHITELISTED_PLUGINS, Constants.WHITELISTED_PLUGINS);
        s.put(PROP_WHITELISTED_PACKAGES, Constants.WHITELISTED_PACKAGES);
        s.put(PROP_SEND_ACTION, SendAction.ASK.name());
        s.put(PROP_REMEMBER_SEND_ACTION, RememberSendAction.NONE.name());
        s.putBoolean(PROP_ANONYMIZE_STACKTRACES, true);
        s.putBoolean(PROP_ANONYMIZE_MESSAGES, false);
    }

    public static Settings getDefault() {
        if (settings == null) {
            settings = ModelFactory.eINSTANCE.createSettings();
            final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
            final EClass eClass = settings.eClass();
            loadFromPreferences(store, settings, eClass);
            registerPreferenceStoreChangeListener(store, settings, eClass);
            registerSettingsChangeListener(store, settings);
            handleRestart24hSendAction(settings);
        }
        return settings;
    }

    private static void registerSettingsChangeListener(final ScopedPreferenceStore store, final Settings settings) {
        settings.eAdapters().add(new AdapterImpl() {
            @Override
            public void notifyChanged(Notification msg) {
                Object feature = msg.getFeature();
                if (!(feature instanceof EAttribute)) {
                    return;
                }
                EAttribute attr = (EAttribute) feature;
                String key = attr.getName();
                EDataType type = attr.getEAttributeType();
                Object value = msg.getNewValue();
                String data = EcoreUtil.convertToString(type, value);
                try {
                    store.putValue(key, data);
                    store.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void registerPreferenceStoreChangeListener(final ScopedPreferenceStore store,
            final Settings settings, final EClass eClass) {
        store.addPropertyChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                EStructuralFeature feature = eClass.getEStructuralFeature(property);
                if (feature != null && feature instanceof EAttribute) {
                    EAttribute attr = (EAttribute) feature;
                    EDataType type = attr.getEAttributeType();
                    String string = EcoreUtil.convertToString(type, event.getNewValue());
                    Object value = EcoreUtil.createFromString(type, string);
                    settings.eSet(feature, value);
                }
            }
        });
    }

    private static void loadFromPreferences(final ScopedPreferenceStore store, final Settings settings,
            final EClass eClass) {
        settings.eSetDeliver(false);
        for (EAttribute attr : eClass.getEAllAttributes()) {
            EDataType type = attr.getEAttributeType();
            String key = attr.getName();
            String value = store.getString(key);
            try {
                Object data = EcoreUtil.createFromString(type, value);
                settings.eSet(attr, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        settings.eSetDeliver(true);
    }

    static void handleRestart24hSendAction(Settings settings) {
        switch (settings.getRememberSendAction()) {
        case RESTART:
            settings.setAction(ASK);
            settings.setRememberSendAction(NONE);
            break;
        case HOURS_24:
            long elapsedTime = System.currentTimeMillis() - settings.getRememberSendActionPeriodStart();
            boolean isDayElapsed = elapsedTime >= MS_PER_DAY;
            if (isDayElapsed) {
                log(LogMessages.PAUSE_PERIOD_ELAPSED);
                settings.setAction(ASK);
            }
            settings.setRememberSendAction(NONE);
            break;
        default:
            // do nothing
        }
    }
}
