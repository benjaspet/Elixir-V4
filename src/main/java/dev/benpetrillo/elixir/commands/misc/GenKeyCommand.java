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

package dev.benpetrillo.elixir.commands.misc;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.utils.APIAuthUtil;
import dev.benpetrillo.elixir.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.time.OffsetDateTime;

public final class GenKeyCommand extends Command {

  public GenKeyCommand() {
    super("genkey", "Generate an API key.");
  }

  @Override
  public void execute(Interaction interaction) {

    interaction.setEphemeral(true);
    interaction.deferReply();

    Member member = interaction.getMember();
    if (member == null || interaction.getGuild() == null) {
      interaction.reply("You must be in a server to use this command.", false);
      return;
    }
    if (!member.getPermissions().contains(Permission.ADMINISTRATOR)) {
      interaction.reply("Only server administrators can generate API keys.", false);
      return;
    }


    String id = member.getId();
    String guildId = interaction.getGuild().getId();


    if (APIAuthUtil.hasAPIKey(id, guildId)) {
      interaction.reply("You already have an API key. Please contact a developer.", false);
      return;
    }

    try {
      String key = APIAuthUtil.createAPIKey(id, guildId);

      String avatar = ElixirClient.getInstance().jda.getSelfUser().getEffectiveAvatarUrl();

      MessageEmbed embed = new EmbedBuilder()
          .setDescription("API key for guild %s generated.".formatted(interaction.getGuild().getName()))
          .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
          .addField("API Key", String.format("`%s`", key), false)
          .setFooter("Elixir Music", avatar)
          .setTimestamp(OffsetDateTime.now())
          .build();

      interaction.reply(embed);

    } catch (Exception e) {
      ElixirClient.logger.error(e.getMessage());
      interaction.reply("An error occurred while generating an API key.", false);
    }
  }
}
