package dev.benpetrillo.elixir.commands.misc;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.utils.Embed;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
public final class ConfigureCommand extends Command implements Arguments {
    public ConfigureCommand() {
        super("configure", "Configure the bot's settings.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.setEphemeral();
        var musicManager = ElixirMusicManager.getInstance();

        // Pull arguments.
        var delete = interaction.getArgument("delete", false, Boolean.class);

        // Check if YouTube is configured.
        if (musicManager.isYoutubeConfigured() && !delete) {
            interaction
                .setEphemeral()
                .reply(Embed.error("YouTube is already configured."));
            return;
        }

        // Prompt for OAuth2 authorization.
        interaction.deferReply();
        var pair = musicManager.doAuthFlow(_void -> {
            try {
                var hook = interaction.getSlashExecutor().getHook();
                hook
                    .editOriginalEmbeds(Embed.def("Authorization flow completed!"))
                    .queue();

                // Save the authorization credentials.
                musicManager.saveCredentials();
            } catch (IOException exception) {
                log.warn("Failed to save credentials.", exception);
            }
        });

        // Check if the code is valid.
        if (pair == null) {
            interaction
                .setEphemeral()
                .reply(Embed.error("Invalid authorization code."));
            return;
        }

        // Reply to the embed.
        var url = pair.a();
        var code = pair.b();
        interaction.reply(Embed.def(
            "Please authorize [here](<%s>) with the code `%s`."
                .formatted(url, code))
        );
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("delete", "Ignores any existing credentials.", "delete", OptionType.BOOLEAN, false, 0)
        );
    }
}
