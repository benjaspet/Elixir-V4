/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
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

package dev.benpetrillo.elixir.api;

import com.sun.net.httpserver.*;
import dev.benpetrillo.elixir.api.endpoints.*;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class WebAPI {

    public static void create() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(ElixirConstants.API_ADDRESS, Integer.parseInt(ElixirConstants.API_PORT)), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(10));
            httpServer.createContext("/", new Index());
            httpServer.createContext("/playlist", new PlaylistEndpoint());
            httpServer.createContext("/player", new PlayerEndpoint());
            httpServer.createContext("/queue", new QueueEndpoint());
            httpServer.start();
        } catch (IOException exception) {
            Utilities.throwThrowable(new ElixirException().exception(exception));
        }
    }
    
    public static class Index extends HttpEndpoint {

        @Override
        public void get() throws IOException {
            this.respond(new HttpResponse.Default());
        }
    }
}