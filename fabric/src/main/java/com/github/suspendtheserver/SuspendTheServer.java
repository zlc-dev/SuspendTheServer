//    Fabric mod entry
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

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SuspendTheServer implements DedicatedServerModInitializer {

    public static final Config CONFIG = Config.fromConfigFile();
    public static final Map<MinecraftServer, ServerSuspension> SUSPENSIONS = new ConcurrentHashMap<>();
    public static final Logger LOGGER = LogManager.getLogger(SuspendTheServer.class);

    @Override
    public void onInitializeServer() {
        if (!CONFIG.enable())
            return;
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            if (server.getPlayerCount() > 1)
                return;
            if (CONFIG.saveAllBeforeSuspension()) {
                server.saveEverything(false, false, false);
            }
            SUSPENSIONS.computeIfAbsent(server, ServerSuspension::new).suspend();
        }));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.getPlayerCount() > 1)
                return;
            try {
                SUSPENSIONS.get(server).resume();
            } catch (NullPointerException e) {
                LOGGER.warn(e);
            }
        });
    }
}
