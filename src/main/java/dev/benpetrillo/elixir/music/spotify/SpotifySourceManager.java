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

package dev.benpetrillo.elixir.music.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.readNullableText;
import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.writeNullableText;

public final class SpotifySourceManager implements AudioSourceManager {

    public static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("(https?://)?(www\\.)?open\\.spotify\\.com/(user/[a-zA-Z0-9-_]+/)?(?<type>track|album|playlist|artist)/(?<identifier>[a-zA-Z0-9-_]+)");
    public static final String SEARCH_PREFIX = "spsearch:";

    private static SpotifyApi spotify;
    private final AudioSourceManager searchAudioSourceManager;

    public SpotifySourceManager(AudioSourceManager searchAudioSourceManager) {
        if (Config.get("SPOTIFY-CLIENT-ID") == null || Config.get("SPOTIFY-CLIENT-ID").isEmpty()) {
            throw new IllegalArgumentException("Spotify client ID must be set.");
        }
        if (Config.get("SPOTIFY-CLIENT-SECRET") == null || Config.get("SPOTIFY-CLIENT-SECRET").isEmpty()) {
            throw new IllegalArgumentException("Spotify secret must be set.");
        }
        try {
            SpotifySourceManager.authorize();
        } catch (IOException | ParseException | SpotifyWebApiException exception) {
            exception.printStackTrace();
        }
        this.searchAudioSourceManager = searchAudioSourceManager;
    }

    public static void authorize() throws IOException, ParseException, SpotifyWebApiException {
        spotify = new SpotifyApi.Builder()
            .setClientId(Config.get("SPOTIFY-CLIENT-ID"))
            .setClientSecret(Config.get("SPOTIFY-CLIENT-SECRET"))
            .build();
        final ClientCredentialsRequest.Builder credRequest =
            new ClientCredentialsRequest.Builder(spotify.getClientId(), spotify.getClientSecret());
        final ClientCredentials credentials = credRequest.grant_type("client_credentials").build().execute();
        spotify.setAccessToken(credentials.getAccessToken());
        ElixirClient.logger.info("Successfully updated Spotify OAuth access token.");
    }

    public static SpotifyApi getSpotify() {
        return spotify;
    }

    public AudioSourceManager getSearchSourceManager() {
        return this.searchAudioSourceManager;
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            if (reference.identifier.startsWith(SEARCH_PREFIX)) {
                return this.getSearch(reference.identifier.substring(SEARCH_PREFIX.length()).trim());
            }
            final Matcher matcher = SPOTIFY_URL_PATTERN.matcher(reference.identifier);
            if (!matcher.find()) return null;
            final String id = matcher.group("identifier");
            switch (matcher.group("type")) {
                case "album" -> {
                    return this.getAlbum(id);
                }
                case "track" -> {
                    return this.getTrack(id);
                }
                case "playlist" -> {
                    return this.getPlaylist(id);
                }
                case "artist" -> {
                    return this.getArtist(id);
                }
            }
        } catch (IOException | ParseException | SpotifyWebApiException | NullPointerException e) {
            return null;
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        final SpotifyTrack spotifyTrack = (SpotifyTrack) track;
        try {
            writeNullableText(output, spotifyTrack.getISRC());
            writeNullableText(output, spotifyTrack.getArtworkURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        String isrc = null, artworkURL = null;
        try {
            isrc = readNullableText(input);
            artworkURL = readNullableText(input);
        } catch (IOException ignored) {
        }
        return new SpotifyTrack(trackInfo, isrc, artworkURL, this);
    }

    @Override
    public void shutdown() {
    }

    public AudioItem getSearch(String query) throws IOException, ParseException, SpotifyWebApiException {
        final Paging<Track> searchResult = spotify.searchTracks(query).build().execute();
        if (searchResult.getItems().length == 0) {
            return AudioReference.NO_TRACK;
        }
        var tracks = new ArrayList<AudioTrack>();
        for (var item : searchResult.getItems()) {
            tracks.add(SpotifyTrack.of(item, this));
        }
        return new BasicAudioPlaylist("Search results for: " + query, tracks, null, true);
    }

    public AudioItem getTrack(String id) throws IOException, ParseException, SpotifyWebApiException {
        final Track track = spotify.getTrack(id).build().execute();
        return SpotifyTrack.of(track, this);
    }

    public AudioItem getAlbum(String id) throws IOException, ParseException, SpotifyWebApiException {
        final Album album = spotify.getAlbum(id).build().execute();
        var tracks = new ArrayList<AudioTrack>();
        Paging<TrackSimplified> paging = null;
        do {
            paging = spotify.getAlbumsTracks(id).limit(50).offset(paging == null ? 0 : paging.getOffset() + 50).build().execute();
            for (var item : paging.getItems()) {
                if (item.getType() != ModelObjectType.TRACK) {
                    continue;
                }
                tracks.add(SpotifyTrack.of(item, album, this));
            }
        }
        while (paging.getNext() != null);
        return new BasicAudioPlaylist(album.getName(), tracks, null, false);
    }

    public AudioItem getPlaylist(String id) throws IOException, SpotifyWebApiException, ParseException, NullPointerException {
        final Playlist playlist = spotify.getPlaylist(id).build().execute();
        var tracks = new ArrayList<AudioTrack>();
        Paging<PlaylistTrack> paging = null;
        do {
            paging = spotify.getPlaylistsItems(id).limit(50).offset(paging == null ? 0 : paging.getOffset() + 50).build().execute();
            for (var item : paging.getItems()) {
                if (item.getIsLocal() || item.getTrack().getType() != ModelObjectType.TRACK) {
                    continue;
                }
                tracks.add(SpotifyTrack.of((Track) item.getTrack(), this));
            }
        }
        while (paging.getNext() != null);
        return new BasicAudioPlaylist(playlist.getName(), tracks, null, false);
    }

    public AudioItem getArtist(String id) throws IOException, ParseException, SpotifyWebApiException {
        final Artist artist = spotify.getArtist(id).build().execute();
        final Track[] artistTracks = spotify.getArtistsTopTracks(id, CountryCode.US).build().execute();
        var tracks = new ArrayList<AudioTrack>();
        for (var item : artistTracks) {
            if (item.getType() != ModelObjectType.TRACK) continue;
            tracks.add(SpotifyTrack.of(item, this));
        }
        return new BasicAudioPlaylist(artist.getName() + "'s Top Tracks", tracks, null, false);
    }
}
