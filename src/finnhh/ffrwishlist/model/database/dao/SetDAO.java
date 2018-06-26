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

import finnhh.ffrwishlist.model.Item;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.database.schema.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.SetSchemaColumn;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.parser.QueryParser;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SetDAO extends DataAccessObject {

    public SetDAO() { }

    public List<Set> querySets(Map<Integer, Set> setMap, String name) {
        List<Set> matchedSets = new ArrayList<>();

        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            String query =
                    "SELECT " +
                            SetSchemaColumn.SETID + " " +
                    "FROM " +
                            DatabaseManager.Table.SETS + " " +
                    "WHERE " +
                            QueryableColumn.SETNAME + " LIKE ? " +
                    "ORDER BY " +
                            SetSchemaColumn.SETNAME + ";";

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, QueryParser.likeQueryArgument(name));

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next())
                    matchedSets.add(setMap.get(resultSet.getInt(SetSchemaColumn.SETID.name())));

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return matchedSets;
    }

    public List<Set> defaultQuerySets(Map<Integer, Set> setMap) {
        return querySets(setMap, "");
    }

    public Map<Integer, Set> getAllSetsMap() {
        Map<Integer, Set> allSets = new HashMap<>();

        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(
                        "SELECT " +
                                SetSchemaColumn.SETID    + ", " +
                                SetSchemaColumn.SETNAME  + " " +
                        "FROM " + DatabaseManager.Table.SETS + ";"
                );

                while (resultSet.next()) {
                    int setid = resultSet.getInt(SetSchemaColumn.SETID.name());

                    allSets.put(setid, new Set(setid, resultSet.getString(SetSchemaColumn.SETNAME.name())));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return allSets;
    }

    public void makeSetItemAssociations(Map<Integer, Set> setMap, final Map<Integer, Item> itemMap) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(
                        "SELECT " +
                                ItemSetSchemaColumn.SETID + ", " +
                                "GROUP_CONCAT(" + ItemSetSchemaColumn.ITEMID + ", \'" + GROUP_SEPARATOR + "\') " +
                        "FROM " + DatabaseManager.Table.ITEMS_SETS + " " +
                        "GROUP BY " + ItemSetSchemaColumn.SETID + ";"
                );

                while (resultSet.next()) {
                    Set curSet = setMap.get(resultSet.getInt(ItemSetSchemaColumn.SETID.name()));
                    String[] itemids = resultSet.getString(2).split(GROUP_SEPARATOR);

                    curSet.addAllToItemsAssociated(
                            Arrays.stream(itemids)
                                    .map(iid -> itemMap.get(Integer.parseInt(iid)))
                                    .collect(Collectors.toList())
                    );
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
