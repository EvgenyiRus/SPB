package service;

import dao.PersonDAO;
import dao.StatisticsDAO;
import dao.impl.IPersonDAO;
import dao.impl.IStatisticsDAO;
import model.Person;
import model.Statistics;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Объект для работы с населением
 */
public class PersonService {
    private final Logger logger = getLogger(PersonService.class);
    private final PersonDAO personDAO;
    private final StatisticsDAO statisticsDAO;
    private final ExecutorService executorService;
    private final Connection connection;

    public PersonService(Connection connection, int maxRegions, int threadPoolSize) {
        this.connection = connection;
        personDAO = new IPersonDAO(connection);
        statisticsDAO = new IStatisticsDAO(connection, maxRegions);
        if (threadPoolSize >= 0) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        } else {
            executorService = Executors.newCachedThreadPool();
        }
    }

    public void processPersons(Collection<Person> persons) {
        CompletableFuture.runAsync(() -> {
            try {
                processPersonInternal(persons);
            } catch (Exception exception) {
                logger.error("Error processing persons, {}", exception.getMessage(), exception);
                throw new CompletionException(exception);
            }
        }, executorService);
    }

    private void processPersonInternal(Collection<Person> persons) throws SQLException {
        if (persons == null || persons.isEmpty()) {
            return;
        }
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        try {
            personDAO.save(persons);
            logger.debug("Persons saved");

            // Обновление статистики по региону
            Map<Integer, Statistics> personsByRegions = getStatisticsByRegions(persons);
            for (Map.Entry<Integer, Statistics> personsByRegion : personsByRegions.entrySet()) {
                int region = personsByRegion.getKey();
                statisticsDAO.updateRegionStatistics(region, personsByRegion.getValue());
                logger.debug("Statistics updated for region: {}", region);
            }

            // Обновление общей статистики
            statisticsDAO.updateTotalStatistics(calculateStatistics(persons));
            logger.debug("Total statistics updated");
            connection.commit();
            logger.info("Transfer completed successfully");
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        }
    }

    //
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
