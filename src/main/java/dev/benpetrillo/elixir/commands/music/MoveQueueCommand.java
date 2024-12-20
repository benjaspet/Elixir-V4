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
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.Embed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class MoveQueueCommand extends Command implements Arguments {
    public MoveQueueCommand() {
        super("movequeue", "Moves the server's existing queue to another server.");
    }

    @Override
    public void execute(Interaction interaction) {
        final String guildId = interaction.getArgument("guild", String.class);
        final Guild guild = ElixirClient.getJda().getGuildById(guildId);
        if (guild == null) {
            interaction.reply(Embed.error("The guild with the specified ID doesn't exist."));
            return;
        }
        final AudioManager targetAudioManager = guild.getAudioManager();
        if (!targetAudioManager.isConnected()) {
            interaction.reply(Embed.error("Connect the bot to a voice channel before moving queues."));
            return;
        }
        GuildMusicManager targetMusicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        final BlockingQueue<AudioTrack> targetQueue = targetMusicManager.scheduler.queue;
        final AudioPlayer targetPlayer = targetMusicManager.audioPlayer;
        if (!targetQueue.isEmpty() || targetPlayer.getPlayingTrack() != null) {
            interaction.reply(Embed.error(guild.getName() + "'s Elixir is in use!"));
            return;
        }
        assert interaction.getGuild() != null;
        final GuildMusicManager sourceMusicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final BlockingQueue<AudioTrack> sourceQueue = sourceMusicManager.scheduler.queue;
        final AudioPlayer sourcePlayer = sourceMusicManager.audioPlayer;
        targetQueue.addAll(sourceQueue);
        targetPlayer.setVolume(sourcePlayer.getVolume());
        final AudioTrack newTrack = sourcePlayer.getPlayingTrack().makeClone();
        newTrack.setPosition(sourcePlayer.getPlayingTrack().getPosition());
        targetPlayer.playTrack(newTrack);
        final AudioManager sourceAudioManager = interaction.getGuild().getAudioManager();
        if (sourceAudioManager.isConnected()) {
            sourceMusicManager.stop();
            sourceAudioManager.closeAudioConnection();
        }
        interaction.reply(Embed.def("Moved the queue to `" + guild.getName() + "`."));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("guild", "The guild to move the queue to.", "guild", OptionType.STRING, true, 0)
        );
    }
}
