package kun.nicko.mysql.exemplo;

import java.util.UUID;

import kun.nicko.mysql.IdAdapter;

public class UUIDAdapter implements IdAdapter<UUID> {

    @Override
    public UUID get(Object o) {
        return UUID.fromString((String) o);
    }

    @Override
    public Object create(UUID uuid) {
        return uuid.toString();
    }
}
