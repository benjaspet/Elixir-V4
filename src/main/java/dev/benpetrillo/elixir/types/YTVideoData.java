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

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public final class YTVideoData {

    public String kind, etag;
    public List<Item> items;

    public static class Item {
        public String kind, etag, id;
        public Snippet snippet;
        public ContentDetails contentDetails;

        public static class Snippet {
            public String publishedAt, channelId, title, description;
            public Map<String, Map<String, String>> thumbnails;
            public List<String> tags;
            public String channelTitle, liveBroadcastContent;
            public Map<String, String> localized;
        }

        public static class ContentDetails {
            public String duration, dimension, definition, caption, licensedContent;
            public JsonObject contentRating;
            public String projection;
        }
    }
}
