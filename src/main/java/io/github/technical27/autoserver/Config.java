package io.github.technical27.autoserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.velocitypowered.api.util.ModInfo;

import com.moandjiezana.toml.Toml;

public class Config {
    private String lobby;
    private HashMap<String, List<ModInfo.Mod>> serverInfo;

    private static final String DEFAULT_CONFIG = "# autoserver config\n\n# Name of the base server everyone can connect to.\n# This is the first server anyone will connect to before being\n# redirected to the correct modpack/server and is required to be set.\nlobby_name = \"lobby\"\n\n# example server config\n# this redirects anybody with appliedenergistics2 with version rv6-stable-7 to the\n# server 'modpack1' as defined in the velocity config.\n# [servers.modpack1]\n# required = [ \"appliedenergistics2@rv6-stable-7\" ]\n";

    public Config(String lobby, HashMap<String, List<ModInfo.Mod>> serverInfo) {
        this.lobby = lobby;
        this.serverInfo = serverInfo;
    }

    public static Config read(Logger logger, Path dataDir) {
        Path configPath = dataDir.resolve("config.toml");

        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectory(dataDir);
            } catch (IOException e) {
                logger.error("failed to create plugin dir");
            }
        }

        if (!Files.exists(configPath)) {
            try {
                Files.createFile(configPath);
                Files.writeString(configPath, DEFAULT_CONFIG);
            } catch (IOException e) {
                logger.error("failed to create default config");
            }
        }

        Toml toml = new Toml().read(configPath.toFile());
        String lobby = toml.getString("lobby_name");

        if (lobby == null) {
            logger.error("key 'lobby_name' was empty, this will not work");
            return null;
        }

        HashMap<String, List<ModInfo.Mod>> serverInfo = new HashMap<String, List<ModInfo.Mod>>();
        Toml servers = toml.getTable("servers");

        if (servers == null) {
            logger.error("no servers were configured, this will not work");
            return null;
        }

        for (Map.Entry<String, Object> e : servers.entrySet()) {
            String name = e.getKey();
            Toml values = (Toml) e.getValue();

            List<String> mods = values.getList("required");

            if (mods == null || mods.size() < 1) {
                logger.error("the server '" + name + "' has no required mods, ignoring");
                continue;
            }

            List<ModInfo.Mod> modinfo = mods.stream().map(s -> {
                if (s == null) {
                    return null;
                }

                String[] parts = s.split("@");

                if (parts.length != 2) {
                    logger.error("bad mod format `" + s + "`, this mod will be ignored");
                    return null;
                }

                return new ModInfo.Mod(parts[0], parts[1]);
            }).filter(m -> {
                return m != null;
            }).collect(Collectors.toList());

            serverInfo.put(name, modinfo);
        }

        return new Config(lobby, serverInfo);
    }

    public String getLobby() {
        return lobby;
    }

    public HashMap<String, List<ModInfo.Mod>> getServerInfo() {
        return serverInfo;
    }
}
