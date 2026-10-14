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

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.theming.ThemeStyle;

import com.google.ux.material.libmonet.dynamiccolor.DynamicScheme;
import com.google.ux.material.libmonet.hct.Hct;
import com.google.ux.material.libmonet.palettes.TonalPalette;
import com.google.ux.material.libmonet.scheme.SchemeExpressive;
import com.google.ux.material.libmonet.scheme.SchemeFruitSalad;
import com.google.ux.material.libmonet.scheme.SchemeNeutral;
import com.google.ux.material.libmonet.scheme.SchemeRainbow;
import com.google.ux.material.libmonet.scheme.SchemeTonalSpot;
import com.google.ux.material.libmonet.scheme.SchemeVibrant;

import java.util.Objects;
import java.util.Optional;

/** Generates complete Material resource maps from four independent seed families. */
public final class CustomThemePaletteGenerator {
    private static final double MIN_CONTRAST = -1.0;
    private static final double MAX_CONTRAST = 1.0;

    private CustomThemePaletteGenerator() {}

    @NonNull
    public static CustomThemePalette generate(
            @NonNull CustomThemeSeeds lightSeeds,
            @Nullable CustomThemeSeeds darkSeeds,
            boolean separateDark,
            @ThemeStyle.Type int style,
            double contrastLevel) {
        Objects.requireNonNull(lightSeeds, "lightSeeds");
        CustomThemeContract.requireSupportedStyle(style);
        requireValidContrast(contrastLevel);
        if (separateDark != (darkSeeds != null)) {
            throw new IllegalArgumentException("Dark seeds must match separate-dark mode");
        }

        DynamicScheme lightScheme = compose(lightSeeds, false, style, contrastLevel);
        CustomThemeSeeds effectiveDarkSeeds = separateDark ? darkSeeds : lightSeeds;
        DynamicScheme darkScheme = compose(effectiveDarkSeeds, true, style, contrastLevel);
        return new CustomThemePalette(lightScheme, darkScheme);
    }

    private static DynamicScheme compose(
            CustomThemeSeeds seeds,
            boolean dark,
            @ThemeStyle.Type int style,
            double contrastLevel) {
        DynamicScheme primary = createScheme(seeds.getPrimary(), dark, style, contrastLevel);
        DynamicScheme secondary = createScheme(seeds.getSecondary(), dark, style, contrastLevel);
        DynamicScheme tertiary = createScheme(seeds.getTertiary(), dark, style, contrastLevel);
        DynamicScheme neutral = createScheme(seeds.getNeutral(), dark, style, contrastLevel);

        return new DynamicScheme(
                primary.sourceColorHct,
                primary.variant,
                dark,
                contrastLevel,
                primary.platform,
                primary.specVersion,
                primary.primaryPalette,
                secondary.secondaryPalette,
                tertiary.tertiaryPalette,
                neutral.neutralPalette,
                neutral.neutralVariantPalette,
                Optional.of(TonalPalette.fromHueAndChroma(25.0, 84.0)));
    }

    private static DynamicScheme createScheme(
            int seed, boolean dark, @ThemeStyle.Type int style, double contrastLevel) {
        Hct source = Hct.fromInt(seed);
        return switch (style) {
            case ThemeStyle.SPRITZ -> new SchemeNeutral(source, dark, contrastLevel);
            case ThemeStyle.TONAL_SPOT -> new SchemeTonalSpot(source, dark, contrastLevel);
            case ThemeStyle.VIBRANT -> new SchemeVibrant(source, dark, contrastLevel);
            case ThemeStyle.EXPRESSIVE -> new SchemeExpressive(source, dark, contrastLevel);
            case ThemeStyle.RAINBOW -> new SchemeRainbow(source, dark, contrastLevel);
            case ThemeStyle.FRUIT_SALAD -> new SchemeFruitSalad(source, dark, contrastLevel);
            default -> throw new IllegalArgumentException("Unsupported custom theme style: " + style);
        };
    }

    private static void requireValidContrast(double contrastLevel) {
        if (!Double.isFinite(contrastLevel)
                || contrastLevel < MIN_CONTRAST
                || contrastLevel > MAX_CONTRAST) {
            throw new IllegalArgumentException("Contrast level must be between -1 and 1");
        }
    }
}
