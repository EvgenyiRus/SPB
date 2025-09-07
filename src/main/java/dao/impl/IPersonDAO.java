package dao.impl;

import dao.PersonDAO;
import model.Gender;
import model.Person;
import org.slf4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

public class IPersonDAO implements PersonDAO {
    private final Connection connection;
    private static final int BATCH_SIZE = 1000;
    private final Logger logger = getLogger(IPersonDAO.class);

    public IPersonDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Collection<Person> persons) {
        if (persons == null || persons.isEmpty()) {
            return;
        }
        Collection<Person> mans = new ArrayList<>();
        Collection<Person> womens = new ArrayList<>();
        for (Person person : persons) {
            if (person.getGender() == Gender.MAN) {
                mans.add(person);
            } else {
                womens.add(person);
            }
        }
        String sqlMans = "INSERT INTO mans (id, birth_date, region, income) VALUES (?, ?, ?, ?)";
        String sqlWomens = "INSERT INTO womens (id, birth_date, region, income) VALUES (?, ?, ?, ?)";
        saveBatch(sqlMans, mans);
        saveBatch(sqlWomens, womens);
    }

    private void saveBatch(String sql, Collection<Person> persons) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int count = 0;
            for (Person person : persons) {
                preparedStatement.setLong(1, person.getId());
                preparedStatement.setDate(2, Date.valueOf(person.getBirthDate()));
                preparedStatement.setInt(3, person.getRegion());
                if (person.getIncome() != null) {
                    preparedStatement.setBigDecimal(4, person.getIncome());
                }
                preparedStatement.addBatch();
                count++;
                if (count % BATCH_SIZE == 0) {
                    preparedStatement.executeBatch();
                    count = 0;
                }
            }
            if (count % BATCH_SIZE != 0) {
                preparedStatement.executeBatch();
            }
            preparedStatement.clearBatch();
        } catch (SQLException e) {
            logger.error("Error saving persons: {}", e.getMessage(), e);
        }
    }
}
