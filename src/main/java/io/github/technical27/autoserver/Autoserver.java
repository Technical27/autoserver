package io.github.technical27.autoserver;

import com.google.inject.Inject;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;

@Plugin(id = "autoserver", name = "Autoserver", version = "0.1.0-SNAPSHOT", url = "https://technical27.github.io", description = "automatically switch between forge servers based on client mods", authors = {
        "Technical27" })
public class Autoserver {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDir;

    @Inject
    public Autoserver(ProxyServer server, Logger logger, @DataDirectory Path dataDir) {
        this.server = server;
        this.logger = logger;
        this.dataDir = dataDir;

        logger.info("Autoserver starting");
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        Collection<RegisteredServer> servers = server.getAllServers();

        Config cfg = Config.read(logger, dataDir);

        if (cfg == null) {
            logger.error("Failed to read config, this plugin will not do anything");
            return;
        }

        server.getEventManager().register(this, new Listener(logger, servers, cfg));
    }
}
