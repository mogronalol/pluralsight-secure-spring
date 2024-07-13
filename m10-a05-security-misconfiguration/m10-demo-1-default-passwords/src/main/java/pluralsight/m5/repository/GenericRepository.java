package pluralsight.m5.repository;

import java.util.List;

public interface GenericRepository<T, ID> {
    void save(T entity);
    void deleteAll();
    List<T> findAll();
    T findById(ID id);
}