package kun.nicko.mysql.exemplo;

import java.util.UUID;

import kun.nicko.mysql.SqlAPISet;
import kun.nicko.mysql.Loader;
import kun.nicko.mysql.MySQL;
import kun.nicko.mysql.impl.SimpleSqlSet;
import kun.nicko.mysql.impl.SimpleLoader;

public class Exemplo {

    public static void main(String[] args) {
        MySQL mySQL = new MySQL("jdbc:mysql://localhost:3306/exemplo", "username", "password");
        Loader<UUID, User> loader = new SimpleLoader<>(mySQL, User.class, new UUIDAdapter());
        SqlAPISet<UUID, User> users = new SimpleSqlSet<>(loader, "users");

        users.fetchAsync(); // buscar usuários da tabela

        for (User user : users) {
            String msg = "Usuário encontrado com uuid %s e a balança %s.";
            System.out.println(String.format(msg, user.getKey().toString(), user.getBalance()));
        }

        UUID uuid = UUID.randomUUID();
        User user = users.get(uuid).orElse(new User(uuid));
        user.setBalance(1337);
        users.add(user);
        users.updateAsync(); // insere o usuário se não estiver presente na tabela

        user.setBalance(69);
        users.updateAsync(); // atualiza os usuários na tabela

        users.delete(user.getKey());
        users.updateAsync(); // deleta o usuário da tabela
    }
}
