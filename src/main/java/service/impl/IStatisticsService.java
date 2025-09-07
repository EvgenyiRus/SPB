package service.impl;

import dao.StatisticsDAO;
import dao.impl.IStatisticsDAO;
import model.Person;
import model.Statistics;
import org.slf4j.Logger;
import service.StatisticsService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Объект для работы со статистикой показателей
 */
public class IStatisticsService implements StatisticsService {
    private final Logger logger = getLogger(IStatisticsService.class);
    private final StatisticsDAO statisticsDAO;

    public IStatisticsService(Connection connection, int maxRegions) {
        statisticsDAO = new IStatisticsDAO(connection, maxRegions);
    }

    // Обновление статистики по региону
    @Override
    public void updateRegionStatistics(Collection<Person> persons) throws SQLException {
        Map<Integer, Statistics> personsByRegions = getStatisticsByRegions(persons);
        for (Map.Entry<Integer, Statistics> personsByRegion : personsByRegions.entrySet()) {
            int region = personsByRegion.getKey();
            statisticsDAO.updateRegionStatistics(region, personsByRegion.getValue());
            logger.debug("Statistics updated for region: {}", region);
        }
    }

    // обновление общей статистики
    @Override
    public void updateTotalStatistics(Collection<Person> persons) throws SQLException {
        statisticsDAO.updateTotalStatistics(calculateStatistics(persons));
    }

    private Map<Integer, Statistics> getStatisticsByRegions(Collection<Person> persons) {
        Map<Integer, Statistics> result = new HashMap<>();
        Set<Integer> regions = persons.stream().map(Person::getRegion).collect(Collectors.toSet());
        for (Integer region : regions) {
            List<Person> regionPersons = persons.stream()
                    .filter(person -> person.getRegion() == region)
                    .collect(Collectors.toList());
            result.put(region, calculateStatistics(regionPersons));
        }
        return result;
    }

    private Statistics calculateStatistics(Collection<Person> persons) {
        return Statistics.builder()
                .maxIncome(persons.stream().max(Comparator.comparing(Person::getIncome)).get().getIncome())
                .totalPopulation((long) persons.size())
                .workingPopulation(persons.stream().filter(Person::isWorkingAge).count())
                .unemployedCount(persons.stream().filter(Person::isUnemployed).count())
                .averageIncome(getAverageIncome(persons))
                .build();
    }

    private BigDecimal getAverageIncome(Collection<Person> persons) {
        BigDecimal workIncome = new BigDecimal(0);
        long workCount = 0;
        for (Person person : persons) {
            if (!person.isUnemployed()) {
                workIncome.add(person.getIncome());
                workCount++;
            }
        }
        if (workCount == 0) {
            return workIncome;
        }
        return BigDecimal.valueOf(workIncome.longValue() / workCount);
    }
}
