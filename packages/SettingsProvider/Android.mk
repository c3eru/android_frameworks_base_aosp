LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/android/providers/settings/EventLogTags.logtags

LOCAL_JAVA_LIBRARIES := telephony-common ims-common
<<<<<<< HEAD
LOCAL_STATIC_JAVA_LIBRARIES := org.cyanogenmod.platform.sdk
=======
LOCAL_STATIC_JAVA_LIBRARIES := junit legacy-android-test \
    org.lineageos.platform.internal
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040

LOCAL_PACKAGE_NAME := SettingsProvider
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

########################
include $(call all-makefiles-under,$(LOCAL_PATH))
