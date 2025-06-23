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

public class Config {
    public static final Path CONFIG_PATH = Paths.get("config/server-suspend-now.properties");
    public static final String DEFAULT_CONFIG = """
            # server-suspend-now.properties
            enable=true
            """;

    private boolean enable = false;
    private static final Logger LOGGER = LogManager.getLogger(Config.class);

    public Config() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(CONFIG_PATH))) {
                    writer.println(DEFAULT_CONFIG);
                }
            }
            Properties props = new Properties();
            props.load(Files.newInputStream(CONFIG_PATH));
            enable = props.getProperty("enable", "false").equals("true");
        } catch (IOException e) {
            LOGGER.warn(e);
            enable = false;
        }
    }

    public boolean isEnable() {
        return enable;
    }
}
