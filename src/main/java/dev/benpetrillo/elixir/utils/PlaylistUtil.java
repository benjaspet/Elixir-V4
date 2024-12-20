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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.DatabaseManager;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.types.ExtendedAudioTrackInfo;
import net.dv8tion.jda.api.entities.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public final class PlaylistUtil {

    /**
     * Create a custom playlist.
     *
     * @param playlistId The playlist ID.
     * @return If the playlist was created.
     */

    public static boolean createPlaylist(String playlistId, Member member) {
        if (findPlaylist(playlistId) != null) return false;
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        dbCollection.insertOne(new Document("playlistId", playlistId)
            .append("creatorId", member.getId())
            .append("playlistData", Utilities.base64Encode(Utilities.serialize(
                CustomPlaylist.create(playlistId, member)
            ))));
        return true;
    }

    /**
     * Deletes a custom playlist.
     *
     * @param playlistId The playlist ID.
     */

    public static void deletePlaylist(String playlistId) {
        if (findPlaylist(playlistId) == null) return;
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        dbCollection.deleteOne(new Document("playlistId", playlistId));
    }

    /**
     * Get a custom playlist object by ID.
     *
     * @param playlistId The playlist ID to get.
     * @return The playlist object or null if one cannot be found.
     */

    public static CustomPlaylist findPlaylist(String playlistId) {
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        BasicDBObject object = new BasicDBObject("playlistId", playlistId);
        Document document = dbCollection.find(object).first();
        if (document == null) return null;
        String serializedPlaylistData = document.getString("playlistData");
        return Utilities.deserialize(Utilities.base64Decode(serializedPlaylistData), CustomPlaylist.class);
    }

    /**
     * Gets all Elixir playlists in the database.
     *
     * @return A list of all Elixir playlists.
     */

    public static List<CustomPlaylist> getAllPlaylists() {
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        List<CustomPlaylist> playlists = new ArrayList<>();
        for (Document document : dbCollection.find()) {
            String serializedPlaylistData = document.getString("playlistData");
            playlists.add(Utilities.deserialize(Utilities.base64Decode(serializedPlaylistData), CustomPlaylist.class));
        }

        return playlists;
    }

    /**
     * Determines if the member is an author of the playlist.
     *
     * @param playlist The playlist to check.
     * @param member   The member to check.
     * @return The result of the check.
     */

    public static boolean isAuthor(CustomPlaylist playlist, Member member) {
        return playlist.info.author.matches(member.getId());
    }

    /**
     * Get a collection of tracks in a custom playlist.
     *
     * @param playlist The playlist to get tracks from.
     * @return A collection of playable tracks.
     */

    public static List<PlaylistTrack> getTracks(CustomPlaylist playlist) {
        final List<PlaylistTrack> tracks = new ArrayList<>();
        for (CustomPlaylist.CustomPlaylistTrack track : playlist.tracks) {
            TrackUtil.TrackType trackType = TrackUtil.determineTrackType(track.url);
            AudioSourceManager source = switch (trackType) {
                case YOUTUBE -> ElixirMusicManager.getInstance().youtubeSource;
                case SPOTIFY -> ElixirMusicManager.getInstance().spotifySource;
                case SOUNDCLOUD -> ElixirMusicManager.getInstance().soundCloudSource;
                default -> throw new IllegalArgumentException("Unsupported track URL: " + track.url);
            };
            tracks.add(new PlaylistTrack(track.title, track, source));
        }
        return tracks;
    }

    /**
     * Adds a track to a custom playlist.
     *
     * @param track    The track to add.
     * @param playlist The playlist to add the track to.
     */

    public static void addTrackToList(AudioTrackInfo track, CustomPlaylist playlist, int index) {
        if (track.uri == null) {
            try {
                var url = HttpUtil.searchForVideo(track.title);

                track = new ExtendedAudioTrackInfo(
                    track.title, track.author, track.length, track.identifier, track.isStream, url
                );
            } catch (Exception ignored) {
                return;
            }
        }
        var newTrack = CustomPlaylist.CustomPlaylistTrack.from(track);
        if (index == -1) {
            playlist.tracks.add(newTrack);
        } else {
            try {
                playlist.tracks.add(index, newTrack);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        updatePlaylist(playlist);
    }

    /**
     * Removes a track from a custom playlist.
     *
     * @param index    The track to remove.
     * @param playlist The playlist to remove the track from.
     */

    public static void removeTrackFromList(int index, CustomPlaylist playlist) throws IndexOutOfBoundsException {
        playlist.tracks.remove(index - 1);
        updatePlaylist(playlist);
    }

    /**
     * Sets the cover of a custom playlist.
     *
     * @param playlist The playlist to set the cover of.
     * @param url      The URL of the cover image.
     */

    public static void setPlaylistCover(CustomPlaylist playlist, String url) {
        playlist.info.playlistCoverUrl = url;
        updatePlaylist(playlist);
    }

    /**
     * Sets the name of a custom playlist.
     *
     * @param playlist The playlist to set the name of.
     * @param name     The name to set.
     */

    public static void setPlaylistName(CustomPlaylist playlist, String name) {
        playlist.info.name = name;
        updatePlaylist(playlist);
    }

    /**
     * Sets the description of a custom playlist.
     *
     * @param playlist    The playlist to set the description of.
     * @param description The description to set.
     */

    public static void setPlaylistDescription(CustomPlaylist playlist, String description) {
        playlist.info.description = description;
        updatePlaylist(playlist);
    }

    /**
     * Sets the volume to set the player to on startup. (for a custom playlist)
     *
     * @param playlist The playlist to set the starting volume of.
     * @param volume   The volume, as an integer, to set this playlist to.
     */

    public static void setPlaylistVolume(CustomPlaylist playlist, int volume) {
        playlist.info.volume = volume;
        updatePlaylist(playlist);
    }

    /**
     * Sets the author of a custom playlist.
     *
     * @param setting  The author to set.
     * @param playlist The playlist to set the author of.
     * @param value    The value to set.
     */

    public static void setPlaylistSetting(Setting setting, CustomPlaylist playlist, boolean value) {
        switch (setting) {
            case SHUFFLE -> playlist.options.shuffle = value;
            case REPEAT -> playlist.options.repeat = value;
        }
        updatePlaylist(playlist);
    }

    /**
     * Updates a custom playlist.
     *
     * @param playlist The playlist to update.
     */

    private static void updatePlaylist(CustomPlaylist playlist) {
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        Bson dbObject = new BasicDBObject("playlistId", playlist.info.id);
        Bson playlistObject = dbCollection.find(dbObject).first();
        if (playlistObject == null) return;
        var newData = Utilities.base64Encode(Utilities.serialize(playlist));
        var update = Updates.set("playlistData", newData);
        try {
            dbCollection.updateOne(dbObject, update, new UpdateOptions().upsert(true));
        } catch (MongoException ignored) {
        }
    }

    public enum Setting {
        SHUFFLE,
        REPEAT
    }
}
