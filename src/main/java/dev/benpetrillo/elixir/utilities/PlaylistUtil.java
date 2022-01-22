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

package dev.benpetrillo.elixir.utilities;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PlaylistUtil {
    /**
     * Determines if the member is an author of the playlist.
     * @param playlistId The playlist ID to check.
     * @param member The member to check.
     * @return The result of the check.
     */
    
    public static boolean isAuthor(String playlistId, Member member) {
        return false;
    }

    /**
     * Get a collection of tracks in a custom playlist.
     * @param playlistId The playlist ID to get tracks from.
     * @return A collection of playable tracks.
     */
    
    public static List<PlaylistTrack> getTracks(String playlistId) {
        // TODO: Implement getting playlist data from the database.
        CustomPlaylist playlist = Utilities.deserialize("{}", CustomPlaylist.class);
        final List<PlaylistTrack> tracks = new ArrayList<>();
        
        for(Map.Entry<String, CustomPlaylist.CustomPlaylistTrack> entry : playlist.tracks.entrySet()) {
            CustomPlaylist.CustomPlaylistTrack track = entry.getValue();
            TrackUtil.TrackType trackType = TrackUtil.determineTrackType(track.url);
            AudioSourceManager source = switch (trackType) {
                case YOUTUBE -> ElixirMusicManager.getInstance().youtubeSource;
                case SPOTIFY -> ElixirMusicManager.getInstance().spotifySource;
                default -> throw new IllegalArgumentException("Unsupported track URL: " + track.url);
            };
            
            tracks.add(new PlaylistTrack(entry.getKey(), track, source));
        }
        
        return tracks;
    }

    /**
     * Adds a track to a custom playlist.
     * @param track The track to add.
     * @param playlistId The playlist ID to add the track to.
     */
    
    public static void addTrackToList(AudioTrack track, String playlistId) {
        // TODO: Implement getting playlist data from the database.
        CustomPlaylist playlist = Utilities.deserialize("{}", CustomPlaylist.class);
        playlist.tracks.put(track.getInfo().title, CustomPlaylist.CustomPlaylistTrack.from(track.getInfo()));
        
        var newData = Utilities.serialize(playlist);
        // TODO: Set data for the playlist to newData.
    }
}