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
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.AudioUtil;
import dev.benpetrillo.elixir.utils.Embed;
import net.dv8tion.jda.api.EmbedBuilder;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

public final class ShuffleCommand extends Command {

    public ShuffleCommand() {
        super("shuffle", "Shuffle the current queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(Embed.error("This command can only be used in a guild."));
            return;
        }
        if (AudioUtil.audioCheck(interaction)) return;
        interaction.deferReply();
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        var tracks = musicManager.getScheduler().shuffle();

        final StringBuilder description = new StringBuilder();
        final int maxAmount = Math.min(tracks.size(), 12);
        for (int i = 0; i < maxAmount; i++) {
            final AudioTrack track = tracks.get(i);
            final AudioTrackInfo info = track.getInfo();
            String title = info.title.length() > 55 ? info.title.substring(0, 52) + "..." : info.title;
            String formattedString = String.format("**#%s** - [%s](%s)", i + 1, title, info.uri);
            description.append(formattedString).append("\n");
        }
        if (tracks.size() > maxAmount) {
            description.append("\n").append(String.format("...and %s more tracks.", tracks.size() - maxAmount));
        }

        interaction.reply(new EmbedBuilder().setTitle("New Queue:")
            .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
            .setAuthor("Shuffled the queue.")
            .setDescription(description).build(), false);
    }
}
