/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2015 The CyanogenMod Project
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

#define LOG_TAG "LightsService"

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#include <android/hardware/light/2.0/ILight.h>
#include <android/hardware/light/2.0/types.h>
#include <utils/misc.h>
#include <utils/Log.h>
#include <map>
#include <stdio.h>

namespace android {

<<<<<<< HEAD
// These values must correspond with the LIGHT_ID constants in
// LightsService.java
enum {
    LIGHT_INDEX_BACKLIGHT = 0,
    LIGHT_INDEX_KEYBOARD = 1,
    LIGHT_INDEX_BUTTONS = 2,
    LIGHT_INDEX_BATTERY = 3,
    LIGHT_INDEX_NOTIFICATIONS = 4,
    LIGHT_INDEX_ATTENTION = 5,
    LIGHT_INDEX_BLUETOOTH = 6,
    LIGHT_INDEX_WIFI = 7,
    LIGHT_INDEX_CAPS = 8,
    LIGHT_INDEX_FUNC = 9,
    LIGHT_COUNT
=======
using Brightness = ::android::hardware::light::V2_0::Brightness;
using Flash      = ::android::hardware::light::V2_0::Flash;
using ILight     = ::android::hardware::light::V2_0::ILight;
using LightState = ::android::hardware::light::V2_0::LightState;
using Status     = ::android::hardware::light::V2_0::Status;
using Type       = ::android::hardware::light::V2_0::Type;
template<typename T>
using Return     = ::android::hardware::Return<T>;

class LightHal {
private:
    static sp<ILight> sLight;
    static bool sLightInit;

    LightHal() {}

public:
    static void disassociate() {
        sLightInit = false;
        sLight = nullptr;
    }

