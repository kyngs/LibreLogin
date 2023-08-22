/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.integration.nanolimbo;

import java.net.SocketAddress;
import java.time.Duration;

import ua.nanit.limbo.configuration.LimboConfig;
import ua.nanit.limbo.server.data.BossBar;
import ua.nanit.limbo.server.data.InfoForwarding;
import ua.nanit.limbo.server.data.PingData;
import ua.nanit.limbo.server.data.Title;

public class NanoLimboConfig implements LimboConfig {
    private final PingData pingData;
    private final SocketAddress address;
    private final InfoForwarding forwarding;

    public NanoLimboConfig(SocketAddress address, InfoForwarding forwarding) {
        this.pingData = new PingData();

        this.pingData.setDescription("NanoLimbo");
        this.pingData.setVersion("NanoLimbo");

        this.address = address;
        this.forwarding = forwarding;
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public int getMaxPlayers() {
        return -1;
    }

    @Override
    public PingData getPingData() {
        return pingData;
    }

    @Override
    public String getDimensionType() {
        return "the_end";
    }

    @Override
    public int getGameMode() {
        return 2; // Adventure game mode
    }

    @Override
    public InfoForwarding getInfoForwarding() {
        return forwarding;
    }

    @Override
    public long getReadTimeout() {
        return Duration.ofSeconds(30).toMillis();
    }

    @Override
    public int getDebugLevel() {
        return 0; // Display only errors
    }

    @Override
    public boolean isUseBrandName() {
        return false;
    }

    @Override
    public boolean isUseJoinMessage() {
        return false;
    }

    @Override
    public boolean isUseBossBar() {
        return false;
    }

    @Override
    public boolean isUseTitle() {
        return false;
    }

    @Override
    public boolean isUsePlayerList() {
        return false;
    }

    @Override
    public boolean isUseHeaderAndFooter() {
        return false;
    }

    @Override
    public String getBrandName() {
        return null;
    }

    @Override
    public String getJoinMessage() {
        return null;
    }

    @Override
    public BossBar getBossBar() {
        return null;
    }

    @Override
    public Title getTitle() {
        return null;
    }

    @Override
    public String getPlayerListUsername() {
        return "";
    }

    @Override
    public String getPlayerListHeader() {
        return null;
    }

    @Override
    public String getPlayerListFooter() {
        return null;
    }

    @Override
    public boolean isUseEpoll() {
        return false;
    }

    @Override
    public int getBossGroupSize() {
        return 1; // Default value
    }

    @Override
    public int getWorkerGroupSize() {
        return 4; // Default value
    }
}
