package dao.impl;

import dao.StatisticsDAO;
import model.Statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Объект для работы со статистикой показателей
 */
public class IStatisticsDAO implements StatisticsDAO {
    private final Connection connection;
    private final ReentrantLock totalLock = new ReentrantLock();
    private final ReentrantLock[] regionLocks;

    public IStatisticsDAO(Connection connection, int maxRegions) {
        this.regionLocks = new ReentrantLock[maxRegions + 1];
        for (int i = 0; i <= maxRegions; i++) {
            regionLocks[i] = new ReentrantLock();
        }
        this.connection = connection;
    }

    @Override
    public void updateRegionStatistics(int region, Statistics statistics) throws SQLException {
        regionLocks[region].lock();
        String insertSql = "INSERT INTO region_statistics (region) VALUES (?)";
        String updateSql = """
                    UPDATE region_statistics SET 
                        total_population = total_population + ?,
                        working_population = working_population + ?,
                        average_income = ?,
                        max_income = GREATEST(max_income, COALESCE(?, 0)),
                        unemployed_count = unemployed_count + ?,
                        working_percentage = working_population * 100.0 / total_population,
                        unemployed_percentage = CASE 
                            WHEN working_population > 0 THEN unemployed_count * 100.0 / working_population 
                            ELSE 0 
                        END,
                        last_updated = CURRENT_TIMESTAMP
                    WHERE region = ?
                """;
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, region);
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(String.format("Failed to insert new %s region statistic", region));
        } finally {
            regionLocks[region].unlock();
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            preparedStatement.setLong(1, statistics.getTotalPopulation());
            preparedStatement.setLong(2, statistics.getWorkingPopulation());
            preparedStatement.setBigDecimal(3, statistics.getAverageIncome());
            preparedStatement.setBigDecimal(4, statistics.getMaxIncome());
            preparedStatement.setLong(5, statistics.getUnemployedPopulation());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(String.format("Failed to update %s region statistic", region));
        } finally {
            regionLocks[region].unlock();
        }
    }

    @Override
    public void updateTotalStatistics(Statistics statistics) throws SQLException {
        totalLock.lock();
        try {
            String updateSql = """
                        UPDATE total_statistics SET 
                            total_population = total_population + ?,
                            working_population = working_population + ?,
                            average_income = ?,
                            max_income = GREATEST(max_income, COALESCE(?, 0)),
                            unemployed_count = unemployed_count + ?,
                            working_percentage = working_population * 100.0 / total_population,
                            unemployed_percentage = CASE 
                                WHEN working_population > 0 THEN unemployed_count * 100.0 / working_population 
                                ELSE 0 
                            END,
                            last_updated = CURRENT_TIMESTAMP
                        WHERE id = 1
                    """;

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
                preparedStatement.setLong(1, statistics.getTotalPopulation());
                preparedStatement.setLong(2, statistics.getWorkingPopulation());
                preparedStatement.setBigDecimal(3, statistics.getAverageIncome());
                preparedStatement.setBigDecimal(4, statistics.getMaxIncome());
                preparedStatement.setLong(5, statistics.getUnemployedPopulation());

                // Создание новой записи при отсутствии записей
                if (preparedStatement.executeUpdate() == 0) {
                    String initSql = """
                                INSERT INTO total_statistics (id, total_population, working_population, 
                                average_income, max_income, unemployed_count) 
                                VALUES (1, 1, ?, ?, ?, ?)
                            """;
                    try (PreparedStatement initStmt = connection.prepareStatement(initSql)) {
                        initStmt.setLong(1, statistics.getWorkingPopulation());
                        initStmt.setBigDecimal(2, statistics.getAverageIncome());
                        initStmt.setBigDecimal(3, statistics.getMaxIncome());
                        initStmt.setLong(4, statistics.getUnemployedPopulation());
                        initStmt.executeUpdate();
                    }
                }
            }
        } finally {
            totalLock.unlock();
        }
    }

    @Override
    public Statistics getRegionStatistics(int region) throws SQLException {
        String sql = "SELECT * FROM region_statistics WHERE region = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, region);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStatistics(rs);
            }
            return null;
        }
    }

    @Override
    public Statistics getTotalStatistics() throws SQLException {
        String sql = "SELECT * FROM total_statistics WHERE id = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStatistics(rs);
            }
            return null;
        }
    }

    private Statistics mapResultSetToStatistics(ResultSet rs) throws SQLException {
        return Statistics.builder()
                .region(rs.getInt("region"))
                .totalPopulation(rs.getLong("total_population"))
                .workingPopulation(rs.getLong("working_population"))
                .workingPercentage(rs.getDouble("working_percentage"))
                .averageIncome(rs.getBigDecimal("average_income"))
                .maxIncome(rs.getBigDecimal("max_income"))
                .unemployedCount(rs.getLong("unemployed_count"))
                .unemployedPercentage(rs.getDouble("unemployed_percentage"))
                .build();
    }
}
