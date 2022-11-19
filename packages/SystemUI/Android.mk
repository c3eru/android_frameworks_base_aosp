LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := SystemUI-proto

LOCAL_SRC_FILES := $(call all-proto-files-under,src)

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTO_JAVA_OUTPUT_PARAMS := optional_field_style=accessors

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := SystemUI-tags

LOCAL_SRC_FILES := src/com/android/systemui/EventLogTags.logtags

include $(BUILD_STATIC_JAVA_LIBRARY)

# ------------------

include $(CLEAR_VARS)

LOCAL_USE_AAPT2 := true

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-Iaidl-files-under, src)

LOCAL_STATIC_ANDROID_LIBRARIES := \
    SystemUIPluginLib \
    android-support-v4 \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v7-mediarouter \
    android-support-v7-palette \
    android-support-v14-preference \
<<<<<<< HEAD
    android-support-v17-leanback \
    android-support-v7-palette \
    android-support-v4 \
    framework-protos \
    SystemUI-proto-tags \
    org.cyanogenmod.platform.internal \
    uicommon

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_FULL_LIBS_MANIFEST_FILES := $(LOCAL_PATH)/AndroidManifest_cm.xml
=======
    android-support-v17-leanback

LOCAL_STATIC_JAVA_LIBRARIES := \
    SystemUI-tags \
    SystemUI-proto \
    org.lineageos.platform.internal

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += android.car
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040

LOCAL_PACKAGE_NAME := SystemUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res-keyguard $(LOCAL_PATH)/res

<<<<<<< HEAD
LOCAL_RESOURCE_DIR := \
    frameworks/base/packages/Keyguard/res \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/v17/leanback/res

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.android.keyguard:android.support.v7.recyclerview:android.support.v7.preference:android.support.v14.preference:android.support.v7.appcompat \
	--extra-packages android.support.v17.leanback

ifneq ($(SYSTEM_UI_INCREMENTAL_BUILDS),)
=======
ifneq ($(INCREMENTAL_BUILDS),)
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
    LOCAL_DX_FLAGS := --multi-dex
    LOCAL_JACK_FLAGS := --multi-dex native
endif

include frameworks/base/packages/SettingsLib/common.mk

LOCAL_AAPT_FLAGS := --extra-packages com.android.keyguard

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
