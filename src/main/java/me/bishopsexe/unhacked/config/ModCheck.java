/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.config;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import me.bishopsexe.unhacked.check.DetectionMode;
import me.bishopsexe.unhacked.check.ModCheckResult;

public record ModCheck(
    @NonNull String id,
    @NonNull String displayName,
    @NonNull String key,
    @NonNull DetectionMode mode,
    @NonNull Map<ModCheckResult, List<String>> executeOn
) {

}