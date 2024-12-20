/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
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

package dev.benpetrillo.elixir.commands.music;

import dev.benpetrillo.elixir.commands.playlist.*;
import dev.benpetrillo.elixir.utils.Embed;
import tech.xigam.cch.command.Baseless;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

public final class PlaylistCommand extends Command implements Baseless {

    public PlaylistCommand() {
        super("playlist", "Manage your custom playlists.");
        registerSubCommand(new AddTrackSubCommand());
        registerSubCommand(new RemoveTrackSubCommand());
        registerSubCommand(new QueueSubCommand());
        registerSubCommand(new CreateSubCommand());
        registerSubCommand(new DeleteSubCommand());
        registerSubCommand(new FetchSubCommand());
        registerSubCommand(new SettingSubCommand());
        registerSubCommand(new ImportSubCommand());
        registerSubCommand(new ListSubCommand());
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.reply(Embed.error("Cannot execute this command."), false);
    }
}
