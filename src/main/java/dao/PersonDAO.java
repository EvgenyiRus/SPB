package dao;

import model.Person;

import java.sql.SQLException;
import java.util.Collection;

public interface PersonDAO {

    void save(Collection<Person> person) throws SQLException;
}
