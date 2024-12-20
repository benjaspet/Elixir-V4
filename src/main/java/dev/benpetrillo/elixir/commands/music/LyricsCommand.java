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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.LyricManager;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.Utilities;
import genius.SongSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class LyricsCommand extends Command implements Arguments {

    public LyricsCommand() {
        super("lyrics", "Fetch lyrics for a song.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        String song = interaction.getArgument("song", "", String.class);
        if (song.isEmpty()) {
            assert interaction.getGuild() != null;
            AudioTrack track = ElixirMusicManager.getInstance()
                .getMusicManager(interaction.getGuild())
                .audioPlayer.getPlayingTrack();
            if (track == null) {
                interaction.reply(Embed.error("There is not a song playing."), false);
                return;
            } else song = track.getInfo().title;
        }
        try {
            SongSearch result = LyricManager.getTrackData(song);
            SongSearch.Hit shortened = result.getHits().get(0);
            MessageEmbed embed = new EmbedBuilder()
                .setTitle(shortened.getTitle())
                .setThumbnail(shortened.getThumbnailUrl())
                .setDescription(Utilities.shorten(shortened.fetchLyrics()))
                .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                .setFooter("Lyrics Powered by Genius", "https://images.rapgenius.com/365f0e9e7e66a120867b7b0ff340264a.750x750x1.png")
                .setTimestamp(new Date().toInstant())
                .build();
            interaction.reply(embed);
        } catch (IOException | IndexOutOfBoundsException exception) {
            interaction.reply(Embed.error("Unable to fetch lyrics for that track."), false);
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.createTrailingArgument("song", "The song to fetch the lyrics of.", "song", OptionType.STRING, false, 0)
        );
    }
}
