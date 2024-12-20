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

package dev.benpetrillo.elixir.api;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import dev.benpetrillo.elixir.api.controllers.PlayerController;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public final class APIHandler {
    /**
     * Initializes the Javalin-based REST API.
     */
    public static void initialize() {
        var address = ElixirConstants.API_ADDRESS;
        var port = Integer.parseInt(ElixirConstants.API_PORT);

        EndpointGroup endpoints = () -> path("/api/v1", () -> {
            get("/{guild}/nowplaying", PlayerController::getNowPlaying);
            post("/{guild}/join", PlayerController::postJoinChannel);
            post("/{guild}/stop", PlayerController::postStopPlayer);
            post("/{guild}/volume", PlayerController::postVolume);
            post("/{guild}/play", PlayerController::postPlay);
        });

        Javalin.create(config -> config.router.apiBuilder(endpoints))
            .exception(NullPointerException.class, (e, ctx) ->
                ctx.status(400).json(APIError.from(e.getMessage())))
            .get("/", ctx -> ctx.result("Elixir Music API"))
            .start(address, port);

        ElixirClient.logger.info("API server started on {}:{}", address, port);
    }
}
