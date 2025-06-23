//    Config of the mod
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public record Config(boolean enable, boolean saveAllBeforeSuspension) {

    public static final Path DEFAULT_CONFIG_FILE_PATH = Paths.get("config/suspend-the-server.properties");
    public static final String DEFAULT_CONFIG = """
            # suspend-the-server.properties
            
            # Enable the mod
            enable=true
            
            # Save everything before suspension
            save_all_before_suspension=true
            """;

    private static final Logger LOGGER = LogManager.getLogger(Config.class);

    public static Config fromConfigFile() {
        return fromConfigFile(DEFAULT_CONFIG_FILE_PATH);
    }

    public static Config fromConfigFile(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                    writer.print(DEFAULT_CONFIG);
                }
            }
            Properties props = new Properties();
            props.load(Files.newInputStream(path));
            boolean enable = props.getProperty("enable", "false").trim().equalsIgnoreCase("true");
            boolean saveAllBeforeSuspension = props.getProperty("save_all_before_suspension", "true").trim().equalsIgnoreCase("true");
            return new Config(enable, saveAllBeforeSuspension);
        } catch (IOException e) {
            LOGGER.warn(e);
            return new Config(false, true);
        }
    }
}
