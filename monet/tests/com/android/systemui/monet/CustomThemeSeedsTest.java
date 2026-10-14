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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public final class CustomThemeSeedsTest {
    private static final int PRIMARY = 0xFFB69CFF;
    private static final int SECONDARY = 0xFF75D5C7;
    private static final int TERTIARY = 0xFFFF91B8;
    private static final int NEUTRAL = 0xFF8C8791;

    @Test
    public void constructor_rejectsTransparentSeed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CustomThemeSeeds(0x00FF0000, SECONDARY, TERTIARY, NEUTRAL));
    }

    @Test
    public void valueSemantics_includeAllFamilies() {
        CustomThemeSeeds first =
                new CustomThemeSeeds(PRIMARY, SECONDARY, TERTIARY, NEUTRAL);
        CustomThemeSeeds second =
                new CustomThemeSeeds(PRIMARY, SECONDARY, TERTIARY, NEUTRAL);
        CustomThemeSeeds different =
                new CustomThemeSeeds(0xFFFF0000, SECONDARY, TERTIARY, NEUTRAL);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    public void getters_returnAllFamilies() {
        CustomThemeSeeds seeds =
                new CustomThemeSeeds(PRIMARY, SECONDARY, TERTIARY, NEUTRAL);

        assertEquals(PRIMARY, seeds.getPrimary());
        assertEquals(SECONDARY, seeds.getSecondary());
        assertEquals(TERTIARY, seeds.getTertiary());
        assertEquals(NEUTRAL, seeds.getNeutral());
    }
}
