package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static revxrsal.commands.util.Collections.listOf;

/**
 * A provider for tab completions.
 * <p>
 * Register with {@link AutoCompleter#registerSuggestion(String, SuggestionProvider)}
 */
public interface SuggestionProvider {

    /**
     * A {@link SuggestionProvider} that always returns an empty list.
     */
    SuggestionProvider EMPTY = (args, sender, command) -> Collections.emptyList();

    /**
     * Returns the suggestions
     *
     * @param args    The command arguments
     * @param sender  The command sender
     * @param command The handled command
     * @return The command suggestions.
     */
    @NotNull
    Collection<String> getSuggestions(@NotNull List<String> args,
                                      @NotNull CommandActor sender,
                                      @NotNull ExecutableCommand command) throws Throwable;

    /**
     * Composes the two {@link SuggestionProvider}s into one provider that returns
     * the completions from both.
     *
     * @param other Other provider to merge with
     * @return The new provider
     */
    @Contract("null -> this; !null -> new")
    default SuggestionProvider compose(@Nullable SuggestionProvider other) {
        if (other == null) return this;
        if (this == EMPTY && other == EMPTY) return EMPTY;
        if (other == EMPTY) return this;
        if (this == EMPTY) return other;
        return (args, sender, command) -> {
            Set<String> completions = new HashSet<>(other.getSuggestions(args, sender, command));
            completions.addAll(getSuggestions(args, sender, command));
            return completions;
        };
    }

    /**
     * Returns a {@link SuggestionProvider} that always returns the given values
     *
     * @param suggestions Values to return.
     * @return The provider
     */
    static SuggestionProvider of(@Nullable Collection<String> suggestions) {
        if (suggestions == null) return EMPTY;
        return (args, sender, command) -> suggestions;
    }

    /**
     * Returns a {@link SuggestionProvider} that always returns the given values
     *
     * @param suggestions Values to return.
     * @return The provider
     */
    static SuggestionProvider of(@Nullable String... suggestions) {
        if (suggestions == null) return EMPTY;
        List<String> values = listOf(suggestions);
        return (args, sender, command) -> values;
    }

    /**
     * Returns a {@link SuggestionProvider} that computes the given supplier
     * every time suggestions are returned.
     *
     * @param supplier The collection supplier
     * @return The provider
     */
    static SuggestionProvider of(@NotNull Supplier<Collection<String>> supplier) {
        return (args, sender, command) -> supplier.get();
    }

    /**
     * Returns a {@link SuggestionProvider} that takes the given collection of
     * values and maps it to strings according to the given function.
     *
     * @param values   Values to map
     * @param function Function to remap values with
     * @param <T>      The values type
     * @return The provider
     */
    static <T> SuggestionProvider map(@NotNull Supplier<Collection<T>> values, Function<T, String> function) {
        return (args, sender, command) -> values.get().stream().map(function).collect(Collectors.toList());
    }
}
