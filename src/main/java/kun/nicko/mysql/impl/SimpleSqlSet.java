package kun.nicko.mysql.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import kun.nicko.mysql.SqlAPI;
import kun.nicko.mysql.SqlAPISet;
import kun.nicko.mysql.Loader;

public class SimpleSqlSet<I, T extends SqlAPI<I>> implements SqlAPISet<I, T> {

    private Map<I, T> cache;
    private final Loader<I, T> loader;
    private final String table;

    public SimpleSqlSet(Loader<I, T> loader, String table) {
        this.loader = loader;
        this.cache = loader.fetch(table);
        this.table = table;
    }

    public Optional<T> get(I id) {
        return Optional.ofNullable(cache.get(id));
    }

    public void add(T t) {
        cache.put(t.getKey(), t);
    }

    public void delete(I i) {
        cache.remove(i);
    }

    @Override
    public void updateAsync() {
        CompletableFuture.runAsync(() -> loader.update(table, cache));
    }

    @Override
    public void fetchAsync() {
        CompletableFuture.runAsync(() -> this.cache = loader.fetch(table));
    }

    public Iterator<T> iterator() {
        return cache.values().iterator();
    }
}
