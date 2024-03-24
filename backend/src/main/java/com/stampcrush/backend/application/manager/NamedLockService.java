package com.stampcrush.backend.application.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NamedLockService {

    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";

    private final DataSource dataSource;

    public void executeWithLock(String userLockName,
                                int timeoutSeconds,
                                Runnable runnable) {

        try (Connection connection = dataSource.getConnection()) {
            try {
                getLock(connection, userLockName, timeoutSeconds);
                runnable.run();
            } finally {
                releaseLock(connection, userLockName);
            }
        } catch (SQLException | RuntimeException e) {
            System.out.println("fail: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void getLock(Connection connection,
                         String userLockName,
                         int timeoutseconds) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(GET_LOCK)) {
            preparedStatement.setString(1, userLockName);
            preparedStatement.setInt(2, timeoutseconds);
            preparedStatement.executeQuery();
        }
    }

    private void releaseLock(Connection connection,
                             String userLockName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(RELEASE_LOCK)) {
            preparedStatement.setString(1, userLockName);
            preparedStatement.executeQuery();
        }
    }
}
