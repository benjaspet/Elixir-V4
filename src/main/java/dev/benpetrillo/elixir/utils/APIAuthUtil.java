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

import com.mongodb.client.MongoCollection;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.DatabaseManager;
import org.bson.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class APIAuthUtil {

    public static String createAPIKey(String userId, String guildId) {

        String key = generateAPIKey();

        MongoCollection<Document> dbCollection = DatabaseManager.getAPIKeyCollection();
        dbCollection.insertOne(new Document("userId", userId)
            .append("guildId", guildId)
            .append("apiKey", key)
            .append("createdAt", System.currentTimeMillis() / 1000L)
        );
        return key;
    }

    public static boolean hasAPIKey(String userId, String guildId) {
        MongoCollection<Document> dbCollection = DatabaseManager.getAPIKeyCollection();
        return dbCollection.find(new Document("userId", userId)
            .append("guildId", guildId)).first() != null;
    }

    public static boolean isValidAPIKey(String userId, String guildId, String apiKey) {
        MongoCollection<Document> dbCollection = DatabaseManager.getAPIKeyCollection();
        return dbCollection.find(new Document("userId", userId)
            .append("guildId", guildId)
            .append("apiKey", apiKey)).first() != null;
    }

    private static String generateAPIKey() {

        try {
            String uuid = UUID.randomUUID().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(uuid.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            ElixirClient.logger.error("Failed to generate API key.");
            return null;
        }
    }
}
