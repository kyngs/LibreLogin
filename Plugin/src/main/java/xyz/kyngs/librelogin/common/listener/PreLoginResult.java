/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.listener;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.database.User;

public record PreLoginResult(PreLoginState state, @Nullable Component message, User user) {
}
