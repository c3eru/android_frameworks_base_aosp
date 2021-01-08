/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.location.timezone;

import static android.service.timezone.TimeZoneProviderService.TEST_COMMAND_RESULT_ERROR_KEY;
import static android.service.timezone.TimeZoneProviderService.TEST_COMMAND_RESULT_SUCCESS_KEY;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteCallback;
import android.os.SystemClock;
import android.service.timezone.TimeZoneProviderSuggestion;
import android.util.IndentingPrintWriter;

import com.android.internal.annotations.GuardedBy;
import com.android.server.timezonedetector.ReferenceWithHistory;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Objects;

/**
 * A replacement for a real binder proxy for use during integration testing
 * that can be used to inject simulated {@link LocationTimeZoneProviderProxy} behavior.
 */
class SimulatedLocationTimeZoneProviderProxy extends LocationTimeZoneProviderProxy {

    private static final String TEST_COMMAND_NAME_ON_BIND = "on_bind";
    private static final String TEST_COMMAND_NAME_ON_UNBIND = "on_unbind";
    private static final String TEST_COMMAND_NAME_PERM_FAIL = "perm_fail";
    private static final String TEST_COMMAND_NAME_UNCERTAIN = "uncertain";
    private static final String TEST_COMMAND_NAME_SUCCESS = "success";

    /**
     * Used for {@link #TEST_COMMAND_NAME_SUCCESS}.
     */
    private static final String KEY_TIME_ZONE = "tz";

    @GuardedBy("mSharedLock")
    @NonNull private TimeZoneProviderRequest mRequest;

    @GuardedBy("mSharedLock")
    @NonNull private final ReferenceWithHistory<String> mLastEvent = new ReferenceWithHistory<>(50);

    SimulatedLocationTimeZoneProviderProxy(
            @NonNull Context context, @NonNull ThreadingDomain threadingDomain) {
        super(context, threadingDomain);
        mRequest = TimeZoneProviderRequest.createStopUpdatesRequest();
    }

    @Override
    void onInitialize() {
        // No-op - nothing to do for the simulated provider.
    }

    void handleTestCommand(@NonNull TestCommand testCommand, @Nullable RemoteCallback callback) {
        mThreadingDomain.assertCurrentThread();

        Objects.requireNonNull(testCommand);

        synchronized (mSharedLock) {
            Bundle resultBundle = new Bundle();
            switch (testCommand.getName()) {
                case TEST_COMMAND_NAME_ON_BIND: {
                    mLastEvent.set("Simulating onProviderBound(), testCommand=" + testCommand);
                    mThreadingDomain.post(this::onBindOnHandlerThread);
                    resultBundle.putBoolean(TEST_COMMAND_RESULT_SUCCESS_KEY, true);
                    break;
                }
                case TEST_COMMAND_NAME_ON_UNBIND: {
                    mLastEvent.set("Simulating onProviderUnbound(), testCommand=" + testCommand);
                    mThreadingDomain.post(this::onUnbindOnHandlerThread);
                    resultBundle.putBoolean(TEST_COMMAND_RESULT_SUCCESS_KEY, true);
                    break;
                }
                case TEST_COMMAND_NAME_PERM_FAIL:
                case TEST_COMMAND_NAME_UNCERTAIN:
                case TEST_COMMAND_NAME_SUCCESS: {
                    if (!mRequest.sendUpdates()) {
                        String errorMsg = "testCommand=" + testCommand
                                + " is testing an invalid case:"
                                + " updates are off. mRequest=" + mRequest;
                        mLastEvent.set(errorMsg);
                        resultBundle.putBoolean(TEST_COMMAND_RESULT_SUCCESS_KEY, false);
                        resultBundle.putString(TEST_COMMAND_RESULT_ERROR_KEY, errorMsg);
                        break;
                    }
                    mLastEvent.set("Simulating TimeZoneProviderEvent, testCommand=" + testCommand);
                    TimeZoneProviderEvent timeZoneProviderEvent =
                            createTimeZoneProviderEventFromTestCommand(testCommand);
                    handleTimeZoneProviderEvent(timeZoneProviderEvent);
                    resultBundle.putBoolean(TEST_COMMAND_RESULT_SUCCESS_KEY, true);
                    break;
                }
                default: {
                    String errorMsg = "Unknown test event type. testCommand=" + testCommand;
                    mLastEvent.set(errorMsg);
                    resultBundle.putBoolean(TEST_COMMAND_RESULT_SUCCESS_KEY, false);
                    resultBundle.putString(TEST_COMMAND_RESULT_ERROR_KEY, errorMsg);
                    break;
                }
            }
            if (callback != null) {
                callback.sendResult(resultBundle);
            }
        }
    }

