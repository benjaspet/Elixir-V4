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

package dev.benpetrillo.elixir.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public final class ShutdownListener extends ListenerAdapter {

    @Override
    public void onShutdown(ShutdownEvent event) {
        final JDA jda = event.getJDA();
        for (Guild guild : jda.getGuildCache()) {
            Member self = guild.getSelfMember();
            try {
                GuildVoiceState voiceState = Objects.requireNonNull(self.getVoiceState());
                Objects.requireNonNull(voiceState.getChannel());
                final VoiceChannel voiceChannel = voiceState.getChannel().asVoiceChannel();
                final AudioManager audioManager = voiceChannel.getGuild().getAudioManager();
                audioManager.closeAudioConnection();

            } catch (NullPointerException ignored) {
            }
        }
    }
}
