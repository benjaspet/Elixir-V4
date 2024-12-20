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

package dev.benpetrillo.elixir.commands;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.time.OffsetDateTime;
import java.util.Collection;

public final class GuildsCommand extends Command {

    public GuildsCommand() {
        super("guilds", "View all guilds in which streams are playing.");
    }

    @Override
    public void execute(Interaction interaction) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(ElixirConstants.DEFAULT_EMBED_COLOR);

        Collection<GuildMusicManager> managers = ElixirMusicManager.getInstance().getMusicManagers();
        User self = ElixirClient.getInstance().jda.getSelfUser();

        if (managers.isEmpty()) {
            embed.setDescription("No guilds are currently streaming.");
            embed.setFooter("Elixir Music", self.getEffectiveAvatarUrl());

        } else {
            embed.setDescription("**Guilds Now Streaming**");
            for (GuildMusicManager manager : managers) {
                if (manager.audioPlayer.getPlayingTrack() != null) {
                    Guild guild = manager.getGuild();
                    embed.addField("Guild Name: " + guild.getName(), "Member Count: " + guild.getMemberCount(), false);
                }
            }
            embed.setFooter("Elixir Music", ElixirClient.getInstance().jda.getSelfUser().getEffectiveAvatarUrl());
        }
        embed.setTimestamp(OffsetDateTime.now());
        interaction.reply(embed.build(), false);
    }
}
