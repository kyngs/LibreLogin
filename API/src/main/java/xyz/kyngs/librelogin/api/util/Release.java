/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.util;

/**
 * A record representing a release.
 * A release consists of a semantic version and a name.
 *
 * @param version The semantic version of the release.
 * @param name    The name of the release.
 */
public record Release(SemanticVersion version, String name) {
}