    private void onBindOnHandlerThread() {
        mThreadingDomain.assertCurrentThread();

        synchronized (mSharedLock) {
            mListener.onProviderBound();
        }
    }

    private void onUnbindOnHandlerThread() {
        mThreadingDomain.assertCurrentThread();

        synchronized (mSharedLock) {
            mListener.onProviderUnbound();
        }
    }

    @Override
    final void setRequest(@NonNull TimeZoneProviderRequest request) {
        mThreadingDomain.assertCurrentThread();

        Objects.requireNonNull(request);
        synchronized (mSharedLock) {
            mLastEvent.set("Request received: " + request);
            mRequest = request;
        }
    }

    @Override
    public void dump(@NonNull IndentingPrintWriter ipw, @Nullable String[] args) {
        synchronized (mSharedLock) {
            ipw.println("mRequest=" + mRequest);
            ipw.println("mLastEvent=" + mLastEvent);

            ipw.increaseIndent();
            ipw.println("Last event history:");
            mLastEvent.dump(ipw);
            ipw.decreaseIndent();
        }
    }

    /**
     * Prints the command line options that to create a {@link TestCommand} that can be passed to
     * {@link #createTimeZoneProviderEventFromTestCommand(TestCommand)}.
     */
    static void printTestCommandShellHelp(@NonNull PrintWriter pw) {
        pw.printf("%s\n", TEST_COMMAND_NAME_ON_BIND);
        pw.printf("%s\n", TEST_COMMAND_NAME_ON_UNBIND);
        pw.printf("%s\n", TEST_COMMAND_NAME_PERM_FAIL);
        pw.printf("%s\n", TEST_COMMAND_NAME_UNCERTAIN);
        pw.printf("%s %s=string_array:<time zone id>[&<time zone id>]+\n",
                TEST_COMMAND_NAME_SUCCESS, KEY_TIME_ZONE);
    }

    @NonNull
    private static TimeZoneProviderEvent createTimeZoneProviderEventFromTestCommand(
            @NonNull TestCommand testCommand) {
        String name = testCommand.getName();
        switch (name) {
            case TEST_COMMAND_NAME_PERM_FAIL: {
                return TimeZoneProviderEvent.createPermanentFailureEvent("Simulated failure");
            }
            case TEST_COMMAND_NAME_UNCERTAIN: {
                return TimeZoneProviderEvent.createUncertainEvent();
            }
            case TEST_COMMAND_NAME_SUCCESS: {
                Bundle args = testCommand.getArgs();
                String[] timeZoneIds = args.getStringArray(KEY_TIME_ZONE);
                if (timeZoneIds == null) {
                    throw new IllegalArgumentException("No " + KEY_TIME_ZONE + " arg found");
                }
                TimeZoneProviderSuggestion suggestion = new TimeZoneProviderSuggestion.Builder()
                        .setTimeZoneIds(Arrays.asList(timeZoneIds))
                        .setElapsedRealtimeMillis(SystemClock.elapsedRealtime())
                        .build();
                return TimeZoneProviderEvent.createSuggestionEvent(suggestion);
            }
            default: {
                String msg = String.format("Error: Unknown command name %s", name);
                throw new IllegalArgumentException(msg);
            }
        }
    }
}
