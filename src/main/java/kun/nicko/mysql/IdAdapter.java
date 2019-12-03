package kun.nicko.mysql;

public interface IdAdapter<T> {

    T get(Object o);

    Object create(T t);

}
