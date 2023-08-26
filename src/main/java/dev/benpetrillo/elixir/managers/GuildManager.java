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

package dev.benpetrillo.elixir.managers;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinTypes;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.LaudiolinUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GuildManager extends ListenerAdapter {
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final List<String> IN_GUILDS
            = new CopyOnWriteArrayList<>();

    /**
     * Load the guilds from the database.
     */
    public static void loadGuilds(JDA jda) {
        jda.getGuilds().forEach(guild ->
                IN_GUILDS.add(guild.getId()));
        GuildManager.updateGuilds();
    }

    /**
     * Adds a guild to the database.
     *
     * @param guild The guild to add.
     */
    public static void addNewGuild(Guild guild) {
        IN_GUILDS.add(guild.getId());
        GuildManager.updateGuilds();
    }

    /**
     * Removes a guild from the database.
     *
     * @param guild The guild to remove.
     */
    public static void removeGuild(Guild guild) {
        IN_GUILDS.remove(guild.getId());
        GuildManager.updateGuilds();
    }

    /**
     * Checks if a guild is in the database.
     *
     * @param id The guild ID.
     * @return Whether the guild is in the database.
     */
    public static boolean inCache(String id) {
        return IN_GUILDS.contains(id);
    }

    /**
     * Updates the guilds on the backend.
     */
    public static void updateGuilds() {
        var botId = ElixirClient.getId();
        var message = new LaudiolinTypes.Guilds(botId,
                GuildManager.IN_GUILDS,
                ElixirMusicManager.getInstance().getMusicManagers().stream()
                        .map(manager -> manager.getGuild().getId())
                        .toList());

        // Prepare the request.
        var guildList = RequestBody.create(
                Utilities.serialize(message), JSON);
        var request = new Request.Builder()
                .url(LaudiolinUtil.ENDPOINT + "/elixir/guilds")
                .header("authorization", ElixirConstants.LAUDIOLIN_TOKEN)
                .method("POST", guildList)
                .build();

        // Execute the request.
        try (var response = HttpUtil.getClient()
                .newCall(request).execute()) {
            ElixirClient.getLogger().debug("Updated guilds for {}.", botId);
        } catch (IOException exception) {
            ElixirClient.getLogger().error("Unable to update guilds.", exception);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        GuildManager.addNewGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        GuildManager.removeGuild(event.getGuild());
    }
}
