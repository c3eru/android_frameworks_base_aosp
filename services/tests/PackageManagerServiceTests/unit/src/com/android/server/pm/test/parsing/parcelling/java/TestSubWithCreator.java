/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.pm.test.parsing.parcelling.java;

import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TestSubWithCreator extends TestSuperClass {

    @NonNull
    public static final Parcelable.Creator<TestSubWithCreator> CREATOR =
            new Parcelable.Creator<TestSubWithCreator>() {
                @Override
                public TestSubWithCreator createFromParcel(Parcel source) {
                    return new TestSubWithCreator(source);
                }

                @Override
                public TestSubWithCreator[] newArray(int size) {
                    return new TestSubWithCreator[size];
                }
            };

    @Nullable
    private String subString;

    public TestSubWithCreator() {
    }

    public TestSubWithCreator(@NonNull Parcel in) {
        super(in);
        subString = in.readString();
    }

    public void writeSubToParcel(@NonNull Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(subString);
    }
}
