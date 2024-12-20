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

package dev.benpetrillo.elixir.api.response;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.utils.TrackUtil;
import dev.benpetrillo.elixir.utils.Utilities;

public record TrackDataResponse(
    String title, String author, long duration, String formattedDuration, String uri,
    String thumbnail, long position, boolean isStream, String identifier, String requestedBy) {

    public static TrackDataResponse create(AudioTrack track) {

        final long position = track.getPosition();
        final AudioTrackInfo trackInfo = track.getInfo();

        return new TrackDataResponse(
            trackInfo.title, trackInfo.author, trackInfo.length / 1000,
            Utilities.formatDuration(position) + "/" + Utilities.formatDuration(trackInfo.length),
            trackInfo.uri, TrackUtil.getCoverArt(trackInfo), track.getPosition(),
            trackInfo.isStream, trackInfo.identifier, track.getUserData(String.class)
        );
    }
}
