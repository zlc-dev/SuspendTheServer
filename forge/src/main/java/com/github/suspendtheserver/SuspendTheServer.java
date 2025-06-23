//    Forge mod entry
//    Copyright (C) 2025 zlc
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.github.suspendtheserver;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod("suspendtheserver")
public class SuspendTheServer {
    public static final Config CONFIG = new Config();
    public static final Map<MinecraftServer, ServerSuspension> SUSPENSIONS = new ConcurrentHashMap<>();
    public static final Logger LOGGER = LogManager.getLogger(SuspendTheServer.class);

    public SuspendTheServer() {
        if (!CONFIG.isEnable())
            return;
        MinecraftForge.EVENT_BUS.register(ServerEventHandler.class);
    }
}

class ServerEventHandler {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        SuspendTheServer.SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        var server = event.getEntity().getServer();
        if (server == null || server.getPlayerCount() > 1) {
            return;
        }
        SuspendTheServer.SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var server = event.getEntity().getServer();
        if (server == null || server.getPlayerCount() > 1) {
            return;
        }
        try {
            SuspendTheServer.SUSPENSIONS.get(server).resume();
        } catch (NullPointerException e) {
            SuspendTheServer.LOGGER.warn(e);
        }
    }
}
