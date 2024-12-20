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

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.types.ElixirException;
import net.dv8tion.jda.api.JDAInfo;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

public final class Utilities {
    private static final Gson gson
        = new GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .create();

    /**
     * Throws an exception to a webhook.
     *
     * @param exception The throwable to throw.
     */

    public static void throwThrowable(ElixirException exception) {
        var webhook = Config.get("DEBUG-WEBHOOK");
        var description = new StringBuilder();
        if (exception.guild != null) description.append("Guild ID: ").append(exception.guild.getId()).append("\n");
        if (exception.member != null) description.append("Member: <@").append(exception.member.getId()).append(">\n");
        if (exception.stackTrace() != null) {
            var stackTrace = exception.stackTrace();
            assert stackTrace != null;
            description.append("File: ").append(stackTrace.getFileName()).append("\n");
            description.append("Line: ").append(stackTrace.getLineNumber()).append("\n");
            description.append("Method: ").append(stackTrace.getMethodName()).append("\n");
        }
        var client = WebhookClient.withUrl(webhook);
        var embed = new WebhookEmbedBuilder()
            .setTitle(new WebhookEmbed.EmbedTitle("Exception", ""))
            .setColor(ElixirConstants.ERROR_EMBED_COLOR.getRGB())
            .setTimestamp(OffsetDateTime.now())
            .addField(new WebhookEmbed.EmbedField(false, "Message", exception.getMessage()))
            .setDescription(String.valueOf(description));
        if (exception.additionalInformation != null) {
            embed.addField(new WebhookEmbed.EmbedField(false, "Additional Information", exception.additionalInformation));
        }
        client.send(embed.build());
        client.close();
    }

    /**
     * Send a message to a webhook.
     *
     * @param message The message to send.
     */

    public static void sendToWebhook(String message) {
        var webhook = Config.get("DEBUG-WEBHOOK");

        try (var client = WebhookClient.withUrl(webhook)) {
            client.send(message);
        }
    }

    /**
     * Determine if a URL is valid.
     *
     * @param input The URL to check.
     * @return boolean
     */

    @SuppressWarnings("unused")
    public static boolean isValidURL(String input) {
        try {
            new URI(input);
            return true;
        } catch (IllegalArgumentException | URISyntaxException e) {
            return false;
        }
    }

    /**
     * Encode a specific URI component.
     *
     * @param str The string to encode.
     * @return String
     */

    public static String encodeURIComponent(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * Convert ms to a formatted timestamp.
     *
     * @param ms The amount of milliseconds.
     * @return String
     */

    public static String formatDuration(long ms) {
        final Duration duration = Duration.ofMillis(ms);
        final int hours = duration.toHoursPart();
        final int minutes = duration.toMinutesPart();
        final int seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Formats the funky ISO 8601 timestamp into seconds.
     *
     * @param duration The ISO 8601 timestamp.
     * @return long
     */

    public static long cleanYouTubeFormat(String duration) {
        duration = duration.replace("PT", "").replace("H", ":")
            .replace("M", ":").replace("S", "");
        var split = duration.split(":");
        long length = 0;
        switch (split.length) {
            default -> {
                return Long.parseLong(duration) * 1000;
            }
            case 3 -> {
                length += Long.parseLong(split[0]) * 60 * 60 * 1000;
                length += Long.parseLong(split[1]) * 60 * 1000;
                return length + Long.parseLong(split[2]) * 1000;
            }
            case 2 -> {
                length += Long.parseLong(split[0]) * 60 * 1000;
                return length + Long.parseLong(split[1]) * 1000;
            }
            case 1 -> {
                return Long.parseLong(split[0]) * 1000;
            }
        }
    }

    /**
     * Extracts the video ID from a given URL.
     *
     * @param url The YouTube URL to extract the video ID from.
     * @return A video ID.
     */

    public static String extractVideoId(String url) {
        String[] segments = url.split("/");
        return url.contains("youtu.be") ? segments[3] : segments[3].split("v=")[1];
    }

    /**
     * Extracts the playlist ID from a given URL.
     *
     * @param url The YouTube URL to extract the playlist ID from.
     * @return A playlist ID.
     */

    public static String extractPlaylistId(String url) {
        String[] segments = url.split("/");
        return url.contains("youtu.be") ? segments[3] : segments[3].split("list=")[1];
    }

    /**
     * Extracts the song ID from a given URL.
     *
     * @param url The Spotify URL to extract the song ID from.
     * @return A song ID.
     */

    public static String extractSongId(String url) {
        String[] segments = url.split("/");
        return segments[4].split("\\?")[0];
    }

    /**
     * Pretty prints a given string.
     *
     * @param toPrint The string to format.
     * @return A pretty/formatted string.
     */

    public static String prettyPrint(String toPrint) {
        String pass1 = toPrint.toLowerCase();
        String[] lower = pass1.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String string : lower) {
            String pass2 = string.substring(0, 1).toUpperCase() + string.substring(1);
            builder.append(pass2).append(" ");
        }
        return builder.toString();
    }

    /**
     * Parses other strings into a boolean value.
     *
     * @param toParse The string to parse.
     * @return A boolean, or false if unable to parse.
     */

    public static boolean parseBoolean(String toParse) {
        return toParse.equalsIgnoreCase("true") || toParse.equalsIgnoreCase("yes") || toParse.equalsIgnoreCase("1");
    }

    /**
     * Convert a given object to a JSON string.
     *
     * @param json  The object to convert.
     * @param klass The class of the object.
     * @param <T>   The type of the object.
     * @return A de-serialized object.
     */

    public static <T> T deserialize(String json, Class<T> klass) {
        return gson.fromJson(json, klass);
    }

    /**
     * Convert a given object to a JSON string.
     *
     * @param json  The object to convert.
     * @param klass The class of the object.
     * @param <T>   The type of the object.
     * @return A de-serialized object.
     */

    public static <T> T deserialize(JsonElement json, Class<T> klass) {
        return gson.fromJson(json, klass);
    }

    /**
     * Convert a given object to a JSON string.
     *
     * @param object The object to convert.
     * @return A serialized object.
     */

    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    /**
     * Convert a given object to a JSON object.
     *
     * @param object The object to convert.
     * @return A serialized object.
     */
    public static JsonElement tree(Object object) {
        return gson.toJsonTree(object);
    }

    /**
     * Encode a string to Base64
     *
     * @param toEncode The string to encode.
     * @return A Base64 encoded string.
     */

    public static String base64Encode(String toEncode) {
        return Base64.getUrlEncoder().encodeToString(toEncode.getBytes());
    }

    /**
     * Decode a Base64 string.
     *
     * @param toDecode The string to decode.
     * @return A decoded string.
     */

    public static String base64Decode(String toDecode) {
        return new String(Base64.getUrlDecoder().decode(toDecode));
    }

    /**
     * Shorten a string to enable it to fit inside an embed.
     *
     * @param toShorten The string to shorten.
     * @return String
     */

    public static String shorten(String toShorten) {
        return StringUtils.abbreviate(toShorten, 3900);
    }

    /**
     * Get the JDA version.
     *
     * @return String
     */

    public static String getJDAVersion() {
        return JDAInfo.VERSION.split("_")[0];
    }

    /**
     * Clamps a value between a min and max.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
