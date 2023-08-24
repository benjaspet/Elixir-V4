package dev.benpetrillo.elixir.music.laudiolin;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler.LoopMode;
import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface LaudiolinMessages {
    Map<String, LaudiolinMessages> HANDLERS = new HashMap<>() {{
        this.put("initialize", LaudiolinMessages::initialize);
        this.put("playTrack", LaudiolinMessages::playTrack);
        this.put("resume", LaudiolinMessages::resume);
        this.put("pause", LaudiolinMessages::pause);
        this.put("volume", LaudiolinMessages::volume);
        this.put("shuffle", LaudiolinMessages::shuffle);
        this.put("skip", LaudiolinMessages::skip);
        this.put("seek", LaudiolinMessages::seek);
        this.put("queue", LaudiolinMessages::queue);
        this.put("loop", LaudiolinMessages::loop);
        this.put("synchronize", LaudiolinMessages::synchronize);
    }};

    /**
     * Event method.
     * Fires when the server sends a message.
     * This message is JSON-encoded, and should be decoded by the handler.
     *
     * @param handle The session that received the message.
     * @param message The message that was sent.
     */
    void handle(LaudiolinInterface handle, JsonObject message);

    /**
     * Handles the server's request to initialize the client.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void initialize(LaudiolinInterface handle, JsonObject content) {
        var guild = handle.getGuild();

        handle.send(new LaudiolinTypes.Initialize(
                ElixirConstants.LAUDIOLIN_TOKEN,
                ElixirClient.getId(), guild.getId()
        ));

        handle.getLogger().debug("Guild '{}' ({}) has finished initializing.",
                guild.getName(), guild.getId());
    }

    /**
     * Handles the server's request to play/queue a track.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void playTrack(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.PlayTrack.class);

        // Check if the data is a URL.
        var url = message.getData();
        if (!Utilities.isValidURL(url)) {
            url = HttpUtil.searchForVideo(url);
        }

        // Play the track in the guild.
        ElixirMusicManager.getInstance().loadAndPlay(
                handle.getGuild(), url, r -> {});
    }

    /**
     * Handles the server's request to resume the player.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void resume(LaudiolinInterface handle, JsonObject content) {
        handle.getManager().getAudioPlayer().setPaused(false);
    }

    /**
     * Handles the server's request to pause the player.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void pause(LaudiolinInterface handle, JsonObject content) {
        handle.getManager().getAudioPlayer().setPaused(true);
    }

    /**
     * Handles the server's request to set the player's volume.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void volume(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Volume.class);
        handle.getManager().getAudioPlayer().setVolume(message.getVolume());
    }

    /**
     * Handles the server's request to shuffle the player's queue.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void shuffle(LaudiolinInterface handle, JsonObject content) {
        handle.getManager().getScheduler().shuffle();
    }

    /**
     * Handles the server's request to skip the current track.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void skip(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Skip.class);

        var skipAmount = message.getTrack();
        var scheduler = handle.getManager().getScheduler();
        for (var i = 0; i < skipAmount; i++) {
            scheduler.nextTrack();
        }
    }

    /**
     * Handles the server's request to seek the current track.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void seek(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Seek.class);
        var player = handle.getManager().getAudioPlayer();

        var currentTrack = player.getPlayingTrack();
        if (currentTrack != null)
            currentTrack.setPosition(message.getPosition());
    }

    /**
     * Handles the server's request to fetch all tracks in the queue.
     * The server expects a 'queue' response.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void queue(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Queue.class);
        var scheduler = handle.getManager().getScheduler();

        // Serialize the queue.
        var tracks = scheduler.getQueue();
        var queue = new ArrayList<LaudiolinTrackInfo>();
        for (var track : tracks) {
            var trackInfo = track.getInfo();
            queue.add(LaudiolinTrackInfo.from(track));
        }

        // Send the queue to the server.
        handle.send(new LaudiolinTypes.Queue(queue));
    }

    /**
     * Handles the server's request to set the player's loop.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void loop(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Loop.class);
        var scheduler = handle.getManager().getScheduler();

        scheduler.repeating = LoopMode.fromNumber(message.getLoopMode());
    }

    /**
     * Handles the server's request to synchronize the player.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void synchronize(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.Synchronize.class);
        var player = handle.getManager().getAudioPlayer();
        var scheduler = handle.getManager().getScheduler();

        if (message.getDoAll() != null && message.getDoAll()) {
            handle.fullSync();
        }
        if (message.getPlayingTrack() != null) {
            if (player.getPlayingTrack() != null)
                player.stopTrack();

            player.playTrack(message.getPlayingTrack().toAudioItem());
        }
        if (message.getPaused() != null) {
            player.setPaused(message.getPaused());
        }
        if (message.getVolume() != null) {
            player.setVolume(Utilities.clamp(
                    message.getVolume(), 0, 150));
        }
        if (message.getQueue() != null) {
            scheduler.setQueue(message.getQueue().stream()
                    .map(LaudiolinTrackInfo::toAudioItem)
                    .map(AudioTrack.class::cast)
                    .toList());
        }
        if (message.getLoopMode() != null) {
            scheduler.repeating = LoopMode.fromNumber(message.getLoopMode());
        }
        if (message.getPosition() != null) {
            var playing = player.getPlayingTrack();
            if (playing != null && playing.isSeekable()) {
                playing.setPosition(Math.round(message.getPosition() * 1000f));
            }
        }
        if (message.getShuffle() != null && message.getShuffle()) {
            scheduler.shuffle();
        }
    }
}
