//    NeoForge mod entry
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(value = "suspendtheserver", dist = Dist.DEDICATED_SERVER)
public class SuspendTheServer {

    public static final Config CONFIG = Config.fromConfigFile();
    public static final Map<MinecraftServer, ServerSuspension> SUSPENSIONS = new ConcurrentHashMap<>();
    public static final Logger LOGGER = LogManager.getLogger(SuspendTheServer.class);

    public SuspendTheServer(FMLModContainer container, IEventBus modBus, Dist dist) {
        if (!CONFIG.enable())
            return;
        NeoForge.EVENT_BUS.addListener(SuspendTheServer::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(SuspendTheServer::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(SuspendTheServer::onServerStarted);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        var server = event.getEntity().getServer();
        if (server == null || server.getPlayerCount() > 1) {
            return;
        }
        if (CONFIG.saveAllBeforeSuspension()) {
            server.saveEverything(false, false, false);
        }
        SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var server = event.getEntity().getServer();
        if (server == null || server.getPlayerCount() > 1) {
            return;
        }
        try {
            SUSPENSIONS.get(server).resume();
        } catch (NullPointerException e) {
            LOGGER.warn(e);
        }
    }
}