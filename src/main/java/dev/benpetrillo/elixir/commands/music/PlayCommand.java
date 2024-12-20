/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
 *
 * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * All portions of this software are available for public use, provided that
 * credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.commands.music;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.HttpUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PlayCommand extends Command implements Arguments {

    public PlayCommand() {
        super("play", "Play a track with a link or query.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(Embed.error("This command can only be used in a guild."));
            return;
        }
        assert interaction.getGuild() != null;
        final GuildVoiceState voiceState = interaction.getGuild().getSelfMember().getVoiceState();
        assert interaction.getMember() != null;
        final GuildVoiceState memberVoiceState = interaction.getMember().getVoiceState();
        assert memberVoiceState != null;
        interaction.deferReply();
        if (!memberVoiceState.inAudioChannel()) {
            interaction.reply(Embed.error("You must be in a voice channel to run this command."), false);
            return;
        }
        String query = interaction.getArgument("query", "https://www.youtube.com/watch?v=7-qGKqveZaM", String.class);
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        final VoiceChannel memberChannel = Objects.requireNonNull(memberVoiceState.getChannel()).asVoiceChannel();
        assert voiceState != null;
        if (!voiceState.inAudioChannel()) {
            audioManager.openAudioConnection(memberChannel);
            audioManager.setSelfDeafened(true);
        }
        if (!Utilities.isValidURL(query)) {
            query = HttpUtil.searchForVideo(query);
            if (query == null) {
                interaction.reply(Embed.error("No results found for that query."));
                return;
            }
            ElixirMusicManager.getInstance().loadAndPlay(query, interaction, "https://www.youtube.com");
            return;
        }
        if (Utilities.isValidURL(query) && query.contains("spotify") && query.contains("track")) {
            try {
                SpotifySourceManager.authorize();
            } catch (IOException | ParseException | SpotifyWebApiException exception) {
                Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception).additionalInformation("Spotify authorization exception."));
            }
        }
        ElixirMusicManager.getInstance().loadAndPlay(query, interaction, query);
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.createTrailingArgument("query", "The track to play, by URL or query.", "query", OptionType.STRING, true, 0)
        );
    }
}
