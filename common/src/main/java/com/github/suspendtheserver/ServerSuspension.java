//    Suspension of the server
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerSuspension {
    public volatile @Nullable MinecraftServer server = null;

    private final AtomicBoolean suspended;

    private static final Logger logger = LogManager.getLogger(ServerSuspension.class);

    public ServerSuspension(@Nullable MinecraftServer server) {
        this.server = server;
        this.suspended = new AtomicBoolean(false);
    }

    public boolean isSuspended() {
        return server != null && suspended.get();
    }

    public void suspend() {
        MinecraftServer server = this.server;
        if (server == null) {
            return;
        }
        boolean hasSuspended = suspended.get();
        suspended.set(true);
        if (hasSuspended || !server.isRunning()) {
            return;
        }
        logger.info("suspend the server");
        System.gc();
        server.execute(()-> {
            try {
                server.saveEverything(false, false, false);
            } catch (NoSuchMethodError ex) {
                // This happens on Minecraft versions before 1.18 where
                // MinecraftServer.saveEverything doesn't exist.
                server.getPlayerList().saveAll();
                server.saveAllChunks(false, false, false);
            } finally {
                var serverTickRateManager = server.tickRateManager();
                if (serverTickRateManager.isSprinting()) {
                    serverTickRateManager.stopSprinting();
                }
                if (serverTickRateManager.isSteppingForward()) {
                    serverTickRateManager.stopStepping();
                }
                serverTickRateManager.setFrozen(true);
            }
        });
    }

    public void resume() {
        MinecraftServer server = this.server;
        if (server == null) {
            return;
        }
        logger.info("resume the server");
        suspended.set(false);
        var serverTickRateManager = server.tickRateManager();
        serverTickRateManager.setFrozen(false);
    }
}
