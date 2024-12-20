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

package dev.benpetrillo.elixir.utils;

import com.sedmelluq.discord.lavaplayer.source.soundcloud.*;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ExtendedAudioTrackInfo;
import dev.benpetrillo.elixir.types.YTVideoData;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TrackUtil {

    private static final SoundCloudDataLoader soundCloudDataLoader;
    private static final SoundCloudDataReader soundCloudDataReader;
    private static final SoundCloudFormatHandler formatHandler;

    static {
        soundCloudDataLoader = new DefaultSoundCloudDataLoader();
        soundCloudDataReader = new DefaultSoundCloudDataReader();
        formatHandler = new DefaultSoundCloudFormatHandler();
    }

    /**
     * Returns a URL of a track/video's cover art/thumbnail.
     *
     * @param track The AudioTrack to fetch.
     * @return String
     */

    public static String getCoverArt(AudioTrackInfo track) {
        var trackUri = track.uri;
        switch (TrackUtil.determineTrackType(trackUri)) {
            default -> {
                return null;
            }
            case SPOTIFY -> {
                String artUrl = null;
                try {
                    String[] firstSplit = trackUri.split("/");
                    String[] secondSplit;
                    String id;
                    if (firstSplit.length > 5) {
                        secondSplit = firstSplit[6].split("\\?");
                    } else {
                        secondSplit = firstSplit[4].split("\\?");
                    }
                    id = secondSplit[0];
                    GetTrackRequest trackRequest = SpotifySourceManager.getSpotify().getTrack(id).build();
                    Track spotifyTrack = trackRequest.execute();
                    Image thumbnail = spotifyTrack.getAlbum().getImages()[0];
                    artUrl = thumbnail.getUrl();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return artUrl;
            }
            case YOUTUBE -> {
                final YTVideoData data = HttpUtil.getVideoData(Utilities.extractVideoId(track.uri));
                return data != null ? data.items.get(0).snippet.thumbnails.get("default").get("url") : null;
            }
            case SOUNDCLOUD -> {
                try (HttpInterface httpInterface = ElixirMusicManager.getInstance().soundCloudSource.getHttpInterface()) {
                    JsonBrowser rootData = soundCloudDataLoader.load(httpInterface, trackUri);
                    JsonBrowser trackData = soundCloudDataReader.findTrackData(rootData);
                    if (trackData == null) {
                        return null;
                    }
                    return trackData.get("artwork_url").text();
                } catch (IOException e) {
                    return null;
                }
            }
        }
    }

    /**
     * Get track data from a Spotify URL.
     *
     * @param url The Spotify URL.
     * @return Track
     */

    public static Track getTrackDataFromSpotifyURL(String url) {
        try {
            String[] firstSplit = url.split("/");
            String[] secondSplit;
            String id;
            if (firstSplit.length > 5) {
                secondSplit = firstSplit[6].split("\\?");
            } else {
                secondSplit = firstSplit[4].split("\\?");
            }
            id = secondSplit[0];
            GetTrackRequest trackRequest = SpotifySourceManager.getSpotify().getTrack(id).build();
            return trackRequest.execute();
        } catch (SpotifyWebApiException | IOException | ParseException | NullPointerException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Get playlist data from a Spotify URL.
     *
     * @param url The Spotify URL.
     * @return Playlist
     */

    public static List<se.michaelthelin.spotify.model_objects.specification.PlaylistTrack> getPlaylistDataFromSpotifyUrl(String url) {
        try {
            String[] firstSplit = url.split("/");
            String[] secondSplit;
            String id;
            if (firstSplit.length > 5) {
                secondSplit = firstSplit[6].split("\\?");
            } else {
                secondSplit = firstSplit[4].split("\\?");
            }
            id = secondSplit[0];
            GetPlaylistsItemsRequest playlistRequest = SpotifySourceManager.getSpotify().getPlaylistsItems(id).build();
            Paging<se.michaelthelin.spotify.model_objects.specification.PlaylistTrack> tracks = playlistRequest.execute();
            return List.of(tracks.getItems());
        } catch (SpotifyWebApiException | IOException | ParseException | NullPointerException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an {@link AudioTrackInfo} object from a given URL.
     *
     * @param url The URL to fetch information from.
     * @return An AudioTrackInfo object.
     */

    public static AudioTrackInfo getTrackInfoFromUrl(String url) {
        TrackType type = TrackUtil.determineTrackType(url);
        switch (type) {
            case SPOTIFY -> {
                Track track = TrackUtil.getTrackDataFromSpotifyURL(url);
                if (track == null) return null;
                var trackInfo = new ExtendedAudioTrackInfo(
                    track.getName(), track.getArtists()[0].getName(),
                    track.getDurationMs(), track.getId(), false,
                    track.getHref()
                );
                trackInfo.isrc = track.getExternalIds().getExternalIds().getOrDefault("isrc", null);
                return trackInfo;
            }
            case YOUTUBE -> {
                YTVideoData searchData = HttpUtil.getVideoData(Utilities.extractVideoId(url));
                if (searchData == null) return null;
                YTVideoData.Item.Snippet query = searchData.items.get(0).snippet;
                long length = Utilities.cleanYouTubeFormat(searchData.items.get(0).contentDetails.duration);
                return new AudioTrackInfo(
                    query.title, query.channelTitle, length,
                    searchData.items.get(0).id, false,
                    "https://youtu.be/" + searchData.items.get(0).id
                );
            }
            case SOUNDCLOUD -> {
                try (HttpInterface httpInterface = ElixirMusicManager.getInstance().soundCloudSource.getHttpInterface()) {
                    JsonBrowser rootData = soundCloudDataLoader.load(httpInterface, url);
                    JsonBrowser trackData = soundCloudDataReader.findTrackData(rootData);

                    if (trackData == null) {
                        return null;
                    }

                    SoundCloudTrackFormat format = formatHandler.chooseBestFormat(soundCloudDataReader.readTrackFormats(trackData));
                    return soundCloudDataReader.readTrackInfo(trackData, formatHandler.buildFormatIdentifier(format));
                } catch (IOException e) {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Creates a collection of {@link AudioTrackInfo} objects from a given URL.
     *
     * @param url The URL to fetch information from.
     * @return A collection of AudioTrackInfo objects.
     */

    public static Collection<AudioTrackInfo> getPlaylistInfoFromUrl(String url) {
        TrackType type = TrackUtil.determineTrackType(url);
        Collection<AudioTrackInfo> trackInfoCollection = new ArrayList<>();
        switch (type) {
            case SPOTIFY -> {
                List<se.michaelthelin.spotify.model_objects.specification.PlaylistTrack> tracks = TrackUtil.getPlaylistDataFromSpotifyUrl(url);
                if (tracks == null) return null;
                for (se.michaelthelin.spotify.model_objects.specification.PlaylistTrack track : tracks) {
                    Track playlistItem = (Track) track.getTrack();
                    ExtendedAudioTrackInfo trackInfo = new ExtendedAudioTrackInfo(
                        playlistItem.getName(), playlistItem.getArtists()[0].getName(),
                        playlistItem.getDurationMs(), playlistItem.getId(), false,
                        playlistItem.getHref()
                    );
                    trackInfo.isrc = playlistItem.getExternalIds().getExternalIds().getOrDefault("isrc", null);
                    trackInfoCollection.add(trackInfo);
                }
            }
            case YOUTUBE -> {
                YTVideoData searchData = HttpUtil.getPlaylistData(Utilities.extractPlaylistId(url));
                for (YTVideoData.Item video : searchData.items) {
                    YTVideoData.Item.Snippet query = video.snippet;
                    long length = Utilities.cleanYouTubeFormat(video.contentDetails.duration);
                    trackInfoCollection.add(new AudioTrackInfo(
                        query.title, query.channelTitle, length,
                        video.id, false,
                        "https://youtu.be/" + video.id
                    ));
                }
            }
            default -> {
                return null;
            }
        }
        return trackInfoCollection;
    }

    /**
     * Appends a user ID to a track.
     *
     * @param userId The user ID to add to the track.
     * @param tracks The track(s) to append the user ID to.
     */

    public static void appendUser(String userId, List<PlaylistTrack> tracks) {
        for (AudioTrack track : tracks) {
            track.setUserData(userId);
        }
    }

    /**
     * Determines the source for a given URL.
     *
     * @param url The URL to find the source of.
     * @return A {@link TrackType} representing the source of the URL.
     */

    public static TrackType determineTrackType(String url) {
        if (url.contains("youtu")) return TrackType.YOUTUBE;
        if (url.contains("spotify")) return TrackType.SPOTIFY;
        if (url.contains("soundcloud")) return TrackType.SOUNDCLOUD;
        if (url.contains("laudiolin")) return TrackType.LAUDIOLIN;
        if (url.contains("file")) return TrackType.CUSTOM;
        return TrackType.UNKNOWN;
    }

    public enum TrackType {
        YOUTUBE,
        SPOTIFY,
        SOUNDCLOUD,
        LAUDIOLIN,
        CUSTOM,
        UNKNOWN
    }
}
