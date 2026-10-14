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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.theming.ThemeStyle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class CustomThemeContractTest {
    private static final UUID THEME_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final CustomThemeSeeds LIGHT_SEEDS =
            new CustomThemeSeeds(0xFFB69CFF, 0xFF75D5C7, 0xFFFF91B8, 0xFF8C8791);
    private static final CustomThemeSeeds DARK_SEEDS =
            new CustomThemeSeeds(0xFFCEB4FF, 0xFF8DEDDD, 0xFFFFACD0, 0xFFA5A0AA);

    @Test
    public void parse_nonCustomSource_doesNotInspectCustomObject() throws Exception {
        JSONObject root =
                new JSONObject()
                        .put(CustomThemeContract.KEY_COLOR_SOURCE, "preset")
                        .put(CustomThemeContract.KEY_CUSTOM_THEME, "broken");

        assertEquals(
                CustomThemeContract.Status.NOT_CUSTOM,
                CustomThemeContract.parse(root).getStatus());
    }

    @Test
    public void serializeThenParse_linkedTheme_normalizesRgb() throws Exception {
        CustomThemeContract.ActiveTheme input =
                new CustomThemeContract.ActiveTheme(
                        THEME_ID, ThemeStyle.VIBRANT, false, LIGHT_SEEDS, null);

        JSONObject serialized = CustomThemeContract.serialize(input);
        CustomThemeContract.ParseResult parsed = CustomThemeContract.parse(serialized);

        assertTrue(serialized.toString().contains("B69CFF"));
        assertEquals(CustomThemeContract.Status.VALID, parsed.getStatus());
        assertEquals(THEME_ID, parsed.getTheme().getId());
        assertEquals(LIGHT_SEEDS, parsed.getTheme().getLightSeeds());
        assertNull(parsed.getTheme().getDarkSeeds());
    }

    @Test
    public void serializeThenParse_separateTheme_preservesDarkSeeds() throws Exception {
        CustomThemeContract.ActiveTheme input =
                new CustomThemeContract.ActiveTheme(
                        THEME_ID, ThemeStyle.EXPRESSIVE, true, LIGHT_SEEDS, DARK_SEEDS);

        CustomThemeContract.ParseResult parsed =
                CustomThemeContract.parse(CustomThemeContract.serialize(input));

        assertEquals(CustomThemeContract.Status.VALID, parsed.getStatus());
        assertEquals(DARK_SEEDS, parsed.getTheme().getDarkSeeds());
    }

    @Test
    public void parse_unsupportedVersion_returnsSpecificStatus() throws Exception {
        JSONObject root = validRoot();
        root.getJSONObject(CustomThemeContract.KEY_CUSTOM_THEME)
                .put(CustomThemeContract.KEY_VERSION, 2);

        CustomThemeContract.ParseResult result = CustomThemeContract.parse(root);

        assertEquals(CustomThemeContract.Status.UNSUPPORTED_VERSION, result.getStatus());
        assertEquals("version", result.getReasonCode());
    }

    @Test
    public void parse_missingDarkSeeds_rejectsSeparateTheme() throws Exception {
        JSONObject root = validRoot();
        JSONObject custom = root.getJSONObject(CustomThemeContract.KEY_CUSTOM_THEME);
        custom.put(CustomThemeContract.KEY_SEPARATE_DARK, true);

        assertEquals(
                CustomThemeContract.Status.INVALID_LINKAGE,
                CustomThemeContract.parse(root).getStatus());
    }

    @Test
    public void parse_monochromaticStyle_isRejected() throws Exception {
        JSONObject root = validRoot();
        root.put(CustomThemeContract.KEY_THEME_STYLE, "MONOCHROMATIC");

        assertEquals(
                CustomThemeContract.Status.INVALID_STYLE,
                CustomThemeContract.parse(root).getStatus());
    }

    @Test
    public void parse_lowercaseRgb_acceptsAndNormalizes() throws Exception {
        JSONObject root = validRoot();
        root.getJSONObject(CustomThemeContract.KEY_CUSTOM_THEME)
                .getJSONObject(CustomThemeContract.KEY_LIGHT)
                .put(CustomThemeContract.KEY_PRIMARY, "b69cff");

        CustomThemeContract.ParseResult result = CustomThemeContract.parse(root);

        assertEquals(CustomThemeContract.Status.VALID, result.getStatus());
        assertEquals(
                "B69CFF",
                CustomThemeContract.formatRgb(result.getTheme().getLightSeeds().getPrimary()));
    }

    @Test
    public void parse_unknownFields_areIgnored() throws Exception {
        JSONObject root = validRoot().put("future_root", true);
        root.getJSONObject(CustomThemeContract.KEY_CUSTOM_THEME).put("future_custom", 12);

        assertEquals(
                CustomThemeContract.Status.VALID,
                CustomThemeContract.parse(root).getStatus());
    }

    private static JSONObject validRoot() {
        return CustomThemeContract.serialize(
                new CustomThemeContract.ActiveTheme(
                        THEME_ID, ThemeStyle.VIBRANT, false, LIGHT_SEEDS, null));
    }
}
