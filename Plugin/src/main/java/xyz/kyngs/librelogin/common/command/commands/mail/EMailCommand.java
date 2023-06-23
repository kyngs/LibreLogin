/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.mail;

import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.Command;
import xyz.kyngs.librelogin.common.mail.AuthenticEMailHandler;

public class EMailCommand<P> extends Command<P> {

    protected final AuthenticEMailHandler mailHandler;

    public EMailCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
        mailHandler = plugin.getEmailHandler();
        assert mailHandler != null;
    }
}
