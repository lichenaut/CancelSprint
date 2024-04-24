package com.lichenaut.cancelsprint.db;

import com.lichenaut.cancelsprint.Main;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.UUID;

@RequiredArgsConstructor
public class CSSQLiteManager {

    private HikariDataSource dataSource;

    public void initializeDataSource() {
        dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        String separator = Main.separator;
        dataSource.addDataSourceProperty("url", "jdbc:sqlite:plugins" + separator + "CancelSprint" + separator + "muters.db");
        dataSource.setMaximumPoolSize(3);
    }

    public void closeDataSource() {
        if (dataSource != null) dataSource.close();
    }

    public void createStructure() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS muters (uuid TEXT PRIMARY KEY NOT NULL)");
            }
        }
    }

    public void serializeMuters() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (UUID uuid : Main.muters) statement.addBatch(String.format("INSERT OR REPLACE INTO muters (uuid) VALUES ('%s')", uuid.toString()));
                statement.executeBatch();
            }
        }
    }

    public void deserializeMuters() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM muters")) {
                    while (resultSet.next()) {
                        Main.muters.add(UUID.fromString(resultSet.getString("uuid")));
                    }
                }
            }
        }
    }
}