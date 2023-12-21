package io.github.technical27.autoserver;

import java.util.List;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ModInfo;

public class ServerInfo {
    private final RegisteredServer server;
    private final List<ModInfo.Mod> requiredMods;

    public ServerInfo(RegisteredServer server, List<ModInfo.Mod> requiredMods) {
        this.server = server;
        this.requiredMods = requiredMods;
    }

    public RegisteredServer getServer() {
        return server;
    }

    public List<ModInfo.Mod> getRequiredMods() {
        return requiredMods;
    }
}
