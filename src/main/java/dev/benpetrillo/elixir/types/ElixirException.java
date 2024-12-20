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

package dev.benpetrillo.elixir.types;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class ElixirException extends Exception {

    public Guild guild;
    public Member member;
    public Exception exception;
    public String additionalInformation;

    public ElixirException() {
        this.guild = null;
        this.member = null;
    }

    public ElixirException(Guild guild, Member member) {
        this.guild = guild;
        this.member = member;
    }

    public ElixirException guild(Guild guild) {
        this.guild = guild;
        return this;
    }

    public ElixirException member(Member member) {
        this.member = member;
        return this;
    }

    public ElixirException exception(Exception exception) {
        this.exception = exception;
        return this;
    }

    public ElixirException additionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    public StackTraceElement stackTrace() {
        return this.exception == null ? null : this.exception.getStackTrace()[0];
    }

    public StackTraceElement[] fullStackTrace() {
        return this.exception.getStackTrace();
    }

    /*
     * Overwritten Methods
     */

    public String getMessage() {
        if (this.exception != null) {
            return this.exception.getMessage();
        } else {
            return "There was no message attached.";
        }
    }
}
