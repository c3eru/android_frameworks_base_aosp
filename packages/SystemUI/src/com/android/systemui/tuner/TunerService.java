/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.provider.Settings;

import static android.provider.Settings.System.SHOW_BATTERY_PERCENT;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.SystemUIDialog;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Set;

import cyanogenmod.providers.CMSettings;

public class TunerService extends SystemUI {
=======
public abstract class TunerService {
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040

    public static final String ACTION_CLEAR = "com.android.systemui.action.CLEAR_TUNER";

    public abstract void clearAll();
    public abstract void destroy();

    public abstract String getValue(String setting);
    public abstract int getValue(String setting, int def);
    public abstract String getValue(String setting, String def);

    public abstract void setValue(String setting, String value);
    public abstract void setValue(String setting, int value);

    public abstract void addTunable(Tunable tunable, String... keys);
    public abstract void removeTunable(Tunable tunable);

    public interface Tunable {
        void onTuningChanged(String key, String newValue);
    }

    private static Context userContext(Context context) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0,
                    new UserHandle(ActivityManager.getCurrentUser()));
        } catch (NameNotFoundException e) {
            return context;
        }
    }

    public static final void setTunerEnabled(Context context, boolean enabled) {
        userContext(context).getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, TunerActivity.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        userContext(context).getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, TunerActivity.ACTIVITY_ALIAS_NAME),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static final boolean isTunerEnabled(Context context) {
        return userContext(context).getPackageManager().getComponentEnabledSetting(
                new ComponentName(context, TunerActivity.class))
                == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static class ClearReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CLEAR.equals(intent.getAction())) {
                Dependency.get(TunerService.class).clearAll();
            }
        }
