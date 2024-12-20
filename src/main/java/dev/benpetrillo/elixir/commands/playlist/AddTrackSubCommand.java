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
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utils.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class AddTrackSubCommand extends SubCommand implements Arguments {

    public AddTrackSubCommand() {
        super("addtrack", "Add a track to the playlist.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        final String playlistId = interaction.getArgument("id", "test", String.class);
        final CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            interaction.reply(Embed.error("Unable to find a playlist with ID `" + playlistId + "`."), false);
            return;
        }
        assert interaction.getMember() != null;
        if (!PlaylistUtil.isAuthor(playlist, interaction.getMember())) {
            interaction.reply(Embed.error("You are not the author of this playlist."), false);
            return;
        }
        String track = interaction.getArgument("track", "https://youtube.com/watch?v=dQw4w9WgXcQ", String.class);
        final long index = interaction.getArgument("index", -1L, Long.class);
        if (!Utilities.isValidURL(track)) {
            try {
                track = HttpUtil.searchForVideo(track);
            } catch (Exception ignored) {
                return;
            }
            if (track == null) {
                interaction.reply(Embed.error("Unable to find a track with the query `" + track + "`."), false);
                return;
            }
        }
        try {
            final AudioTrackInfo trackInfo = TrackUtil.getTrackInfoFromUrl(track);
            if (trackInfo == null) {
                interaction.reply(Embed.error("Unable to find a track with the URL `" + track + "`."), false);
                return;
            }
            PlaylistUtil.addTrackToList(trackInfo, playlist, (int) index);
            interaction.reply(Embed.def("Successfully added [%s](%s) to playlist.".formatted(trackInfo.title, trackInfo.uri)), false);
        } catch (Exception ignored) {
            interaction.reply(Embed.error("Unable to add track to playlist."), false);
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
            Argument.create("track", "The track to add to the playlist.", "track", OptionType.STRING, true, 1),
            Argument.create("index", "The index of the track.", "index", OptionType.INTEGER, false, 2)
        );
    }
}
