package dev.benpetrillo.elixir.objects;

public record Pair<A, B>(A a, B b) {
    /**
     * Create a new pair.
     */
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }
}
