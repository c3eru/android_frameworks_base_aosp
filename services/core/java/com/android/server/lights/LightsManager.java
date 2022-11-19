/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.server.lights;

import android.hardware.light.V2_0.Type;

public abstract class LightsManager {
<<<<<<< HEAD
    public static final int LIGHT_ID_BACKLIGHT = 0;
    public static final int LIGHT_ID_KEYBOARD = 1;
    public static final int LIGHT_ID_BUTTONS = 2;
    public static final int LIGHT_ID_BATTERY = 3;
    public static final int LIGHT_ID_NOTIFICATIONS = 4;
    public static final int LIGHT_ID_ATTENTION = 5;
    public static final int LIGHT_ID_BLUETOOTH = 6;
    public static final int LIGHT_ID_WIFI = 7;
    public static final int LIGHT_ID_CAPS = 8;
    public static final int LIGHT_ID_FUNC = 9;
    public static final int LIGHT_ID_COUNT = 10;
=======
    public static final int LIGHT_ID_BACKLIGHT = Type.BACKLIGHT;
    public static final int LIGHT_ID_KEYBOARD = Type.KEYBOARD;
    public static final int LIGHT_ID_BUTTONS = Type.BUTTONS;
    public static final int LIGHT_ID_BATTERY = Type.BATTERY;
    public static final int LIGHT_ID_NOTIFICATIONS = Type.NOTIFICATIONS;
    public static final int LIGHT_ID_ATTENTION = Type.ATTENTION;
    public static final int LIGHT_ID_BLUETOOTH = Type.BLUETOOTH;
    public static final int LIGHT_ID_WIFI = Type.WIFI;
    public static final int LIGHT_ID_COUNT = Type.COUNT;
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040

    public abstract Light getLight(int id);
}
