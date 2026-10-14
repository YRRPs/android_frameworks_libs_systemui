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
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.theming.ThemeStyle;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Codec for the self-contained active custom-theme payload. */
public final class CustomThemeContract {
    public static final int VERSION_1 = 1;
    public static final String COLOR_SOURCE_CUSTOM_THEME = "custom_theme";
    public static final String KEY_COLOR_SOURCE = "android.theme.customization.color_source";
    public static final String KEY_THEME_STYLE = "android.theme.customization.theme_style";
    public static final String KEY_CUSTOM_THEME = "android.theme.customization.custom_theme";
    public static final String KEY_VERSION = "version";
    public static final String KEY_ID = "id";
    public static final String KEY_SEPARATE_DARK = "separate_dark";
    public static final String KEY_LIGHT = "light";
    public static final String KEY_DARK = "dark";
    public static final String KEY_PRIMARY = "primary";
    public static final String KEY_SECONDARY = "secondary";
    public static final String KEY_TERTIARY = "tertiary";
    public static final String KEY_NEUTRAL = "neutral";

    private static final Pattern RGB_PATTERN = Pattern.compile("(?i)[0-9a-f]{6}");
    private static final Set<String> LEGACY_COLOR_KEYS =
            Set.of(
                    "android.theme.customization.system_palette",
                    "android.theme.customization.accent_color",
                    "android.theme.customization.dynamic_color",
                    "android.theme.customization.color_index",
                    "android.theme.customization.color_both");
    private static final Set<Integer> SUPPORTED_STYLES =
            Set.of(
                    ThemeStyle.TONAL_SPOT,
                    ThemeStyle.VIBRANT,
                    ThemeStyle.EXPRESSIVE,
                    ThemeStyle.SPRITZ,
                    ThemeStyle.RAINBOW,
                    ThemeStyle.FRUIT_SALAD);

    private CustomThemeContract() {}

    public enum Status {
        NOT_CUSTOM,
        VALID,
        MALFORMED,
        UNSUPPORTED_VERSION,
        INVALID_ID,
        INVALID_STYLE,
        INVALID_LINKAGE,
        INVALID_SEEDS
    }

    /** Parses a complete theme-customization setting without throwing. */
    @NonNull
    public static ParseResult parse(@Nullable JSONObject root) {
        if (root == null) {
            return ParseResult.error(Status.MALFORMED, "root");
        }
        if (!COLOR_SOURCE_CUSTOM_THEME.equals(root.optString(KEY_COLOR_SOURCE))) {
            return ParseResult.notCustom();
        }

        JSONObject custom = asObject(root.opt(KEY_CUSTOM_THEME));
        if (custom == null) {
            return ParseResult.error(Status.MALFORMED, "object");
        }
        ParseResult versionError = validateVersion(custom);
        if (versionError != null) {
            return versionError;
        }

        UUID id = parseId(custom.opt(KEY_ID));
        if (id == null) {
            return ParseResult.error(Status.INVALID_ID, "id");
        }
        Integer style = parseStyle(root.opt(KEY_THEME_STYLE));
        if (style == null) {
            return ParseResult.error(Status.INVALID_STYLE, "style");
        }
        Object separateValue = custom.opt(KEY_SEPARATE_DARK);
        if (!(separateValue instanceof Boolean)) {
            return ParseResult.error(Status.INVALID_LINKAGE, "separate_dark");
        }
        boolean separateDark = (Boolean) separateValue;

        try {
            CustomThemeSeeds lightSeeds = parseSeeds(asObject(custom.opt(KEY_LIGHT)));
            CustomThemeSeeds darkSeeds = parseDarkSeeds(custom, separateDark);
            return ParseResult.valid(
                    new ActiveTheme(id, style, separateDark, lightSeeds, darkSeeds));
        } catch (LinkageException exception) {
            return ParseResult.error(Status.INVALID_LINKAGE, exception.getMessage());
        } catch (SeedException exception) {
            return ParseResult.error(Status.INVALID_SEEDS, exception.getMessage());
        }
    }

    /** Merges a valid active theme into existing customization state. */
    @NonNull
    public static JSONObject serialize(
            @NonNull ActiveTheme theme, @NonNull JSONObject existingRoot) {
        try {
            JSONObject root = copy(existingRoot);
            for (String key : LEGACY_COLOR_KEYS) {
                root.remove(key);
            }
            JSONObject custom =
                    new JSONObject()
                            .put(KEY_VERSION, VERSION_1)
                            .put(KEY_ID, theme.getId().toString())
                            .put(KEY_SEPARATE_DARK, theme.isSeparateDark())
                            .put(KEY_LIGHT, serializeSeeds(theme.getLightSeeds()))
                            .put(
                                    KEY_DARK,
                                    theme.isSeparateDark()
                                            ? serializeSeeds(theme.getDarkSeeds())
                                            : JSONObject.NULL);
            return root.put(KEY_COLOR_SOURCE, COLOR_SOURCE_CUSTOM_THEME)
                    .put(KEY_THEME_STYLE, ThemeStyle.name(theme.getStyle()))
                    .put(KEY_CUSTOM_THEME, custom);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to serialize custom theme", exception);
        }
    }

