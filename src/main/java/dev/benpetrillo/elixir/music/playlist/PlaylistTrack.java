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

package dev.benpetrillo.elixir.music.playlist;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.music.spotify.SpotifyTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist.CustomPlaylistTrack;
import dev.benpetrillo.elixir.utils.TrackUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.track.YoutubeAudioTrack;

public final class PlaylistTrack extends DelegatedAudioTrack {

    private final String isrc;
    private final AudioSourceManager sourceManager;
    private final CustomPlaylistTrack trackObject;
    private long length;

    public PlaylistTrack(String title, CustomPlaylistTrack from, AudioSourceManager sourceManager) {
        super(new AudioTrackInfo(
            title, from.artist, from.duration,
            switch (TrackUtil.determineTrackType(from.url)) {
                default -> from.url;
                case YOUTUBE -> Utilities.extractVideoId(from.url);
                case SPOTIFY -> Utilities.extractSongId(from.url);
            }, false, from.url
        ));
        this.isrc = from.isrc;
        this.sourceManager = sourceManager;
        this.trackObject = from;
        this.length = from.duration;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        DelegatedAudioTrack track = null;
        switch (TrackUtil.determineTrackType(this.getInfo().uri)) {
            case YOUTUBE -> {
                track = new YoutubeAudioTrack(
                    this.trackInfo, (YoutubeAudioSourceManager) this.sourceManager
                );
                this.length = track.getDuration();
            }
            case SPOTIFY -> {
                track = new SpotifyTrack(
                    this.trackInfo, this.isrc, this.trackObject.coverArt, (SpotifySourceManager) this.sourceManager
                );
                this.length = track.getDuration();
            }
            case SOUNDCLOUD -> {
                track = new SoundCloudAudioTrack(
                    this.trackInfo, (SoundCloudAudioSourceManager) this.sourceManager
                );
                this.length = track.getDuration();
            }
        }
        if (track != null) track.process(executor);
    }

    @Override
    public long getDuration() {
        return this.length;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new PlaylistTrack(this.trackInfo.title, this.trackObject, this.sourceManager);
    }
}
