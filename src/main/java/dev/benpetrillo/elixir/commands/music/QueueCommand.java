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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.TrackUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.PermissionException;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class QueueCommand extends Command {

    public QueueCommand() {
        super("queue", "View all songs in the queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(Embed.error("This command can only be used in a guild."));
            return;
        }
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        if (musicManager.scheduler.queue.isEmpty()) {
            interaction.reply(Embed.error("There are no songs in the queue."), false);
            return;
        }
        interaction.deferReply();
        try {
            final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
            final List<AudioTrack> arrayQueue = new ArrayList<>(queue);
            final AudioTrack nowPlaying = musicManager.audioPlayer.getPlayingTrack();
            final String thumbnail = TrackUtil.getCoverArt(nowPlaying.getInfo());
            StringBuilder description = new StringBuilder();
            int maxAmount = Math.min(arrayQueue.size(), 12);
            for (int i = 0; i < maxAmount; i++) {
                final AudioTrack track = arrayQueue.get(i);
                final AudioTrackInfo info = track.getInfo();
                final String title = info.title.length() > 55 ? info.title.substring(0, 52) + "..." : info.title;
                final String formattedString = String.format("**#%s** - [%s](%s)", i + 1, title, info.uri);
                description.append(formattedString).append("\n");
            }
            if (arrayQueue.size() > maxAmount) {
                description.append("\n").append(String.format("...and %s more tracks.", arrayQueue.size() - maxAmount));
            }
            final String nowPlayingTitle = nowPlaying.getInfo().title;
            final String nowPlayingTrimmed = nowPlayingTitle.length() > 55 ? nowPlayingTitle.substring(0, 52) + "..." : nowPlayingTitle;
            final String queueData = "• Tracks queued: %s\n• Loop mode: %s\n• Volume: %s".formatted(
                queue.size(), Utilities.prettyPrint(musicManager.scheduler.repeating.toString()), musicManager.audioPlayer.getVolume());
            MessageEmbed embed = new EmbedBuilder()
                .setTitle("Guild Queue")
                .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                .setAuthor("Now Playing: " + nowPlayingTrimmed, nowPlaying.getInfo().uri)
                .setThumbnail(thumbnail)
                .setDescription(description)
                .addField("Queue Data", queueData, false)
                .setFooter("Elixir Music", ElixirClient.getInstance().jda.getSelfUser().getAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build();
            interaction.reply(embed, false);
        } catch (PermissionException ignored) {
            interaction.reply(Embed.error("An error occurred while running this command."), false);
        }
    }
}
