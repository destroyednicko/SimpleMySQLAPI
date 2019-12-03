package kun.nicko.mysql;

import java.util.Map;

public interface Loader<I, T extends SqlAPI<I>> {

    Map<I, T> fetch(String table);

    void update(String table, Map<I, T> cache);

}
