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

package dev.benpetrillo.elixir.utils;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import tech.xigam.cch.utils.Interaction;

import java.util.Objects;

public final class AudioUtil {

    /**
     * Runs the simple check to ensure if:
     * - The user is in a voice channel
     * - The bot is in a voice channel
     * - The user is in the same voice channel as the bot
     */

    public static FailureReason simpleAudioCheck(Guild guild, Member member) {
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        assert selfVoiceState != null;
        if (member == null) {
            return selfVoiceState.inAudioChannel() ? FailureReason.PASSED : FailureReason.BOT_NOT_IN_VOICE_CHANNEL;
        } else {
            final GuildVoiceState memberVoiceState = member.getVoiceState();
            assert memberVoiceState != null;
            if (!memberVoiceState.inAudioChannel()) {
                return FailureReason.MEMBER_NOT_IN_VOICE_CHANNEL;
            }
            if (!selfVoiceState.inAudioChannel()) {
                return FailureReason.BOT_NOT_IN_VOICE_CHANNEL;
            }
            if (!Objects.equals(selfVoiceState.getChannel(), memberVoiceState.getChannel())) {
                return FailureReason.BOT_NOT_IN_SAME_VOICE_CHANNEL;
            }
            return FailureReason.PASSED;
        }
    }

    /**
     * Performs a {@link #simpleAudioCheck(Guild, Member)} and replies on failure.
     */

    public static boolean audioCheck(Interaction interaction) {
        assert interaction.getGuild() != null;
        AudioUtil.FailureReason reason = AudioUtil.simpleAudioCheck(interaction.getGuild(), interaction.getMember());
        switch (reason) {
            case BOT_NOT_IN_VOICE_CHANNEL -> {
                interaction.reply(Embed.error("I must be in a voice channel."));
                return true;
            }
            case MEMBER_NOT_IN_VOICE_CHANNEL -> {
                interaction.reply(Embed.error("You must be in a voice channel."));
                return true;
            }
            case BOT_NOT_IN_SAME_VOICE_CHANNEL -> {
                interaction.reply(Embed.error("You need to be in my voice channel."));
                return true;
            }
        }
        return false;
    }

    public static FailureReason simplePlayerCheck(Guild guild) {
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        return musicManager.audioPlayer.getPlayingTrack() == null ? FailureReason.BOT_IS_NOT_PLAYING : FailureReason.PASSED;
    }

    public static boolean playerCheck(Interaction interaction, ReturnMessage message) {
        if (message == null) {
            message = ReturnMessage.NO_QUEUE;
        }
        AudioUtil.FailureReason reason = AudioUtil.simplePlayerCheck(interaction.getGuild());
        if (reason == FailureReason.BOT_IS_NOT_PLAYING) {
            interaction.reply(Embed.error(message.getContents()));
        }
        return reason != FailureReason.PASSED;
    }

    public enum FailureReason {
        PASSED,
        BOT_IS_NOT_PLAYING,
        BOT_NOT_IN_VOICE_CHANNEL,
        MEMBER_NOT_IN_VOICE_CHANNEL,
        BOT_NOT_IN_SAME_VOICE_CHANNEL
    }

    public enum ReturnMessage {

        NO_QUEUE("There's no queue in this server."),
        NOT_PLAYING("There is nothing playing.");

        private final String contents;

        ReturnMessage(String contents) {
            this.contents = contents;
        }

        public String getContents() {
            return contents;
        }
    }
}
