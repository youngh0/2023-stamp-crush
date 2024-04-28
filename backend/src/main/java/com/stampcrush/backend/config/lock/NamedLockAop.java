package com.stampcrush.backend.config.lock;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.stampcrush.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class NamedLockAop {

    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";

    private final DataSource dataSource;

    @Around("@annotation(com.stampcrush.backend.config.lock.NamedLock)")
    public void executeWithLock(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        NamedLock namedLock = method.getAnnotation(NamedLock.class);
        int timeout = namedLock.timeout();
        String lockKey = namedLock.lockKey();
        String lockType = namedLock.lockType();

        Long postFix = extractPostFixId(signature.getParameterNames(), joinPoint.getArgs(), lockType);
        String namedLockKey = lockKey + postFix;
        try (Connection connection = dataSource.getConnection()) {
            try {
                getLock(connection, namedLockKey, timeout);
                joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                releaseLock(connection, namedLockKey);
            }
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Long extractPostFixId(String[] parameterNames, Object[] args, String lockType) {
        for (int i = 0; i < parameterNames.length; i++) {
            System.out.println(parameterNames[i] + " and " + lockType + " qqwe ");
            System.out.println(parameterNames[i].equals(lockType));
            if (parameterNames[i].equals(lockType)) {
                return  (Long) args[i];
            }
        }
        throw new BadRequestException("lock 의 postfix 값이 없습니다.");
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
