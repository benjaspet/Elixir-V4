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

package dev.benpetrillo.elixir.managers;

import com.grack.nanojson.JsonObject;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.objects.Pair;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utils.Embed;
import dev.benpetrillo.elixir.utils.Utilities;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.http.YoutubeOauth2Handler;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.hc.core5.annotation.Internal;
import org.jetbrains.annotations.Nullable;
import tech.xigam.cch.utils.Interaction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public final class ElixirMusicManager {

    private static ElixirMusicManager instance;
    public final YoutubeAudioSourceManager youtubeSource = new YoutubeAudioSourceManager();
    public final SpotifySourceManager spotifySource = new SpotifySourceManager(youtubeSource);
    public final HttpAudioSourceManager httpSource = new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY);
    public final SoundCloudAudioSourceManager soundCloudSource = SoundCloudAudioSourceManager.createDefault();
    private final Map<String, GuildMusicManager> musicManagers = new HashMap<>();
    @Getter
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    @Getter
    @Setter
    private boolean youtubeConfigured = false;

    public ElixirMusicManager() {
        this.configure();
    }

    /**
     * @return The current, or new instance of the music manager.
     */
    public static ElixirMusicManager getInstance() {
        if (instance == null) {
            instance = new ElixirMusicManager();
        }

        return instance;
    }

    /**
     * Configures the source managers for Elixir.
     */
    private void configure() {
        // Configure YouTube using potential credentials.
        this.configureYouTube();

        this.audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(this.youtubeSource);
        this.audioPlayerManager.registerSourceManager(this.spotifySource);
        this.audioPlayerManager.registerSourceManager(this.soundCloudSource);
        this.audioPlayerManager.registerSourceManager(this.httpSource);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        ElixirClient.logger.info("Source managers registered.");

        // IPv6 rotation setup.
        // If the bot receives a 429 from YouTube, it will rotate the IP address to another, provided that
        // an entire 64-bit block is provided in the config.

        if (!ElixirConstants.IPV6_BLOCK.isEmpty()) {
            new YoutubeIpRotatorSetup(
                new NanoIpRoutePlanner(Collections.singletonList(new Ipv6Block(ElixirConstants.IPV6_BLOCK)), true))
                .forManager(this.audioPlayerManager)
                .withMainDelegateFilter(null)
                .setup();
            ElixirClient.logger.info("IPv6 rotator block set to {}.", ElixirConstants.IPV6_BLOCK);
        } else {
            ElixirClient.logger.warn("You are not using an IPv6 rotator. This may cause issues with YouTube and rate-limiting.");
        }
    }

    /**
     * Configures YouTube using potential credentials on the file system.
     */
    private void configureYouTube() {
        // Check if the file exists.
        if (Files.exists(ElixirConstants.YT_CREDENTIALS)) {
            try {
                // Read the file.
                var token = Files.readString(ElixirConstants.YT_CREDENTIALS);
                this.youtubeSource.useOauth2(token, true);

                ElixirClient.logger.info("OAuth2 for YouTube enabled.");
                this.setYoutubeConfigured(true);
                return;
            } catch (Exception exception) {
                ElixirClient.logger.warn("Failed to read YouTube OAuth2 credentials.");
                ElixirClient.logger.debug("Failed to read YouTube OAuth2 credentials.", exception);
            }
        }

        ElixirClient.getLogger().warn("YouTube OAuth2 credentials not found.");
        ElixirClient.getLogger().warn("Please use the '/configure' command to set up YouTube!");
    }

    /**
     * Begins the YouTube OAuth2 authorization flow.
     *
     * @param callback Invoked when the user has successfully authenticated.
     * @return The user code for the developer to log in with.
     */
    public Pair<String, String> doAuthFlow(Consumer<Void> callback) {
        try {
            // Get the oauth2 handler instance.
            var handlerField = YoutubeAudioSourceManager.class
                .getDeclaredField("oauth2Handler");
            handlerField.setAccessible(true);

            var handler = handlerField.get(this.youtubeSource);

            // Get the device code method.
            var fetchDeviceCode = YoutubeOauth2Handler.class
                .getDeclaredMethod("fetchDeviceCode");
            fetchDeviceCode.setAccessible(true);

            // Invoke the method.
            var response = (JsonObject) fetchDeviceCode.invoke(handler);

            // Extract data.
            var url = response.getString("verification_url");
            var userCode = response.getString("user_code");

            var deviceCode = response.getString("device_code");
            var interval = response.getLong("interval") * 1000;

            // Get the poll method.
            var pollForToken = YoutubeOauth2Handler.class
                .getDeclaredMethod("pollForToken", String.class, long.class);
            pollForToken.setAccessible(true);

            // Begin thread to poll for token acceptance.
            new Thread(() -> {
                try {
                    // Poll for the token.
                    pollForToken.invoke(handler,
                        deviceCode,
                        interval == 0 ? 5000 : interval);

                    // Once the above method finishes, invoke the callback.
                    callback.accept(null);

                    // Set the YouTube configuration to true.
                    this.setYoutubeConfigured(true);
                } catch (Exception ignored) {
                }
            }, "youtube-source-token-poller").start();

            return Pair.of(url, userCode);
        } catch (Exception exception) {
            ElixirClient.getLogger().warn("Failed to fetch device code for YouTube OAuth2.", exception);
            return null;
        }
    }

    /**
     * Saves the YouTube OAuth2 credentials.
     */
    @SneakyThrows
    public void saveCredentials() throws IOException {
        var token = this.youtubeSource.getOauth2RefreshToken();
        if (token == null) {
            throw new Exception("Invalid refresh token.");
        }

        ElixirClient.getLogger().info("Saving YouTube OAuth2 credentials...");
        Files.writeString(ElixirConstants.YT_CREDENTIALS, token);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getId(), (guildId) -> {
            var guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void removeGuildMusicManager(Guild guild) {
        this.musicManagers.remove(guild.getId());
    }

    @Nullable
    public GuildMusicManager getMusicManager(String guildId) {
        return this.musicManagers.get(guildId);
    }

    public Collection<GuildMusicManager> getMusicManagers() {
        return this.musicManagers.values();
    }

    public void loadAndPlay(String track, Interaction interaction, String url) {
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = this.getMusicManager(interaction.getGuild());
        ElixirClient.logger.debug("Loading track: {}", track);
        this.audioPlayerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                assert interaction.getMember() != null;
                track.setUserData(interaction.getMember().getId());
                musicManager.getScheduler().queue(track);
                final String title = track.getInfo().title;
                final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                MessageEmbed embed = new EmbedBuilder()
                    .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                    .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle.replaceAll("\\[|]]", ""), track.getInfo().uri))
                    .build();
                interaction.reply(embed, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (tracks.size() > 300) {
                    interaction.reply(Embed.error("Playlists that exceed 300 tracks cannot be played."));
                    return;
                }
                if (playlist.isSearchResult()) {
                    assert interaction.getMember() != null;
                    tracks.get(0).setUserData(interaction.getMember().getId());
                    final String title = tracks.get(0).getInfo().title;
                    final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                    MessageEmbed embed = new EmbedBuilder()
                        .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                        .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle.replaceAll("\\[|]]", ""), tracks.get(0).getInfo().uri))
                        .build();
                    interaction.reply(embed, false);
                    musicManager.scheduler.queue(tracks.get(0));
                } else {
                    final String success = String.format("Queued **%s** tracks from [%s](%s).", tracks.size(), playlist.getName(), url);
                    MessageEmbed embed = new EmbedBuilder()
                        .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                        .setDescription(success)
                        .build();
                    interaction.reply(embed);
                    for (final AudioTrack track : tracks) {
                        assert interaction.getMember() != null;
                        track.setUserData(interaction.getMember().getId());
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                interaction.reply(Embed.error("Nothing found by that search term."));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception));
                interaction.reply(Embed.error("An error occurred while attempting to play that track."));
            }
        });
    }

    @Internal
    public void loadAndPlay(Guild guild, String track, Consumer<Object> callback) {
        final GuildMusicManager musicManager = this.getMusicManager(guild);
        this.audioPlayerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioTrack.setUserData(ElixirClient.getId());
                musicManager.scheduler.queue(audioTrack);
                callback.accept(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (audioPlaylist.isSearchResult()) {
                    this.trackLoaded(tracks.get(0));
                } else {
                    for (final AudioTrack audioTrack : tracks) {
                        audioTrack.setUserData(ElixirClient.getId());
                        musicManager.scheduler.queue(audioTrack);
                    }
                    callback.accept(tracks);
                }
            }

            @Override
            public void noMatches() {
                ElixirClient.logger.debug("No matches found for: {}", track);
                callback.accept(null);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                ElixirClient.logger.debug("Failed to load: {}", track);
                callback.accept(e);
                Utilities.throwThrowable(new ElixirException().guild(guild).exception(e));
            }
        });
    }
}
