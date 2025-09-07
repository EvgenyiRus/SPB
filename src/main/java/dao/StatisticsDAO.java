package dao;

import model.Statistics;

import java.sql.SQLException;

public interface StatisticsDAO {
    void updateRegionStatistics(int region, Statistics statistics) throws SQLException;

    void updateTotalStatistics(Statistics statistics) throws SQLException;

    Statistics getRegionStatistics(int region) throws SQLException;

    Statistics getTotalStatistics() throws SQLException;

}
