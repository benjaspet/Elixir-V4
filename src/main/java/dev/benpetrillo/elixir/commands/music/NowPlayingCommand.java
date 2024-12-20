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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.TrackUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.PermissionException;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.util.Date;

public final class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        super("nowplaying", "View information on the track currently playing.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(Embed.error("This command can only be used in a guild."));
            return;
        }
        interaction.deferReply();
        try {
            assert interaction.getGuild() != null;
            final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            final AudioTrack track = audioPlayer.getPlayingTrack();
            if (track == null) {
                interaction.reply(Embed.error("There is no track playing at the moment."), false);
            } else {
                final AudioTrackInfo info = track.getInfo();
                final String thumbnail = TrackUtil.getCoverArt(track.getInfo());
                final String title = info.title.length() > 60 ? info.title.substring(0, 60) + "..." : info.title;
                final String duration = Utilities.formatDuration(track.getPosition()) + "/" + Utilities.formatDuration(track.getDuration());
                final String isLive = info.isStream ? "yes" : "no";
                final String artist = info.author;
                final String url = info.uri;
                final String requestedBy = "<@" + track.getUserData(String.class) + ">";
                final String contents = """
                    • Artist: %s
                    • Requested by: %s
                    • Duration: %s
                    • Livestream: %s
                    """.formatted(artist, requestedBy, duration, isLive);
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Currently Playing")
                    .setDescription(String.format("[%s](%s)", title, url))
                    .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                    .addField("Track Data", contents, false)
                    .setFooter("Elixir Music", ElixirClient.getJda().getSelfUser().getAvatarUrl())
                    .setTimestamp(new Date().toInstant())
                    .setThumbnail(thumbnail)
                    .build();
                interaction.reply(embed, false);
            }
        } catch (PermissionException ignored) {
            interaction.reply(Embed.error("I do not have permission to do this."), false);
        } catch (Exception exception) {
            ElixirClient.logger.error("An error occurred while getting the currently playing track.", exception);
            Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception).additionalInformation("Not a permission exception."));
            interaction.reply(Embed.error("An unknown error occurred."), false);
        }
    }
}
