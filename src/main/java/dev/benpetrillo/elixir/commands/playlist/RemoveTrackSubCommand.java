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

import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.PlaylistUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class RemoveTrackSubCommand extends SubCommand implements Arguments {

    public RemoveTrackSubCommand() {
        super("remove", "Remove a track from the playlist.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        final String playlistId = interaction.getArgument("id", String.class);
        final CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            interaction.reply(Embed.error("Unable to find a playlist of id `" + playlistId + "`."), false);
            return;
        }
        assert interaction.getMember() != null;
        if (!PlaylistUtil.isAuthor(playlist, interaction.getMember())) {
            interaction.reply(Embed.error("You are not the author of this playlist."), false);
            return;
        }
        int index = interaction.getArgument("index", 0L, Long.class).intValue();
        try {
            PlaylistUtil.removeTrackFromList(index, playlist);
            interaction.reply(Embed.def("Successfully removed track from playlist."), false);
        } catch (IndexOutOfBoundsException ignored) {
            interaction.reply(Embed.error("That track doesn't exist."), false);
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
            Argument.create("track", "The track number in the playlist.", "track", OptionType.INTEGER, true, 1).range(0, 800)
        );
    }
}
