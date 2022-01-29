/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
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

package dev.benpetrillo.elixir.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ElixirMusicManager {

    private static ElixirMusicManager instance;
    private final Map<String, GuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    
    public final YoutubeAudioSourceManager youtubeSource = new YoutubeAudioSourceManager();
    public final SpotifySourceManager spotifySource = new SpotifySourceManager(youtubeSource);

    public ElixirMusicManager() {
        this.audioPlayerManager.registerSourceManager(this.spotifySource);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getId(), (guildId) -> {
            GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }
    
    public GuildMusicManager[] getMusicManagers() {
        return this.musicManagers.values().toArray(new GuildMusicManager[0]);
    }

    public void loadAndPlay(TextChannel channel, String track, InteractionHook hook, String url) {
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(hook.getInteraction().getUser().getId());
                musicManager.scheduler.queue(track);
                final String title = track.getInfo().title;
                final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                MessageEmbed embed = new EmbedBuilder()
                        .setColor(EmbedUtil.getDefaultEmbedColor())
                        .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle, track.getInfo().uri))
                        .build();
                hook.editOriginalEmbeds(embed).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (playlist.isSearchResult()) {
                    tracks.get(0).setUserData(hook.getInteraction().getUser().getId());
                    final String title = tracks.get(0).getInfo().title;
                    final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(EmbedUtil.getDefaultEmbedColor())
                            .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle, tracks.get(0).getInfo().uri))
                            .build();
                    hook.editOriginalEmbeds(embed).queue();
                    musicManager.scheduler.queue(tracks.get(0));
                } else {
                    final String success = String.format("Queued **%s** tracks from [%s](%s).", tracks.size(), playlist.getName(), url);
                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(EmbedUtil.getDefaultEmbedColor())
                            .setDescription(success)
                            .build();
                    hook.editOriginalEmbeds(embed).queue();
                    for (final AudioTrack track : tracks) {
                        track.setUserData(hook.getInteraction().getUser().getId());
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("Nothing found by that search term.")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Utilities.throwThrowable(new ElixirException(channel.getGuild(), hook.getInteraction().getMember()).exception(exception));
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("An error occurred while attempting to play that track.")).queue();
            }
        });
    }

    public static ElixirMusicManager getInstance() {
        if (instance == null) instance = new ElixirMusicManager();
        return instance;
    }
}