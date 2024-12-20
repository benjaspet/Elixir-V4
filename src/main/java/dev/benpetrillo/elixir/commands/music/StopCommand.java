/*
 * Copyright © 202 Ben Petrillo. All rights reserved.
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

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.AudioUtil;
import dev.benpetrillo.elixir.utils.Embed;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class StopCommand extends Command implements Arguments {

    public StopCommand() {
        super("stop", "Stop the current track & clear the queue.");
    }

    @Override
    public void execute(Interaction interaction) {

        assert interaction.getMember() != null;
        final GuildVoiceState selfVoiceState = interaction.getMember().getVoiceState();
        assert selfVoiceState != null;
        if (AudioUtil.audioCheck(interaction)) return;
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final AudioManager audioManager = interaction.getGuild().getAudioManager();

        // Check if the bot should leave.
        var shouldLeave = interaction.getArgument("leave", true, Boolean.class);
        if (shouldLeave && selfVoiceState.inAudioChannel()) {
            audioManager.closeAudioConnection();
        }

        musicManager.stop(); // Stop the music.
        ElixirMusicManager.getInstance() // Remove the guild music manager.
            .removeGuildMusicManager(interaction.getGuild());
        interaction.reply(Embed.def("The queue has been cleared" +
            (shouldLeave ? " and the player has been stopped" : "") + "."));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
            Argument.create("leave", "Should the bot leave the channel?", "leave", OptionType.BOOLEAN, false, 0)
        );
    }
}
