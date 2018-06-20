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
import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.database.schema.ItemProfileSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.ItemSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.parser.ParsedQueryInformation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ItemPackDAO extends DataAccessObject {

    public ItemPackDAO() { }

    public List<ItemPack> queryItemPacks(Profile activeProfile, Map<Integer, Item> itemMap,
                                         ParsedQueryInformation parsedQueryInformation) {
        List<ItemPack> matchedItemPacks = new ArrayList<>();

        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            String query =
                    "SELECT " +
                            ItemSchemaColumn.ITEMID + ", " +
                            ItemProfileSchemaColumn.AMOUNT + " " +
                    "FROM " +
                            DatabaseManager.Table.ITEMS + " " +
                            "LEFT JOIN (" +
                                    "SELECT " +
                                            ItemProfileSchemaColumn.ITEMID + ", " +
                                            ItemProfileSchemaColumn.AMOUNT + " " +
                                    "FROM " +
                                            DatabaseManager.Table.ITEMS_PROFILES + " " +
                                    "WHERE " +
                                            ItemProfileSchemaColumn.PROFILEID + " = " + activeProfile.getProfileID() +
                            ") " +
                            "USING (" + ItemProfileSchemaColumn.ITEMID + ") " +
                    "WHERE " +
                            parsedQueryInformation.getWhereStatementChecks() + " " +
                    "ORDER BY " +
                            ItemSchemaColumn.RARITY + " DESC, " +
                            ItemSchemaColumn.LEVEL  + " DESC, " +
                            ItemSchemaColumn.TYPE   + " ASC, " +
                            ItemSchemaColumn.NAME   + " ASC;";

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                Queue<String> valuesToInsertToQuery = parsedQueryInformation.getValuesToInsertToQuery();

                int i = 1;
                while (!valuesToInsertToQuery.isEmpty())
                    preparedStatement.setString(i++, valuesToInsertToQuery.remove());

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    matchedItemPacks.add(new ItemPack(
                            itemMap.get(resultSet.getInt(ItemProfileSchemaColumn.ITEMID.name())),
                            resultSet.getInt(ItemProfileSchemaColumn.AMOUNT.name())
                    ));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return matchedItemPacks;
    }

    public List<ItemPack> queryItemPacksBySet(Profile activeProfile, Map<Integer, Item> itemMap, Set set) {
        List<ItemPack> matchedItemPacks = new ArrayList<>();

        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            String query =
                    "SELECT " +
                            ItemSchemaColumn.ITEMID + ", " +
                            ItemProfileSchemaColumn.AMOUNT + " " +
                    "FROM " +
                            DatabaseManager.Table.ITEMS + " " +
                            "LEFT JOIN (" +
                                    "SELECT " +
                                            ItemProfileSchemaColumn.ITEMID + ", " +
                                            ItemProfileSchemaColumn.AMOUNT + " " +
                                    "FROM " +
                                            DatabaseManager.Table.ITEMS_PROFILES + " " +
                                    "WHERE " +
                                            ItemProfileSchemaColumn.PROFILEID + " = " + activeProfile.getProfileID() +
                            ") " +
                            "USING (" + ItemProfileSchemaColumn.ITEMID + ") " +
                            "JOIN " +
                            DatabaseManager.Table.ITEMS_SETS + " " +
                            "USING (" + ItemSetSchemaColumn.ITEMID + ") " +
                    "WHERE " +
                            QueryableColumn.SETID + " = " + set.getSetID() + " " +
                    "ORDER BY " +
                            ItemSchemaColumn.TYPE   + " ASC, " +
                            ItemSchemaColumn.LEVEL  + " ASC, " +
                            ItemSchemaColumn.NAME   + " ASC;";

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    matchedItemPacks.add(new ItemPack(
                            itemMap.get(resultSet.getInt(ItemProfileSchemaColumn.ITEMID.name())),
                            resultSet.getInt(ItemProfileSchemaColumn.AMOUNT.name())
                    ));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return matchedItemPacks;
    }

    public void insertAmount(Profile activeProfile, ItemPack itemPack) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "INSERT INTO " + DatabaseManager.Table.ITEMS_PROFILES + " (" +
                                ItemProfileSchemaColumn.ITEMID + ", " +
                                ItemProfileSchemaColumn.PROFILEID + ", " +
                                ItemProfileSchemaColumn.AMOUNT + " " +
                        ") " +
                        "VALUES (" +
                                itemPack.getItem().getItemID() + ", " +
                                activeProfile.getProfileID() + ", " +
                                Amount.MINIMUM.intValue() +
                        ");"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void batchInsertAmount(Profile activeProfile, List<ItemPack> insertItemPackList) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                connection.setAutoCommit(false);

                for (ItemPack itemPack : insertItemPackList) {
                    statement.addBatch(
                            "INSERT INTO " + DatabaseManager.Table.ITEMS_PROFILES + " (" +
                                    ItemProfileSchemaColumn.ITEMID + ", " +
                                    ItemProfileSchemaColumn.PROFILEID + ", " +
                                    ItemProfileSchemaColumn.AMOUNT + " " +
                            ") " +
                            "VALUES (" +
                                    itemPack.getItem().getItemID() + ", " +
                                    activeProfile.getProfileID() + ", " +
                                    itemPack.getAmount() +
                            ");"
                    );
                }

                statement.executeBatch();

                connection.setAutoCommit(true);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void batchUpdateAmount(Profile activeProfile, List<ItemPack> updateItemPackList) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                connection.setAutoCommit(false);

                for (ItemPack itemPack : updateItemPackList) {
                    statement.addBatch(
                            "UPDATE " + DatabaseManager.Table.ITEMS_PROFILES + " " +
                            "SET " + ItemProfileSchemaColumn.AMOUNT + " = " + itemPack.getAmount() + " " +
                            "WHERE " +
                                    ItemProfileSchemaColumn.ITEMID + " = " + itemPack.getItem().getItemID() + " " +
                                    "AND " +
                                    ItemProfileSchemaColumn.PROFILEID + " = " + activeProfile.getProfileID() + ";"
                    );
                }

                statement.executeBatch();

                connection.setAutoCommit(true);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateAmount(Profile activeProfile, ItemPack itemPack, int newAmount) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "UPDATE " + DatabaseManager.Table.ITEMS_PROFILES + " " +
                        "SET " + ItemProfileSchemaColumn.AMOUNT + " = " + newAmount + " " +
                        "WHERE " +
                                ItemProfileSchemaColumn.ITEMID + " = " + itemPack.getItem().getItemID() + " " +
                                "AND " +
                                ItemProfileSchemaColumn.PROFILEID + " = " + activeProfile.getProfileID() + ";"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteAmount(Profile activeProfile, ItemPack itemPack) {
        try {
            Class.forName(DatabaseManager.DRIVER_NAME);

            try (Connection connection = DriverManager.getConnection(DatabaseManager.DATABASE_URL);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(
                        "DELETE FROM " + DatabaseManager.Table.ITEMS_PROFILES + " " +
                        "WHERE " +
                                ItemProfileSchemaColumn.ITEMID + " = " + itemPack.getItem().getItemID() + " " +
                                "AND " +
                                ItemProfileSchemaColumn.PROFILEID + " = " + activeProfile.getProfileID() + ";"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