    static sp<ILight> associate() {
        if ((sLight == nullptr && !sLightInit) ||
                (sLight != nullptr && !sLight->ping().isOk())) {
            // will return the hal if it exists the first time.
            sLight = ILight::getService();
            sLightInit = true;

            if (sLight == nullptr) {
                ALOGE("Unable to get ILight interface.");
            }
        }

        return sLight;
    }
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
};

sp<ILight> LightHal::sLight = nullptr;
bool LightHal::sLightInit = false;

static bool validate(jint light, jint flash, jint brightness) {
    bool valid = true;

    if (light < 0 || light >= static_cast<jint>(Type::COUNT)) {
        ALOGE("Invalid light parameter %d.", light);
        valid = false;
    }

    if (flash != static_cast<jint>(Flash::NONE) &&
        flash != static_cast<jint>(Flash::TIMED) &&
        flash != static_cast<jint>(Flash::HARDWARE)) {
        ALOGE("Invalid flash parameter %d.", flash);
        valid = false;
    }

    if (brightness != static_cast<jint>(Brightness::USER) &&
        brightness != static_cast<jint>(Brightness::SENSOR) &&
        brightness != static_cast<jint>(Brightness::LOW_PERSISTENCE)) {
        ALOGE("Invalid brightness parameter %d.", brightness);
        valid = false;
    }

    if (brightness == static_cast<jint>(Brightness::LOW_PERSISTENCE) &&
        light != static_cast<jint>(Type::BACKLIGHT)) {
        ALOGE("Cannot set low-persistence mode for non-backlight device.");
        valid = false;
    }

    return valid;
}

static LightState constructState(
        jint colorARGB,
        jint flashMode,
        jint onMS,
        jint offMS,
        jint brightnessMode){
    Flash flash = static_cast<Flash>(flashMode);
    Brightness brightness = static_cast<Brightness>(brightnessMode);

<<<<<<< HEAD
    err = hw_get_module(LIGHTS_HARDWARE_MODULE_ID, (hw_module_t const**)&module);
    if (err == 0) {
        devices->lights[LIGHT_INDEX_BACKLIGHT]
                = get_device(module, LIGHT_ID_BACKLIGHT);
        devices->lights[LIGHT_INDEX_KEYBOARD]
                = get_device(module, LIGHT_ID_KEYBOARD);
        devices->lights[LIGHT_INDEX_BUTTONS]
                = get_device(module, LIGHT_ID_BUTTONS);
        devices->lights[LIGHT_INDEX_BATTERY]
                = get_device(module, LIGHT_ID_BATTERY);
        devices->lights[LIGHT_INDEX_NOTIFICATIONS]
                = get_device(module, LIGHT_ID_NOTIFICATIONS);
        devices->lights[LIGHT_INDEX_ATTENTION]
                = get_device(module, LIGHT_ID_ATTENTION);
        devices->lights[LIGHT_INDEX_BLUETOOTH]
                = get_device(module, LIGHT_ID_BLUETOOTH);
        devices->lights[LIGHT_INDEX_WIFI]
                = get_device(module, LIGHT_ID_WIFI);
        devices->lights[LIGHT_INDEX_CAPS]
                = get_device(module, LIGHT_ID_CAPS);
        devices->lights[LIGHT_INDEX_FUNC]
                = get_device(module, LIGHT_ID_FUNC);
    } else {
        memset(devices, 0, sizeof(Devices));
    }

    return (jlong)devices;
}

static void finalize_native(JNIEnv* /* env */, jobject /* clazz */, jlong ptr)
{
    Devices* devices = (Devices*)ptr;
    if (devices == NULL) {
        return;
    }

    free(devices);
}

static void setLight_native(JNIEnv* /* env */, jobject /* clazz */, jlong ptr,
        jint light, jint colorARGB, jint flashMode, jint onMS, jint offMS, jint brightnessMode,
        jint brightnessLevel, jint multipleLeds)
{
    Devices* devices = (Devices*)ptr;
    light_state_t state;
    int colorAlpha;

    if (light < 0 || light >= LIGHT_COUNT || devices->lights[light] == NULL) {
        return ;
    }

    uint32_t version = devices->lights[light]->common.version;

    if (brightnessLevel > 0 && brightnessLevel <= 0xFF) {
        colorAlpha = (colorARGB & 0xFF000000) >> 24;
        if (colorAlpha == 0x00) {
            colorAlpha = 0xFF;
        }
        colorAlpha = (colorAlpha * brightnessLevel) / 0xFF;
        if (colorAlpha < 1) {
            colorAlpha = 1;
        }
        colorARGB = (colorAlpha << 24) + (colorARGB & 0x00FFFFFF);
    }

    memset(&state, 0, sizeof(light_state_t));

    if (brightnessMode == BRIGHTNESS_MODE_LOW_PERSISTENCE) {
        if (light != LIGHT_INDEX_BACKLIGHT) {
            ALOGE("Cannot set low-persistence mode for non-backlight device.");
            return;
        }
        if (version < LIGHTS_DEVICE_API_VERSION_2_0) {
            // HAL impl has not been upgraded to support this.
            return;
        }
=======
    LightState state{};

    if (brightness == Brightness::LOW_PERSISTENCE) {
        state.flashMode = Flash::NONE;
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    } else {
        // Only set non-brightness settings when not in low-persistence mode
        state.flashMode = flash;
        state.flashOnMs = onMS;
        state.flashOffMs = offMS;
    }

    state.color = colorARGB;
<<<<<<< HEAD
    state.brightnessMode = brightnessMode;
    state.ledsModes = 0 |
                      (multipleLeds ? LIGHT_MODE_MULTIPLE_LEDS : 0);
=======
    state.brightnessMode = brightness;

    return state;
}

static void processReturn(
        const Return<Status> &ret,
        Type type,
        const LightState &state) {
    if (!ret.isOk()) {
        ALOGE("Failed to issue set light command.");
        LightHal::disassociate();
        return;
    }

    switch (static_cast<Status>(ret)) {
        case Status::SUCCESS:
            break;
        case Status::LIGHT_NOT_SUPPORTED:
            ALOGE("Light requested not available on this device. %d", type);
            break;
        case Status::BRIGHTNESS_NOT_SUPPORTED:
            ALOGE("Brightness parameter not supported on this device: %d",
                state.brightnessMode);
            break;
        case Status::UNKNOWN:
        default:
            ALOGE("Unknown error setting light.");
    }
}

static void setLight_native(
        JNIEnv* /* env */,
        jobject /* clazz */,
        jint light,
        jint colorARGB,
        jint flashMode,
        jint onMS,
        jint offMS,
        jint brightnessMode,
        jint brightnessLevel) {

    if (!validate(light, flashMode, brightnessMode)) {
        return;
    }

    sp<ILight> hal = LightHal::associate();

    if (hal == nullptr) {
        return;
    }

    if (brightnessLevel > 0 && brightnessLevel <= 0xFF) {
        int colorAlpha = (colorARGB & 0xFF000000) >> 24;
        if (colorAlpha == 0x00) {
            colorAlpha = 0xFF;
        }
        colorAlpha = (colorAlpha * brightnessLevel) / 0xFF;
        colorARGB = (colorAlpha << 24) + (colorARGB & 0x00FFFFFF);
    }

    Type type = static_cast<Type>(light);
    LightState state = constructState(
        colorARGB, flashMode, onMS, offMS, brightnessMode);
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040

    {
        ALOGD_IF_SLOW(50, "Excessive delay setting light");
        Return<Status> ret = hal->setLight(type, state);
        processReturn(ret, type, state);
    }
}

static const JNINativeMethod method_table[] = {
<<<<<<< HEAD
    { "init_native", "()J", (void*)init_native },
    { "finalize_native", "(J)V", (void*)finalize_native },
    { "setLight_native", "(JIIIIIIII)V", (void*)setLight_native },
=======
    { "setLight_native", "(IIIIIII)V", (void*)setLight_native },
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
};

int register_android_server_LightsService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/android/server/lights/LightsService",
            method_table, NELEM(method_table));
}

};
