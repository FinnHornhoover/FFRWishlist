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

package finnhh.ffrwishlist.model.database.dao;

import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.constants.database.tables.ProfileSchemaColumn;
import finnhh.ffrwishlist.model.constants.profile.ProfileState;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO extends DataAccessObject {

    public ProfileDAO() { }

    public List<Profile> getAllProfiles() {
        List<Profile> listOfProfiles = new ArrayList<>();

        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(
                        "SELECT " +
                                ProfileSchemaColumn.PROFILEID    + ", " +
                                ProfileSchemaColumn.PROFILENAME  + ", " +
                                ProfileSchemaColumn.ACTIVE       + " " +
                        "FROM " + DatabaseManager.Table.PROFILES + ";"
                );

                while (resultSet.next()) {
                    listOfProfiles.add(new Profile(
                            resultSet.getInt(ProfileSchemaColumn.PROFILEID.name()),
                            resultSet.getString(ProfileSchemaColumn.PROFILENAME.name()),
                            resultSet.getInt(ProfileSchemaColumn.ACTIVE.name()) == ProfileState.ACTIVE.intValue()
                    ));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return listOfProfiles;
    }

    public void activateProfile(Profile profile) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "UPDATE " + DatabaseManager.Table.PROFILES + " " +
                        "SET " + ProfileSchemaColumn.ACTIVE + " = " + ProfileState.ACTIVE.intValue() + " " +
                        "WHERE " + ProfileSchemaColumn.PROFILEID + " = " + profile.getProfileID() + ";"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deactivateProfile(Profile profile) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "UPDATE " + DatabaseManager.Table.PROFILES + " " +
                        "SET " + ProfileSchemaColumn.ACTIVE + " = " + ProfileState.INACTIVE.intValue() + " " +
                        "WHERE " + ProfileSchemaColumn.PROFILEID + " = " + profile.getProfileID() + ";"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void clearAllActiveProfileStates() {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "UPDATE " + DatabaseManager.Table.PROFILES + " " +
                        "SET " + ProfileSchemaColumn.ACTIVE + " = " + ProfileState.INACTIVE.intValue() + ";"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
