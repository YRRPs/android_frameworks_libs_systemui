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

import android.util.Pair;

import com.google.ux.material.libmonet.dynamiccolor.DynamicColor;
import com.google.ux.material.libmonet.dynamiccolor.DynamicScheme;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable resource-color snapshot generated for a custom theme. */
public final class CustomThemePalette {
    private final DynamicScheme mLightScheme;
    private final DynamicScheme mDarkScheme;
    private final Map<String, Integer> mLightNeutralColors;
    private final Map<String, Integer> mDarkNeutralColors;
    private final Map<String, Integer> mLightAccentColors;
    private final Map<String, Integer> mDarkAccentColors;
    private final Map<String, Integer> mLightErrorColors;
    private final Map<String, Integer> mDarkErrorColors;
    private final Map<String, Integer> mLightDynamicColors;
    private final Map<String, Integer> mDarkDynamicColors;
    private final Map<String, Integer> mFixedDynamicColors;
    private final Map<String, Integer> mLightCustomColors;
    private final Map<String, Integer> mDarkCustomColors;

    CustomThemePalette(DynamicScheme lightScheme, DynamicScheme darkScheme) {
        mLightScheme = lightScheme;
        mDarkScheme = darkScheme;
        mLightNeutralColors = resolve(DynamicColors.getAllNeutralPalette(), lightScheme);
        mDarkNeutralColors = resolve(DynamicColors.getAllNeutralPalette(), darkScheme);
        mLightAccentColors = resolve(DynamicColors.getAllAccentPalette(), lightScheme);
        mDarkAccentColors = resolve(DynamicColors.getAllAccentPalette(), darkScheme);
        mLightErrorColors = resolve(DynamicColors.getAllErrorPalette(), lightScheme);
        mDarkErrorColors = resolve(DynamicColors.getAllErrorPalette(), darkScheme);
        mLightDynamicColors = resolve(DynamicColors.getAllDynamicColorsMapped(), lightScheme);
        mDarkDynamicColors = resolve(DynamicColors.getAllDynamicColorsMapped(), darkScheme);
        mFixedDynamicColors = resolve(DynamicColors.getFixedColorsMapped(), lightScheme);
        mLightCustomColors = resolve(DynamicColors.getCustomColorsMapped(), lightScheme);
        mDarkCustomColors = resolve(DynamicColors.getCustomColorsMapped(), darkScheme);
    }

    public DynamicScheme getLightScheme() {
        return mLightScheme;
    }

    public DynamicScheme getDarkScheme() {
        return mDarkScheme;
    }

    public Map<String, Integer> getLightNeutralColors() {
        return mLightNeutralColors;
    }

    public Map<String, Integer> getDarkNeutralColors() {
        return mDarkNeutralColors;
    }

    public Map<String, Integer> getLightAccentColors() {
        return mLightAccentColors;
    }

    public Map<String, Integer> getDarkAccentColors() {
        return mDarkAccentColors;
    }

    public Map<String, Integer> getLightErrorColors() {
        return mLightErrorColors;
    }

    public Map<String, Integer> getDarkErrorColors() {
        return mDarkErrorColors;
    }

    public Map<String, Integer> getLightDynamicColors() {
        return mLightDynamicColors;
    }

    public Map<String, Integer> getDarkDynamicColors() {
        return mDarkDynamicColors;
    }

    public Map<String, Integer> getFixedDynamicColors() {
        return mFixedDynamicColors;
    }

    public Map<String, Integer> getLightCustomColors() {
        return mLightCustomColors;
    }

    public Map<String, Integer> getDarkCustomColors() {
        return mDarkCustomColors;
    }

    private static Map<String, Integer> resolve(
            List<Pair<String, DynamicColor>> colors, DynamicScheme scheme) {
        LinkedHashMap<String, Integer> resolved = new LinkedHashMap<>();
        for (Pair<String, DynamicColor> color : colors) {
            resolved.put(color.first, color.second.getArgb(scheme));
        }
        return Collections.unmodifiableMap(resolved);
    }
}
