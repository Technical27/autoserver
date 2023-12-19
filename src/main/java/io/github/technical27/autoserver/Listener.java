package io.github.technical27.autoserver;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ModInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

public class Listener {
    private final Logger logger;

    private final HashMap<RegisteredServer, List<ModInfo.Mod>> infos;
    private final RegisteredServer lobbyServer;

    public Listener(Logger logger, Collection<RegisteredServer> servers, Config cfg) {
        this.logger = logger;

        this.lobbyServer = findServer(servers, cfg.getLobby());

        this.infos = new HashMap<RegisteredServer, List<ModInfo.Mod>>();
        HashMap<String, List<ModInfo.Mod>> serverInfo = cfg.getServerInfo();

        for (Map.Entry<String, List<ModInfo.Mod>> e : serverInfo.entrySet()) {
            RegisteredServer server = findServer(servers, e.getKey());
            if (server == null) {
                logger.warn("Can't find the registered server " + e.getKey() + ", this is not going to work.");
                continue;
            }
            this.infos.put(server, e.getValue());
        }
    }

    private RegisteredServer findServer(Collection<RegisteredServer> servers, String name) {
        for (RegisteredServer s : servers) {
            if (s.getServerInfo().getName().equals(name)) {
                return s;
            }
        }

        return null;
    }

    private void notifyPlayerNoMods(Player player) {
        logger.info("Player " + player.getUsername() + " has no mods, keeping in lobby");
        player.sendMessage(Messages.NO_MODS);
    }

    private void connectPlayer(Player player, RegisteredServer server) {
        try {
            player.createConnectionRequest(server).connect().get();
        } catch (ExecutionException e) {
            logger.info("player " + player.getUsername() + " failed to connect to server "
                    + server.getServerInfo().getName());
            player.sendMessage(Messages.CONNECTION_FAIL);
        } catch (Exception e) {
            logger.warn("caught exception in connectPlayer");
            e.printStackTrace();
        } finally {
            player.clearTitle();
        }

    }

    @Subscribe
    public EventTask onServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();

        List<ModInfo.Mod> mods;

        player.clearTitle();

        if (!player.getCurrentServer().map(s -> {
            return s.getServer() == this.lobbyServer;
        }).orElse(false))
            return null;

        if (event.getPreviousServer() != null) {
            player.sendMessage(Messages.UNEXPECTED_LOBBY);
            return null;
        }

        try {
            mods = player.getModInfo().orElseThrow().getMods();
        } catch (NoSuchElementException e) {
            notifyPlayerNoMods(player);
            return null;
        }

        if (mods.size() < 5) {
            notifyPlayerNoMods(player);
            return null;
        }

        player.showTitle(Messages.CONNECTING_TITLE);

        return EventTask.async(() -> {
            for (Map.Entry<RegisteredServer, List<ModInfo.Mod>> entry : infos.entrySet()) {
                List<ModInfo.Mod> requiredMods = entry.getValue();
                if (mods.containsAll(requiredMods)) {
                    connectPlayer(player, entry.getKey());
                    return;
                }
            }

            logger.warn("failed to find correct server for player " + player.getUsername());
            logger.warn("listing mods:");
            logger.warn(String.join(", ", mods.stream().map(m -> {
                return m.getId() + "@" + m.getVersion();
            }).toArray(String[]::new)));

            player.clearTitle();
            player.sendMessage(Messages.NO_SERVER_FOUND);
        });
    }
}
