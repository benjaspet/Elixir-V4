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


package dev.benpetrillo.elixir.managers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import lombok.Getter;
import org.bson.Document;

public final class DatabaseManager {

    private static MongoClient client;
    @Getter
    private static MongoCollection<Document> playlists;
    @Getter
    private static MongoCollection<Document> apiKeys;

    public static void create() {
        String uri = Config.get("MONGO-URI");
        try {
            DatabaseManager.client = MongoClients.create(uri);

            MongoDatabase db = client.getDatabase("Elixir");
            playlists = db.getCollection("playlists");
            apiKeys = db.getCollection("apiKeys");

            ElixirClient.logger.info("Database loaded successfully.");
        } catch (Exception e) {
            ElixirClient.logger.error("Failed to load database: {}", e.getMessage());
        }
    }

    public static MongoCollection<Document> getPlaylistCollection() {
        return playlists;
    }

    public static MongoCollection<Document> getAPIKeyCollection() {
        return apiKeys;
    }
}
