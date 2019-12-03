package kun.nicko.mysql;

import java.util.Optional;

public interface SqlAPISet<I, T extends SqlAPI<I>> extends Iterable<T> {

    Optional<T> get(I id);

    void add(T t);

    void delete(I i);

    void updateAsync();

    void fetchAsync();

}