<<<<<<< HEAD
        putComponent(TunerService.class, this);

        mCurrentUser = ActivityManager.getCurrentUser();
        mUserTracker = new CurrentUserTracker(mContext) {
            @Override
            public void onUserSwitched(int newUserId) {
                mCurrentUser = newUserId;
                reloadAll();
                reregisterAll();
            }
        };
        mUserTracker.startTracking();
    }

    private void upgradeTuner(int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            String blacklistStr = getValue(StatusBarIconController.ICON_BLACKLIST);
            if (blacklistStr != null) {
                ArraySet<String> iconBlacklist =
                        StatusBarIconController.getIconBlacklist(blacklistStr);

                iconBlacklist.add("rotate");
                iconBlacklist.add("headset");

                setValue(StatusBarIconController.ICON_BLACKLIST,
                         TextUtils.join(",", iconBlacklist));
            }
        }
        setValue(TUNER_VERSION, newVersion);
    }

    private boolean isCMSystem(String key) {
        return key.startsWith("cmsystem:");
    }

    private boolean isCMSecure(String key) {
        return key.startsWith("cmsecure:");
    }

    private boolean isSystem(String key) {
        return key.startsWith("system:");
    }

    private String chomp(String key) {
        return key.replaceFirst("^(cmsecure|cmsystem|system):", "");
    }

    public String getValue(String setting) {
        if (isCMSecure(setting)) {
            return CMSettings.Secure.getStringForUser(
                    mContentResolver, chomp(setting), mCurrentUser);
        }
        if (isCMSystem(setting)) {
            return CMSettings.System.getStringForUser(
                    mContentResolver, chomp(setting), mCurrentUser);
        }
        if (isSystem(setting)) {
            return Settings.System.getStringForUser(
                    mContentResolver, chomp(setting), mCurrentUser);
        }
        return Settings.Secure.getStringForUser(mContentResolver, setting, mCurrentUser);
    }

    public void setValue(String setting, String value) {
        if (isCMSecure(setting)) {
            CMSettings.Secure.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isCMSystem(setting)) {
            CMSettings.System.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isSystem(setting)) {
            Settings.System.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else {
            Settings.Secure.putStringForUser(mContentResolver, setting, value, mCurrentUser);
        }
    }

    public int getValue(String setting, int def) {
        if (isCMSecure(setting)) {
            return CMSettings.Secure.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        }
        if (isCMSystem(setting)) {
            return CMSettings.System.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        }
        if (isSystem(setting)) {
            return Settings.System.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        }
        return Settings.Secure.getIntForUser(mContentResolver, setting, def, mCurrentUser);
    }

    public void setValue(String setting, int value) {
        if (isCMSecure(setting)) {
            CMSettings.Secure.putIntForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isCMSystem(setting)) {
            CMSettings.System.putIntForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isSystem(setting)) {
            Settings.System.putIntForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else {
            Settings.Secure.putIntForUser(mContentResolver, setting, value, mCurrentUser);
        }
    }

    public void addTunable(Tunable tunable, String... keys) {
        for (String key : keys) {
            addTunable(tunable, key);
        }
    }

    private void addTunable(Tunable tunable, String key) {
        if (!mTunableLookup.containsKey(key)) {
            mTunableLookup.put(key, new ArraySet<Tunable>());
        }
        mTunableLookup.get(key).add(tunable);
        final Uri uri;
        if (isCMSecure(key)) {
            uri = CMSettings.Secure.getUriFor(chomp(key));
        } else if (isCMSystem(key)) {
            uri = CMSettings.System.getUriFor(chomp(key));
        } else if (isSystem(key)) {
            uri = Settings.System.getUriFor(chomp(key));
        } else {
            uri = Settings.Secure.getUriFor(key);
        }
        if (!mListeningUris.containsKey(uri)) {
            mListeningUris.put(uri, key);
            mContentResolver.registerContentObserver(uri, false, mObserver, mCurrentUser);
        }
        // Send the first state.
        tunable.onTuningChanged(key, getValue(key));
    }

    public void removeTunable(Tunable tunable) {
        for (Set<Tunable> list : mTunableLookup.values()) {
            list.remove(tunable);
        }
    }

    protected void reregisterAll() {
        if (mListeningUris.size() == 0) {
            return;
        }
        mContentResolver.unregisterContentObserver(mObserver);
        for (Uri uri : mListeningUris.keySet()) {
            mContentResolver.registerContentObserver(uri, false, mObserver, mCurrentUser);
        }
    }

    public void reloadSetting(Uri uri) {
        String key = mListeningUris.get(uri);
        Set<Tunable> tunables = mTunableLookup.get(key);
        if (tunables == null) {
            return;
        }
        String value = getValue(key);
        for (Tunable tunable : tunables) {
            tunable.onTuningChanged(key, value);
        }
    }

    private void reloadAll() {
        for (String key : mTunableLookup.keySet()) {
            String value = getValue(key);
            for (Tunable tunable : mTunableLookup.get(key)) {
                tunable.onTuningChanged(key, value);
            }
        }
    }

    public void clearAll() {
        // A couple special cases.
        Settings.Global.putString(mContentResolver, DemoMode.DEMO_MODE_ALLOWED, null);
        Settings.System.putString(mContentResolver, BatteryMeterDrawable.SHOW_PERCENT_SETTING, null);
        Intent intent = new Intent(DemoMode.ACTION_DEMO);
        intent.putExtra(DemoMode.EXTRA_COMMAND, DemoMode.COMMAND_EXIT);
        mContext.sendBroadcast(intent);

        for (String key : mTunableLookup.keySet()) {
            setValue(key, null);
        }
    }

    // Only used in other processes, such as the tuner.
    private static TunerService sInstance;

    public static TunerService get(Context context) {
        TunerService service = null;
        if (context.getApplicationContext() instanceof SystemUIApplication) {
            SystemUIApplication sysUi = (SystemUIApplication) context.getApplicationContext();
            service = sysUi.getComponent(TunerService.class);
        }
        if (service == null) {
            // Can't get it as a component, must in the tuner, lets just create one for now.
            return getStaticService(context);
        }
        return service;
    }

    private static TunerService getStaticService(Context context) {
        if (sInstance == null) {
            sInstance = new TunerService();
            sInstance.mContext = context.getApplicationContext();
            sInstance.mComponents = new HashMap<>();
            sInstance.start();
        }
        return sInstance;
=======
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    }

    public static final void showResetRequest(final Context context, final Runnable onDisabled) {
        SystemUIDialog dialog = new SystemUIDialog(context);
        dialog.setShowForAllUsers(true);
        dialog.setMessage(R.string.remove_from_settings_prompt);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel),
                (OnClickListener) null);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.guest_exit_guest_dialog_remove), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Tell the tuner (in main SysUI process) to clear all its settings.
                context.sendBroadcast(new Intent(TunerService.ACTION_CLEAR));
                // Disable access to tuner.
                TunerService.setTunerEnabled(context, false);
                // Make them sit through the warning dialog again.
                Settings.Secure.putInt(context.getContentResolver(),
                        TunerFragment.SETTING_SEEN_TUNER_WARNING, 0);
                if (onDisabled != null) {
                    onDisabled.run();
                }
            }
        });
        dialog.show();
    }
}
