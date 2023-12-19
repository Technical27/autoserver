package io.github.technical27.autoserver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class Messages {

        private static final Component SWITCH_INSTRUCTIONS = Component
                        .text("If you know the modpack server you want to join is still up,")
                        .color(NamedTextColor.RED)
                        .appendNewline()
                        .append(Component
                                        .text("try the command '/server', to list all servers"))
                        .appendNewline()
                        .append(Component.text("and '/server <name>' to join manually.").color(NamedTextColor.RED))
                        .appendNewline()
                        .append(Component.text("If that didn't work, complain on the discord or something idk.")
                                        .color(NamedTextColor.RED));

        public static final Component NO_MODS = Component
                        .text("You literally have zero mods.")
                        .color(NamedTextColor.RED)
                        .appendNewline()
                        .append(Component.text("How the hell do you think that was going to work?")
                                        .color(NamedTextColor.RED))
                        .appendNewline()
                        .append(Component.text("Seriously, did you make a mistake or are you just that stupid?")
                                        .color(NamedTextColor.RED));

        public static final Component NO_SERVER_FOUND = Component.text(
                        "Couldn't find the correct server to connect to.")
                        .color(NamedTextColor.RED)
                        .appendNewline()
                        .append(Component.text("You are now stuck here.").color(NamedTextColor.RED))
                        .appendNewline()
                        .append(SWITCH_INSTRUCTIONS);

        public static final Component UNEXPECTED_LOBBY = Component.text("Hmm, you are back here for some reason.")
                        .color(NamedTextColor.RED)
                        .appendNewline()
                        .append(SWITCH_INSTRUCTIONS);

        public static final Component CONNECTION_FAIL = Component.text("Huh, that didn't connect.")
                        .color(NamedTextColor.RED)
                        .appendNewline()
                        .append(SWITCH_INSTRUCTIONS);

        public static final Title CONNECTING_TITLE = Title.title(
                        Component.text("Connecting....").color(NamedTextColor.GOLD),
                        Component.text("just wait a bit").color(NamedTextColor.GOLD));
}
