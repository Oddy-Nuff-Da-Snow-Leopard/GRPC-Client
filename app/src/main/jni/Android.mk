# my-dir represents the current folder, generally without modification
LOCAL_PATH :=$(call my-dir)
# This configuration is required, but no modifications are required.
include $(CLEAR_VARS)
# hello is the name of so.
# If it does not start with lib,
# the compiled so file name will be named with lib + file name as so name.
LOCAL_MODULE := id_getter
# Specify the c++/c file to be compiled
LOCAL_SRC_FILES := id_getter.cpp
# This does not need to be modified.
include $(BUILD_SHARED_LIBRARY)