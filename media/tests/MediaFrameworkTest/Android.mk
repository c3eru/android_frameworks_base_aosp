LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES := android.test.runner

<<<<<<< HEAD
LOCAL_STATIC_JAVA_LIBRARIES := easymocklib \
    mockito-target \
=======
LOCAL_STATIC_JAVA_LIBRARIES := \
    mockito-target-minus-junit4 \
>>>>>>> d75294d8e45e97f3c4a978cbc1986896174c6040
    android-support-test \
    android-ex-camera2 \
    legacy-android-test

LOCAL_PACKAGE_NAME := mediaframeworktest

include $(BUILD_PACKAGE)
