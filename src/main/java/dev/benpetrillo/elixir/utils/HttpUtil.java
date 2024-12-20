/*
 * Copyright © 2023 Ben Petrillo. All rights reserved.
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

import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.types.YTPlaylistData;
import dev.benpetrillo.elixir.types.YTVideoData;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HttpUtil {

    @Getter
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Searches for a video on YouTube.
     *
     * @param query The search query.
     * @return The video's URL.
     */

    public static String searchForVideo(String query) {
        return "ytmsearch:" + query;
    }

    /**
     * Get data on a particular YouTube video by ID.
     *
     * @param videoId The video ID.
     * @return YTVideoData
     */

    public static YTVideoData getVideoData(String videoId) {
        var url = "https://www.googleapis.com/youtube/v3/videos?key=" +
            Config.get("YOUTUBE-API-KEY") +
            "&part=snippet%2CcontentDetails&id=" +
            videoId;
        var request = new Request.Builder()
            .url(url)
            .build();
        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            return Utilities.deserialize(response.body().string(), YTVideoData.class);
        } catch (IOException ex) {
            ElixirClient.getLogger().warn("Failed to get video data for video ID: " + videoId, ex);
            return null;
        }
    }

    /**
     * Get data from a YouTube playlist by its ID.
     *
     * @param playlistId The playlist ID.
     * @return YTVideoData
     */

    public static YTVideoData getPlaylistData(String playlistId) {
        boolean lastPage = false;
        String nextPageToken = null;
        List<YTPlaylistData> totalData = new ArrayList<>();
        while (!lastPage) {
            String url = "https://www.googleapis.com/youtube/v3/playlistItems?key=" + ElixirConstants.YOUTUBE_API_KEY +
                "&part=snippet%2CcontentDetails&maxResults=50&playlistId=" + playlistId;
            if (nextPageToken != null) url += "&pageToken=" + nextPageToken;
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                assert response.body() != null;
                var playlistData = Utilities.deserialize(
                    Objects.requireNonNull(response.body()).string(),
                    YTPlaylistData.class);
                totalData.add(playlistData);
                if (playlistData.nextPageToken != null) {
                    nextPageToken = playlistData.nextPageToken;
                } else lastPage = true;
            } catch (IOException ignored) {
                lastPage = true;
            }
        }
        var videoData = new YTVideoData();
        videoData.items = new ArrayList<>();
        for (var playlistData : totalData) {
            for (var playlistItem : playlistData.items) {
                //noinspection ConstantConditions
                videoData.items.add(HttpUtil.getVideoData(
                        playlistItem.snippet.resourceId.get("videoId"))
                    .items.get(0));
            }
        }
        return videoData;
    }
}