    private static JSONObject copy(JSONObject source) throws JSONException {
        JSONObject result = new JSONObject();
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            result.put(key, source.get(key));
        }
        return result;
    }

    @ColorInt
    public static int parseRgb(@Nullable String value) {
        if (value == null || !RGB_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "RGB must contain exactly six hexadecimal digits");
        }
        return Color.rgb(
                Integer.parseInt(value.substring(0, 2), 16),
                Integer.parseInt(value.substring(2, 4), 16),
                Integer.parseInt(value.substring(4, 6), 16));
    }

    @NonNull
    public static String formatRgb(@ColorInt int color) {
        if (Color.alpha(color) != 0xFF) {
            throw new IllegalArgumentException("RGB must be opaque");
        }
        return String.format(Locale.ROOT, "%06X", color & 0xFFFFFF);
    }

    public static void requireSupportedStyle(@ThemeStyle.Type int style) {
        if (!SUPPORTED_STYLES.contains(style)) {
            throw new IllegalArgumentException("Unsupported custom theme style: " + style);
        }
    }

    private static @Nullable ParseResult validateVersion(JSONObject custom) {
        Object value = custom.opt(KEY_VERSION);
        if (!(value instanceof Number)) {
            return ParseResult.error(Status.MALFORMED, "version");
        }
        if (((Number) value).intValue() != VERSION_1) {
            return ParseResult.error(Status.UNSUPPORTED_VERSION, "version");
        }
        return null;
    }

    private static @Nullable UUID parseId(@Nullable Object value) {
        if (!(value instanceof String)) {
            return null;
        }
        try {
            return UUID.fromString((String) value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static @Nullable Integer parseStyle(@Nullable Object value) {
        if (!(value instanceof String)) {
            return null;
        }
        try {
            int style = ThemeStyle.valueOf((String) value);
            requireSupportedStyle(style);
            return style;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static CustomThemeSeeds parseDarkSeeds(JSONObject custom, boolean separateDark)
            throws LinkageException, SeedException {
        if (!custom.has(KEY_DARK)) {
            throw new LinkageException("dark");
        }
        if (!separateDark) {
            if (!custom.isNull(KEY_DARK)) {
                throw new LinkageException("dark");
            }
            return null;
        }
        JSONObject dark = asObject(custom.opt(KEY_DARK));
        if (dark == null) {
            throw new LinkageException("dark");
        }
        return parseSeeds(dark);
    }

    private static CustomThemeSeeds parseSeeds(@Nullable JSONObject object) throws SeedException {
        if (object == null) {
            throw new SeedException("seed_object");
        }
        try {
            return new CustomThemeSeeds(
                    parseRgb(requireString(object, KEY_PRIMARY)),
                    parseRgb(requireString(object, KEY_SECONDARY)),
                    parseRgb(requireString(object, KEY_TERTIARY)),
                    parseRgb(requireString(object, KEY_NEUTRAL)));
        } catch (IllegalArgumentException exception) {
            throw new SeedException("seed_value");
        }
    }

    private static String requireString(JSONObject object, String key) throws SeedException {
        Object value = object.opt(key);
        if (!(value instanceof String)) {
            throw new SeedException(key);
        }
        return (String) value;
    }

    private static JSONObject serializeSeeds(CustomThemeSeeds seeds) throws JSONException {
        return new JSONObject()
                .put(KEY_PRIMARY, formatRgb(seeds.getPrimary()))
                .put(KEY_SECONDARY, formatRgb(seeds.getSecondary()))
                .put(KEY_TERTIARY, formatRgb(seeds.getTertiary()))
                .put(KEY_NEUTRAL, formatRgb(seeds.getNeutral()));
    }

    private static @Nullable JSONObject asObject(@Nullable Object value) {
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

    /** Immutable parsed active custom theme. */
    public static final class ActiveTheme {
        private final UUID mId;
        @ThemeStyle.Type private final int mStyle;
        private final boolean mSeparateDark;
        private final CustomThemeSeeds mLightSeeds;
        private final CustomThemeSeeds mDarkSeeds;

        public ActiveTheme(
                @NonNull UUID id,
                @ThemeStyle.Type int style,
                boolean separateDark,
                @NonNull CustomThemeSeeds lightSeeds,
                @Nullable CustomThemeSeeds darkSeeds) {
            requireSupportedStyle(style);
            if (separateDark != (darkSeeds != null)) {
                throw new IllegalArgumentException("Dark seeds must match separate-dark mode");
            }
            mId = id;
            mStyle = style;
            mSeparateDark = separateDark;
            mLightSeeds = lightSeeds;
            mDarkSeeds = darkSeeds;
        }

        @NonNull
        public UUID getId() {
            return mId;
        }

        @ThemeStyle.Type
        public int getStyle() {
            return mStyle;
        }

        public boolean isSeparateDark() {
            return mSeparateDark;
        }

        @NonNull
        public CustomThemeSeeds getLightSeeds() {
            return mLightSeeds;
        }

        @Nullable
        public CustomThemeSeeds getDarkSeeds() {
            return mDarkSeeds;
        }
    }

    /** Parsing result with a stable terminal status and optional rejection reason. */
    public static final class ParseResult {
        private final Status mStatus;
        private final ActiveTheme mTheme;
        private final String mReasonCode;

        private ParseResult(Status status, ActiveTheme theme, String reasonCode) {
            mStatus = status;
            mTheme = theme;
            mReasonCode = reasonCode;
        }

        private static ParseResult notCustom() {
            return new ParseResult(Status.NOT_CUSTOM, null, null);
        }

        private static ParseResult valid(ActiveTheme theme) {
            return new ParseResult(Status.VALID, theme, null);
        }

        private static ParseResult error(Status status, String reasonCode) {
            return new ParseResult(status, null, reasonCode);
        }

        @NonNull
        public Status getStatus() {
            return mStatus;
        }

        @Nullable
        public ActiveTheme getTheme() {
            return mTheme;
        }

        @Nullable
        public String getReasonCode() {
            return mReasonCode;
        }
    }

    private static final class LinkageException extends Exception {
        private LinkageException(String reason) {
            super(reason);
        }
    }

    private static final class SeedException extends Exception {
        private SeedException(String reason) {
            super(reason);
        }
    }
}
