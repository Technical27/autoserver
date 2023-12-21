package io.github.technical27.autoserver;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ModInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

public class Listener {
    private final Logger logger;

    private final ArrayList<ServerInfo> infos;
    private final RegisteredServer lobbyServer;

    public Listener(Logger logger, Collection<RegisteredServer> servers, Config cfg) {
        this.logger = logger;

        this.lobbyServer = findServer(servers, cfg.getLobby());

        this.infos = new ArrayList<ServerInfo>();

        for (Config.ServerInfo info : cfg.getServerInfo()) {
            RegisteredServer server = findServer(servers, info.getName());
            if (server == null) {
                logger.warn("Can't find the registered server " + info.getName() + ", this is not going to work.");
                continue;
            }
            this.infos.add(new ServerInfo(server, info.getRequiredMods()));
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
            logger.error("caught exception in connectPlayer");
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
            for (ServerInfo info : infos) {
                List<ModInfo.Mod> requiredMods = info.getRequiredMods();
                if (mods.containsAll(requiredMods)) {
                    connectPlayer(player, info.getServer());
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
