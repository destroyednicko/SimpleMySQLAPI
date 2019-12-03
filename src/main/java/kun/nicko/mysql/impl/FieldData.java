package kun.nicko.mysql.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kun.nicko.mysql.annotation.SqlAPIConstructor;
import kun.nicko.mysql.annotation.Column;
import kun.nicko.mysql.annotation.ColumnKey;

@SuppressWarnings("unchecked")
public class FieldData<T> implements Iterable<Map.Entry<Field, Column>> {

    private final Class<T> aClass;
    private final Map<Field, Column> map;
    private final Constructor<T> constructor;
    private String columnKey;

    public FieldData(Class<T> aClass) {
        this.aClass = aClass;
        this.map = load();
        this.constructor = loadConstructor();
    }

    private Constructor<T> loadConstructor() {
        for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(SqlAPIConstructor.class))
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
        }

        return null;
    }

    private Map<Field, Column> load() {
        Map<Field, Column> map = new HashMap<>();

        for (Field field : aClass.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                map.put(field, column);

                if (field.isAnnotationPresent(ColumnKey.class)) {
                    columnKey = column.value();
                }
            }
        }

        return map;
    }

    @Override
    public Iterator<Map.Entry<Field, Column>> iterator() {
        return map.entrySet().iterator();
    }

    public Constructor<T> constructor() {
        return this.constructor;
    }

    public String getColumnKey() {
        return columnKey;
    }
}
