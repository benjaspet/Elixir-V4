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

import com.sun.management.OperatingSystemMXBean;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.lang.management.ManagementFactory;
import java.time.OffsetDateTime;

public final class InfoCommand extends Command {
    private static final String INVITE = ElixirConstants.INVITE
        .replace("{}", ElixirClient.getId());

    public InfoCommand() {
        super("info", "Get information about Elixir.");
    }

    @Override
    public void execute(Interaction interaction) {
        var streams = 0;
        var users = 0;
        var servers = ElixirClient.getInstance().jda.getGuilds().size();
        for (var musicManager : ElixirMusicManager.getInstance().getMusicManagers()) {
            streams += musicManager.audioPlayer.getPlayingTrack() != null ? 1 : 0;
        }
        for (var server : ElixirClient.getInstance().jda.getGuilds()) {
            users += server.getMemberCount();
        }

        // Get Java process memory usage.
        var os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        var runtime = ManagementFactory.getRuntimeMXBean();
        var memory = os.getTotalMemorySize() / 1024 / 1024;

        // Get Java process CPU usage, and round it to 4 decimal places.
        var cpuUsage = Math.round(os.getProcessCpuLoad() * 100 * 10000.0) / 10000.0;
        // Get the total amount of available CPU cores.
        var cores = Runtime.getRuntime().availableProcessors();
        // Get the total amount of active threads.
        var threads = Thread.activeCount();

        // Get the total process uptime in milliseconds.
        var uptime = runtime.getUptime();
        // Get the uptime in days, hours, minutes, and seconds.
        var days = uptime / 86400000;
        var hours = (uptime % 86400000) / 3600000;
        var minutes = (uptime % 3600000) / 60000;
        var seconds = (uptime % 60000) / 1000;

        var uptimeString = "%dd %dh %dm %ds".formatted(days, hours, minutes, seconds);
        final EmbedBuilder embed = new EmbedBuilder()
            .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
            .setAuthor("Total Playing Streams: " + streams)
            .setDescription("[Invite Elixir to your server!](" + INVITE + ")")
            .addField("Bot Information", """
                • Powered by: %s
                • Developed by: Ponjo Studios
                • Server count: %s
                • User count: %s
                • Uptime: %s
                """.formatted(Utilities.getJDAVersion(), servers, users, uptimeString), false)
            .addField("Host Information", """
                • CPU Usage: %s%%
                • CPU Cores: %s
                • Threads: %s
                • Total Memory: %s MB
                """.formatted(cpuUsage, cores, threads, memory), false)
            .setFooter("Elixir Music", ElixirClient.getInstance().jda.getSelfUser().getEffectiveAvatarUrl())
            .setTimestamp(OffsetDateTime.now());
        interaction.reply(embed.build(), false);
    }
}
