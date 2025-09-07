package service;

import model.Person;

import java.sql.SQLException;
import java.util.Collection;

public interface StatisticsService {

    void updateRegionStatistics(Collection<Person> persons) throws SQLException;

    void updateTotalStatistics(Collection<Person> persons) throws SQLException;
}
