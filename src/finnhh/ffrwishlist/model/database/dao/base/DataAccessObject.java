/*
 * MIT License
 *
 * Copyright (c) 2018 FinnHornhoover
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * Copies and derivative works of the Software which contain significant portions
 * of the data contained within the files that are licensed under the CC-BY-NC 4.0
 * license (specified in LICENSE-CC-BY-NC file) must also satisfy the conditions
 * of the CC-BY-NC 4.0 license, specifically non-commercial use and attribution.
 * Therefore, such copies or derivative works may not be used for commercial
 * purposes, and must include acceptable attribution.
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package finnhh.ffrwishlist.model.database.dao.base;

import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.resources.ResourceHolder;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public abstract class DataAccessObject {
    protected static final String GROUP_SEPARATOR = ";";

    protected DataAccessObject() { }

    protected static void runOnConnection(SQLConsumer<Connection> consumer) throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(consumer);

        Class.forName(DatabaseManager.DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL)) {

            consumer.accept(connection);
        }
    }

    protected static void runOnConnectionNoThrow(SQLConsumer<Connection> consumer) {
        try {
            runOnConnection(consumer);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void runOnStatement(SQLConsumer<Statement> consumer) throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(consumer);

        Class.forName(DatabaseManager.DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
             Statement statement = connection.createStatement()) {

            consumer.accept(statement);
        }
    }

    protected static void runOnStatementNoThrow(SQLConsumer<Statement> consumer) {
        try {
            runOnStatement(consumer);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void runOnPreparedStatement(String sql, SQLConsumer<PreparedStatement> consumer)
            throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(consumer);

        Class.forName(DatabaseManager.DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            consumer.accept(preparedStatement);
        }
    }

    protected static void runOnPreparedStatementNoThrow(String sql, SQLConsumer<PreparedStatement> consumer) {
        try {
            runOnPreparedStatement(sql, consumer);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void runOnConnectionAndStatement(SQLBiConsumer<Connection, Statement> consumer)
            throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(consumer);

        Class.forName(DatabaseManager.DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
             Statement statement = connection.createStatement()) {

            consumer.accept(connection, statement);
        }
    }

    protected static void runOnConnectionAndStatementNoThrow(SQLBiConsumer<Connection, Statement> consumer) {
        try {
            runOnConnectionAndStatement(consumer);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void runOnConnectionAndPreparedStatement(String sql, SQLBiConsumer<Connection, PreparedStatement> consumer)
            throws ClassNotFoundException, SQLException {
        Objects.requireNonNull(consumer);

        Class.forName(DatabaseManager.DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            consumer.accept(connection, preparedStatement);
        }
    }

    protected static void runOnConnectionAndPreparedStatementNoThrow(String sql, SQLBiConsumer<Connection, PreparedStatement> consumer) {
        try {
            runOnConnectionAndPreparedStatement(sql, consumer);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getExistingTableNames() {
        final Set<String> receivedNames = new HashSet<>();

        runOnConnectionNoThrow(connection -> {
            ResultSet mdRes = connection.getMetaData().getTables(null, null, "%", null);

            while (mdRes.next())
                receivedNames.add(mdRes.getString(3));
        });

        return receivedNames;
    }

    public static void initializeDataSource() throws ClassNotFoundException, SQLException {
        runOnStatement(statement -> {
            Scanner scanner = new Scanner(ResourceHolder.getSQLFileResourceAsStream("ffrw.sql"));
            scanner.useDelimiter(Pattern.compile(";"));

            while (scanner.hasNext())
                statement.executeUpdate(scanner.next() + ";");
        });
    }

    public static void rawUpdate(final Queue<String> updateQueue) {
        runOnStatementNoThrow(statement -> {
            while (!updateQueue.isEmpty())
                statement.executeUpdate(updateQueue.remove() + ";");
        });
    }
}
