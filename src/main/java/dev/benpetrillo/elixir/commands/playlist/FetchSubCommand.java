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

package dev.benpetrillo.elixir.commands.playlist;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.PlaylistUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class FetchSubCommand extends SubCommand implements Arguments {

    public FetchSubCommand() {
        super("fetch", "Fetch a playlist.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        final String playlistId = interaction.getArgument("id", String.class);
        int page = interaction.getArgument("page", 1L, Long.class).intValue();
        if (page == 0) page = 1;
        final CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            interaction.reply(Embed.error("Unable to find a playlist of id `" + playlistId + "`."), false);
            return;
        }
        final List<PlaylistTrack> tracks = PlaylistUtil.getTracks(playlist);
        final StringBuilder description = new StringBuilder();
        final int maxAmount = Math.min(tracks.size(), 8);
        final String thumbnail = playlist.info.playlistCoverUrl;
        if (tracks.size() == 0) description.append("This playlist is empty.");
        for (int i = (page - 1) * 8; i < page * 8 && i < tracks.size(); i++) {
            PlaylistTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();
            String title = info.title.length() > 55 ? info.title.substring(0, 52) + "..." : info.title;
            String formattedString = String.format("**#%s** - [%s](%s)", i + 1, title, info.uri);
            description.append(formattedString).append("\n");
        }
        if (tracks.size() > maxAmount) {
            description.append("\n").append(String.format("...and %s more tracks.", tracks.size() - maxAmount));
        }
        final String settings = """
            Shuffle: %s
            Repeat: %s
            """.formatted(playlist.options.shuffle ? "Yes" : "No", playlist.options.repeat ? "Yes" : "No");
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(playlist.info.name)
            .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
            .setThumbnail(thumbnail)
            .setDescription("Author: <@%s>".formatted(playlist.info.author))
            .addField("Description", playlist.info.description, false)
            .addField("Queue Settings", String.valueOf(settings), false)
            .addField("Sample Tracks", String.valueOf(description), false)
            .setFooter("Elixir Music", ElixirClient.getJda().getSelfUser().getAvatarUrl())
            .setTimestamp(new Date().toInstant())
            .build();
        interaction.reply(embed, false);
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
            Argument.create("page", "The page number to fetch.", "page", OptionType.INTEGER, false, 1).range(1, 100)
        );
    }
}
