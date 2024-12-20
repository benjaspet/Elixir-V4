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

import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.PlaylistUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class SettingSubCommand extends SubCommand implements Arguments {

    public SettingSubCommand() {
        super("setting", "Change playlist settings.");
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
        final String toChange = interaction.getArgument("setting", "name", String.class);
        final String value = interaction.getArgument("value", "Default Name", String.class);
        final String defAulr = interaction.getArgument("default", "false", String.class);
        switch (toChange) {
            default -> interaction.reply(Embed.error("Invalid setting: `" + toChange + "`."), false);
            case "cover" -> {
                if (!Utilities.isValidURL(value)) {
                    interaction.reply(Embed.error("That isn't a valid URL!"), false);
                    return;
                }
                PlaylistUtil.setPlaylistCover(playlist, value);
                interaction.reply(new EmbedBuilder()
                    .setDescription("Successfully swapped the playlist cover!")
                    .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                    .setImage(value).build());
            }
            case "name" -> {
                PlaylistUtil.setPlaylistName(playlist, value);
                interaction.reply(Embed.def("Successfully changed the playlist name to `" + value + "`."), false);
            }
            case "description" -> {
                PlaylistUtil.setPlaylistDescription(playlist, value);
                interaction.reply(new EmbedBuilder()
                    .setDescription("Successfully swapped the playlist description!")
                    .addField("New Description", value, false)
                    .setColor(ElixirConstants.DEFAULT_EMBED_COLOR).build(), false);
            }
            case "shuffle" -> {
                PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.SHUFFLE, playlist, Utilities.parseBoolean(value));
                interaction.reply(Embed.def("Successfully changed the shuffle setting to `" + value + "`."), false);
            }
            case "repeat" -> {
                PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.REPEAT, playlist, Utilities.parseBoolean(value));
                interaction.reply(Embed.def("Successfully changed the repeat setting to `" + value + "`."), false);
            }
            case "volume" -> {
                int volume = Integer.parseInt(value);
                if (volume < 0 || volume > 150)
                    volume = 100;
                PlaylistUtil.setPlaylistVolume(playlist, volume);
                interaction.reply(Embed.def("Successfully changed the volume to `" + volume + "`%."), false);
            }
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
            Argument.createWithChoices("setting", "The setting to change.", "setting", OptionType.STRING, true, 1, "shuffle", "repeat", "volume", "name", "description", "cover", "volume"),
            Argument.createTrailingArgument("value", "The new value.", "value", OptionType.STRING, true, 2)
        );
    }
}
