package kun.nicko.mysql.exemplo;

import java.util.UUID;

import kun.nicko.mysql.SqlAPI;
import kun.nicko.mysql.annotation.Column;
import kun.nicko.mysql.annotation.ColumnKey;

public class User implements SqlAPI<UUID> {

    @ColumnKey
    @Column("uniqueId")
    private final UUID uniqueId;

    @Column("balance")
    private int balance;

    public User(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public UUID getKey() {
        return this.uniqueId;
    }
}
