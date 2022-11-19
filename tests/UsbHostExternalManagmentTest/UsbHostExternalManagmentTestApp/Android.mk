# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

LOCAL_PATH:= $(call my-dir)

##################################################

include $(CLEAR_VARS)

<<<<<<< HEAD:packages/Keyguard/Android.mk
LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-subdir-Iaidl-files)

LOCAL_STATIC_JAVA_LIBRARIES := org.cyanogenmod.platform.sdk

LOCAL_MODULE := Keyguard

LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES := SettingsLib

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
=======
LOCAL_SRC_FILES := $(call all-java-files-under, src)
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040:tests/UsbHostExternalManagmentTest/UsbHostExternalManagmentTestApp/Android.mk

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := UsbHostExternalManagementTestApp

LOCAL_PRIVILEGED_MODULE := true
# TODO remove tests tag
#LOCAL_MODULE_TAGS := tests
#LOCAL_MODULE_PATH := $(TARGET_OUT_DATA_APPS)

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

