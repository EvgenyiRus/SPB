package service.impl;

import dao.PersonDAO;
import dao.impl.IPersonDAO;
import model.Person;
import org.slf4j.Logger;
import service.PersonService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Объект для работы с населением
 */
public class IPersonService implements PersonService {
    private final Logger logger = getLogger(IPersonService.class);
    private final PersonDAO personDAO;
    private final IStatisticsService statisticsService;
    private final ExecutorService executorService;
    private final Connection connection;

    public IPersonService(Connection connection, int maxRegions, int threadPoolSize) {
        this.connection = connection;
        personDAO = new IPersonDAO(connection);
        statisticsService = new IStatisticsService(connection, maxRegions);
        if (threadPoolSize >= 0) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        } else {
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public void processPersons(Collection<Person> persons) {
        CompletableFuture.runAsync(() -> {
            try {
                processPersonInternal(persons);
            } catch (Exception exception) {
                logger.error("Error processing persons, {}", exception.getMessage(), exception);
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
            statisticsService.updateRegionStatistics(persons);

            // Обновление общей статистики
            statisticsService.updateTotalStatistics(persons);
            connection.commit();
            logger.info("Transfer completed successfully");
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        }
    }
}
