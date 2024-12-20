/*
 * Copyright © 2024 Ben Petrillo, KingRainbow44.
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
 * All portions of this software are available for public use,
 * provided that credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.commands.music;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.utils.Embed;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

public final class JoinCommand extends Command {

    public JoinCommand() {
        super("join", "Bind Elixir to your voice channel.");
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
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        final AudioChannel audioChannel = memberVoiceState.getChannel();
        assert voiceState != null;
        assert audioChannel != null;
        if (!voiceState.inAudioChannel()) {
            audioManager.openAudioConnection(audioChannel);
            audioManager.setSelfDeafened(true);
            String name = audioChannel.getName();
            MessageEmbed embed = Embed.def(String.format("I've connected to **%s** successfully.", name));
            interaction.reply(embed, false);

            // Create a guild music manager if needed.
            ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        } else {
            interaction.reply(Embed.error("I'm already connected to a voice channel."), false);
        }
    }
}
