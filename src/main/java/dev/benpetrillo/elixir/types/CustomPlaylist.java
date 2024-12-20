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

package dev.benpetrillo.elixir.types;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.utils.TrackUtil;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public final class CustomPlaylist {

    public Info info;
    public List<CustomPlaylistTrack> tracks;
    public Options options;

    public static CustomPlaylist create(String playlistId, Member creator) {
        CustomPlaylist playlist = new CustomPlaylist();
        playlist.info = new Info();
        playlist.tracks = new ArrayList<>();
        playlist.options = new Options();
        playlist.info.id = playlistId;
        playlist.info.name = creator.getEffectiveName() + "'s Playlist";
        playlist.info.description = "A cool playlist by " + creator.getEffectiveName() + "!";
        playlist.info.playlistCoverUrl = Config.get("AVATAR-URL");
        playlist.info.author = creator.getId();
        return playlist;
    }

    public static class Info {
        public String id, name, description, playlistCoverUrl, author;
        public int volume = 100;
    }

    public static class CustomPlaylistTrack {

        public String title, url, artist, coverArt;
        public long duration;
        public String isrc = null;

        public static CustomPlaylistTrack from(AudioTrackInfo info) {
            CustomPlaylistTrack track = new CustomPlaylistTrack();
            track.title = info.title;
            track.url = info.uri.contains("spotify") ? "https://open.spotify.com/track/" + info.uri.split("/")[5] : info.uri;
            track.artist = info.author;
            track.coverArt = TrackUtil.getCoverArt(info);
            track.duration = info.length;
            if (info instanceof ExtendedAudioTrackInfo) {
                track.isrc = ((ExtendedAudioTrackInfo) info).isrc;
            }
            return track;
        }
    }

    public static class Options {
        public boolean shuffle = false, repeat = false;
    }
}
