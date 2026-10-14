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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

import android.content.theming.ThemeStyle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class CustomThemePaletteGeneratorTest {
    private static final CustomThemeSeeds BASE =
            new CustomThemeSeeds(0xFFB69CFF, 0xFF75D5C7, 0xFFFF91B8, 0xFF8C8791);
    private static final CustomThemeSeeds DARK =
            new CustomThemeSeeds(0xFFCEB4FF, 0xFF8DEDDD, 0xFFFFACD0, 0xFFA5A0AA);

    @Test
    public void generate_sameInputs_returnsSameMaps() {
        CustomThemePalette first = generate(BASE);
        CustomThemePalette second = generate(BASE);

        assertEquals(first.getLightAccentColors(), second.getLightAccentColors());
        assertEquals(first.getDarkDynamicColors(), second.getDarkDynamicColors());
    }

    @Test
    public void changingPrimary_changesOnlyPrimaryPaletteFamily() {
        CustomThemePalette original = generate(BASE);
        CustomThemePalette changed =
                generate(
                        new CustomThemeSeeds(
                                0xFFFF0000,
                                BASE.getSecondary(),
                                BASE.getTertiary(),
                                BASE.getNeutral()));

        assertNotEquals(
                original.getLightAccentColors().get("accent1_500"),
                changed.getLightAccentColors().get("accent1_500"));
        assertEquals(
                entriesWithPrefix(original.getLightAccentColors(), "accent2_"),
                entriesWithPrefix(changed.getLightAccentColors(), "accent2_"));
        assertEquals(
                entriesWithPrefix(original.getLightAccentColors(), "accent3_"),
                entriesWithPrefix(changed.getLightAccentColors(), "accent3_"));
        assertEquals(original.getLightNeutralColors(), changed.getLightNeutralColors());
        assertEquals(original.getLightErrorColors(), changed.getLightErrorColors());
    }

    @Test
    public void separateDark_usesIndependentDarkSeeds() {
        CustomThemePalette linked = generate(BASE);
        CustomThemePalette separate =
                CustomThemePaletteGenerator.generate(
                        BASE, DARK, true, ThemeStyle.VIBRANT, 0.0);

        assertEquals(linked.getLightDynamicColors(), separate.getLightDynamicColors());
        assertNotEquals(linked.getDarkDynamicColors(), separate.getDarkDynamicColors());
    }

    @Test
    public void fixedErrorPalette_isIndependentOfSeeds() {
        CustomThemePalette first = generate(BASE);
        CustomThemePalette second =
                generate(
                        new CustomThemeSeeds(
                                0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF222222));

        assertEquals(first.getLightErrorColors(), second.getLightErrorColors());
        assertEquals(first.getDarkErrorColors(), second.getDarkErrorColors());
    }

    @Test
    public void separateGenerations_doNotShareErrorPaletteCache() {
        CustomThemePalette first = generate(BASE);
        CustomThemePalette second = generate(BASE);

        assertNotSame(first.getLightScheme().errorPalette, second.getLightScheme().errorPalette);
    }

    @Test
    public void generatedMaps_areUnmodifiable() {
        CustomThemePalette palette = generate(BASE);

        assertThrows(
                UnsupportedOperationException.class,
                () -> palette.getLightAccentColors().put("accent1_500", 0));
    }

    @Test
    public void nullLightSeeds_areRejectedAtBoundary() {
        assertThrows(
                NullPointerException.class,
                () ->
                        CustomThemePaletteGenerator.generate(
                                null, null, false, ThemeStyle.VIBRANT, 0.0));
    }

    @Test
    public void monochromaticStyle_isRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CustomThemePaletteGenerator.generate(
                                BASE, null, false, ThemeStyle.MONOCHROMATIC, 0.0));
    }

    @Test
    public void contrastOutsideSupportedRange_isRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CustomThemePaletteGenerator.generate(
                                BASE, null, false, ThemeStyle.VIBRANT, 1.1));
    }

    @Test
    public void supportedStyles_generateNonEmptyRoles() {
        int[] styles = {
            ThemeStyle.TONAL_SPOT,
            ThemeStyle.VIBRANT,
            ThemeStyle.EXPRESSIVE,
            ThemeStyle.SPRITZ,
            ThemeStyle.RAINBOW,
            ThemeStyle.FRUIT_SALAD
        };

        for (int style : styles) {
            CustomThemePalette palette =
                    CustomThemePaletteGenerator.generate(BASE, null, false, style, 0.0);
            assertFalse(palette.getLightDynamicColors().isEmpty());
        }
    }

    private static CustomThemePalette generate(CustomThemeSeeds seeds) {
        return CustomThemePaletteGenerator.generate(
                seeds, null, false, ThemeStyle.VIBRANT, 0.0);
    }

    private static Map<String, Integer> entriesWithPrefix(
            Map<String, Integer> colors, String prefix) {
        return colors.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue));
    }
}
