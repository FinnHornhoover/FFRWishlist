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

package finnhh.ffrwishlist.model.database;

import finnhh.ffrwishlist.model.database.dao.ItemDAO;
import finnhh.ffrwishlist.model.database.dao.ProfileDAO;
import finnhh.ffrwishlist.model.database.dao.SetDAO;
import finnhh.ffrwishlist.model.database.dao.VersionDAO;
import finnhh.ffrwishlist.model.database.dao.ItemPackDAO;
import finnhh.ffrwishlist.resources.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatabaseManager {
    public static final String DRIVER_NAME          = "org.sqlite.JDBC";
    public static final String DATABASE_FILE_DIR    = System.getProperty("user.home") + "/FFRWishlistData/db";
    public static final String DATABASE_URL         = "jdbc:sqlite:" + DATABASE_FILE_DIR + "/ffrw.db";

    private static final Set<String> EXPECTED_TABLE_NAMES = Arrays.stream(Table.values())
                                                                    .map(Table::name)
                                                                    .collect(Collectors.toSet());

    private final VersionDAO    versionDAO;

    private final ProfileDAO    profileDAO;
    private final ItemDAO       itemDAO;
    private final SetDAO        setDAO;
    private final ItemPackDAO   itemPackDAO;

    private int databaseVersion;

    public DatabaseManager() throws IOException, SQLException, ClassNotFoundException {
        initializeDataSource();

        this.versionDAO = new VersionDAO();
        databaseVersion = versionDAO.getVersion();

        this.profileDAO  = new ProfileDAO();

        this.itemDAO     = new ItemDAO();

        this.setDAO      = new SetDAO();

        this.itemPackDAO = new ItemPackDAO();
    }

    private Set<String> getExistingTableNames() {
        Set<String> receivedNames = new HashSet<>();

        try {
            Class.forName(DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
                ResultSet mdRes = connection.getMetaData().getTables(null, null, "%", null);

                while (mdRes.next())
                    receivedNames.add(mdRes.getString(3));

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return receivedNames;
    }

    private void initializeDataSource() throws IOException, SQLException, ClassNotFoundException {
        File dbDir = new File(DATABASE_FILE_DIR);
        if (!dbDir.exists())
            Files.createDirectories(dbDir.toPath());

        Class.forName(DRIVER_NAME);

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {

            if (Collections.disjoint(getExistingTableNames(), EXPECTED_TABLE_NAMES)) {
                Scanner scanner = new Scanner(ResourceLoader.getSQLFileResourceAsStream("ffrw.sql"));
                scanner.useDelimiter(Pattern.compile(";"));

                while (scanner.hasNext())
                    statement.executeUpdate(scanner.next() + ";");
            }

        }
    }

    public void rawUpdate(Queue<String> updateQueue) {
        try {
            Class.forName(DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                while (!updateQueue.isEmpty())
                    statement.executeUpdate(updateQueue.remove() + ";");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setDatabaseVersion(int version) {
        this.databaseVersion = version;

        versionDAO.updateVersion(version);
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public boolean allTablesExist() {
        return getExistingTableNames().containsAll(EXPECTED_TABLE_NAMES);

    }

    public ProfileDAO getProfileDAO() {
        return profileDAO;
    }

    public ItemDAO getItemDAO() {
        return itemDAO;
    }

    public SetDAO getSetDAO() {
        return setDAO;
    }

    public ItemPackDAO getItemPackDAO() {
        return itemPackDAO;
    }

    public enum Table {
        VERSIONS,
        ITEMS,
        PROFILES,
        SETS,
        ITEMS_SETS,
        ITEMS_PROFILES
    }
}
