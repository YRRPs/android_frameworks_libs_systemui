/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.android.systemui.monet;

import android.annotation.ColorInt;
import android.graphics.Color;

import java.util.Objects;

/** Four opaque seed colors used to generate a custom system palette. */
public final class CustomThemeSeeds {
    @ColorInt private final int mPrimary;
    @ColorInt private final int mSecondary;
    @ColorInt private final int mTertiary;
    @ColorInt private final int mNeutral;

    public CustomThemeSeeds(
            @ColorInt int primary,
            @ColorInt int secondary,
            @ColorInt int tertiary,
            @ColorInt int neutral) {
        requireOpaque(primary, "primary");
        requireOpaque(secondary, "secondary");
        requireOpaque(tertiary, "tertiary");
        requireOpaque(neutral, "neutral");
        mPrimary = primary;
        mSecondary = secondary;
        mTertiary = tertiary;
        mNeutral = neutral;
    }

    @ColorInt
    public int getPrimary() {
        return mPrimary;
    }

    @ColorInt
    public int getSecondary() {
        return mSecondary;
    }

    @ColorInt
    public int getTertiary() {
        return mTertiary;
    }

    @ColorInt
    public int getNeutral() {
        return mNeutral;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomThemeSeeds)) {
            return false;
        }
        CustomThemeSeeds that = (CustomThemeSeeds) other;
        return mPrimary == that.mPrimary
                && mSecondary == that.mSecondary
                && mTertiary == that.mTertiary
                && mNeutral == that.mNeutral;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPrimary, mSecondary, mTertiary, mNeutral);
    }

    private static void requireOpaque(@ColorInt int color, String family) {
        if (Color.alpha(color) != 0xFF) {
            throw new IllegalArgumentException(family + " seed must be opaque");
        }
    }
}
