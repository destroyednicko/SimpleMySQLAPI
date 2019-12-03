package kun.nicko.mysql.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kun.nicko.mysql.SqlAPI;
import kun.nicko.mysql.IdAdapter;
import kun.nicko.mysql.Loader;
import kun.nicko.mysql.MySQL;
import kun.nicko.mysql.annotation.Column;
import kun.nicko.mysql.annotation.ColumnKey;

public class SimpleLoader<I, T extends SqlAPI<I>> implements Loader<I, T> {

    private final MySQL mySQL;
    private final FieldData<T> fieldData;
    private final IdAdapter<I> idAdapter;
    private Map<I, T> cache;

    public SimpleLoader(MySQL mySQL, Class<T> typeClass, IdAdapter<I> idAdapter) {
        this.mySQL = mySQL;
        this.fieldData = new FieldData<>(typeClass);
        this.idAdapter = idAdapter;
    }

    @Override
    public Map<I, T> fetch(String table) {
        Map<I, T> map = new HashMap<>();

        String query = "SELECT * FROM `" + table + "`";

        try (Connection connection = mySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                T t = construct(set);
                map.put(t.getKey(), t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.cache = new HashMap<>(map);
        return map;
    }

    @Override
    public void update(String table, Map<I, T> cache) {
        Set<T> iterated = new HashSet<>();

        try {
            for (Map.Entry<I, T> entry : cache.entrySet()) {
                if (!this.cache.containsKey(entry.getKey())) {
                    add(entry.getValue(), table);
                } else {
                    update(entry.getValue(), table);
                }

                iterated.add(entry.getValue());
            }

            for (T t : this.cache.values())
                if (!iterated.contains(t))
                    delete(t, table);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.cache = new HashMap<>(cache);
    }

    private void delete(T t, String table) {
        String query = "DELETE FROM `" + table + "` WHERE `" + fieldData.getColumnKey() + "` = '" + idAdapter.create(t.getKey()) + "'";
        System.out.println("deletando: " + query);

        try (Connection connection = mySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void add(T value, String table) throws IllegalAccessException {
        String query = "INSERT INTO `" + table + "` " + getColumns() + " VALUES " + getValues(value);
        System.out.println("inserindo: " + query);

        try (Connection connection = mySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void update(T value, String table) throws IllegalAccessException {
        String query = "UPDATE `" + table + "` SET " + getSetters(value) + " WHERE `" + fieldData.getColumnKey() + "` = '" + idAdapter.create(value.getKey()) + "'";
        System.out.println("atualizando: " + query);

        try (Connection connection = mySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getColumns() {
        StringBuilder columns = new StringBuilder("(");

        for (Map.Entry<Field, Column> entry : fieldData) {
            String column = entry.getValue().value();
            columns.append("`").append(column).append("`").append(", ");
        }

        columns.append(")");
        return columns.toString().replace(", )", ")");
    }

    private String getValues(T t) throws IllegalAccessException {
        StringBuilder values = new StringBuilder("(");

        for (Map.Entry<Field, Column> entry : fieldData) {
            values.append("'").append(entry.getKey().get(t)).append("'").append(", ");
        }

        values.append(")");
        return values.toString().replace(", )", ")");

    }

    private String getSetters(T t) throws IllegalAccessException {
        StringBuilder setters = new StringBuilder();

        for (Map.Entry<Field, Column> entry : fieldData) {
            setters.append("`").append(entry.getValue().value()).append("`").append(" = ");
            setters.append("'").append(entry.getKey().get(t)).append("'").append(", ");
        }

        setters.append(")");
        return setters.toString().replace(", )", "");
    }

    private T construct(ResultSet set) throws SQLException {
        I i = idAdapter.get(set.getObject(fieldData.getColumnKey()));
        T t = construct(i);

        for (Map.Entry<Field, Column> entry : fieldData) {
            String column = entry.getValue().value();

            try {
                if (entry.getKey().isAnnotationPresent(ColumnKey.class)) {
                    entry.getKey().set(t, idAdapter.get(set.getObject(column)));
                } else {
                    entry.getKey().set(t, set.getObject(column));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return t;
    }

    private T construct(I i) {
        try {
            return fieldData.constructor().newInstance(i);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
