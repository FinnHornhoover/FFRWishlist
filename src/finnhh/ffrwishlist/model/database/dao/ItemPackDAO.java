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
import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemProfileSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.expression.ConditionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.OrderExpression;
import finnhh.ffrwishlist.model.database.sql.expression.InstructionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.TableExpression;
import finnhh.ffrwishlist.model.parser.ParsedQueryInformation;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ItemPackDAO extends DataAccessObject {

    public ItemPackDAO() { }

    public List<ItemPack> queryItemPacks(final Profile activeProfile, final Map<Integer, Item> itemMap,
                                         final ParsedQueryInformation parsedQueryInformation) {
        final List<ItemPack> matchedItemPacks = new ArrayList<>();

        String query = SQLBuilders.selectBuilder()
                .select(
                        ItemSchemaColumn.ITEMID,
                        ItemProfileSchemaColumn.AMOUNT
                )
                .from(TableExpression
                        .fromTable(SchemaTable.ITEMS)
                        .leftJoin(TableExpression
                                .fromSubQuery(SQLBuilders.selectBuilder()
                                        .select(
                                                ItemProfileSchemaColumn.ITEMID,
                                                ItemProfileSchemaColumn.AMOUNT
                                        )
                                        .from(SchemaTable.ITEMS_PROFILES)
                                        .where(ConditionExpression
                                                .forColumnExpression(ItemProfileSchemaColumn.PROFILEID)
                                                .check(QueryComparison.EQUAL_TO)
                                                .value(activeProfile.getProfileID())
                                        )
                                )
                        )
                        .using(ItemProfileSchemaColumn.ITEMID)
                )
                .where(parsedQueryInformation.getWhereStatementChecks())
                .orderBy(
                        OrderExpression.descending(ItemSchemaColumn.RARITY),
                        OrderExpression.descending(ItemSchemaColumn.LEVEL),
                        OrderExpression.ascending(ItemSchemaColumn.TYPE),
                        OrderExpression.ascending(ItemSchemaColumn.NAME)
                )
                .toString();

        runOnPreparedStatementNoThrow(query, preparedStatement -> {
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
        });

        return matchedItemPacks;
    }

    public List<ItemPack> queryItemPacksBySet(final Profile activeProfile, final Map<Integer, Item> itemMap, final Set set) {
        final List<ItemPack> matchedItemPacks = new ArrayList<>();

        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    ItemSchemaColumn.ITEMID,
                                    ItemProfileSchemaColumn.AMOUNT
                            )
                            .from(TableExpression
                                    .fromTable(SchemaTable.ITEMS)
                                    .leftJoin(TableExpression
                                            .fromSubQuery(SQLBuilders.selectBuilder()
                                                    .select(
                                                            ItemProfileSchemaColumn.ITEMID,
                                                            ItemProfileSchemaColumn.AMOUNT
                                                    )
                                                    .from(SchemaTable.ITEMS_PROFILES)
                                                    .where(ConditionExpression
                                                            .forColumnExpression(ItemProfileSchemaColumn.PROFILEID)
                                                            .check(QueryComparison.EQUAL_TO)
                                                            .value(activeProfile.getProfileID())
                                                    )
                                            )
                                    )
                                    .using(ItemProfileSchemaColumn.ITEMID)
                                    .join(SchemaTable.ITEMS_SETS)
                                    .using(ItemSetSchemaColumn.ITEMID)
                            )
                            .where(ConditionExpression
                                    .forColumnExpression(QueryableColumn.SETID)
                                    .check(QueryComparison.EQUAL_TO)
                                    .value(set.getSetID())
                            )
                            .orderBy(
                                    OrderExpression.ascending(ItemSchemaColumn.TYPE),
                                    OrderExpression.ascending(ItemSchemaColumn.LEVEL),
                                    OrderExpression.ascending(ItemSchemaColumn.NAME)
                            )
                            .toString()
            );

            while (resultSet.next()) {
                matchedItemPacks.add(new ItemPack(
                        itemMap.get(resultSet.getInt(ItemProfileSchemaColumn.ITEMID.name())),
                        resultSet.getInt(ItemProfileSchemaColumn.AMOUNT.name())
                ));
            }
        });

        return matchedItemPacks;
    }

    public void insertAmount(final Profile activeProfile, final ItemPack itemPack) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.insertBuilder()
                        .insertInto(SchemaTable.ITEMS_PROFILES)
                        .withColumnsSpecified(
                                ItemProfileSchemaColumn.ITEMID,
                                ItemProfileSchemaColumn.PROFILEID,
                                ItemProfileSchemaColumn.AMOUNT
                        )
                        .values(
                                itemPack.getItem().getItemID(),
                                activeProfile.getProfileID(),
                                Amount.MINIMUM
                        )
                        .toString()
        ));
    }

    public void batchInsertAmount(final Profile activeProfile, final List<ItemPack> insertItemPackList) {
        runOnConnectionAndStatementNoThrow((connection, statement) -> {
            connection.setAutoCommit(false);

            for (ItemPack itemPack : insertItemPackList) {
                statement.addBatch(
                        SQLBuilders.insertBuilder()
                                .insertInto(SchemaTable.ITEMS_PROFILES)
                                .withColumnsSpecified(
                                        ItemProfileSchemaColumn.ITEMID,
                                        ItemProfileSchemaColumn.PROFILEID,
                                        ItemProfileSchemaColumn.AMOUNT
                                )
                                .values(
                                        itemPack.getItem().getItemID(),
                                        activeProfile.getProfileID(),
                                        Amount.MINIMUM
                                )
                                .toString()
                );
            }

            statement.executeBatch();

            connection.setAutoCommit(true);
        });
    }

    public void updateAmount(final Profile activeProfile, final ItemPack itemPack, int newAmount) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.updateBuilder()
                        .update(SchemaTable.ITEMS_PROFILES)
                        .set(InstructionExpression
                                .forColumn(ItemProfileSchemaColumn.AMOUNT)
                                .setValue(newAmount)
                        )
                        .where(ConditionExpression
                                .forColumnExpression(ItemProfileSchemaColumn.ITEMID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(itemPack.getItem().getItemID())
                                .and()
                                .columnExpression(ItemProfileSchemaColumn.PROFILEID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(activeProfile.getProfileID())
                        )
                        .toString()
        ));
    }

    public void batchUpdateAmount(final Profile activeProfile, final List<ItemPack> updateItemPackList) {
        runOnConnectionAndStatementNoThrow((connection, statement) -> {
            connection.setAutoCommit(false);

            for (ItemPack itemPack : updateItemPackList) {
                statement.addBatch(
                        SQLBuilders.updateBuilder()
                                .update(SchemaTable.ITEMS_PROFILES)
                                .set(InstructionExpression
                                        .forColumn(ItemProfileSchemaColumn.AMOUNT)
                                        .setValue(itemPack.getAmount())
                                )
                                .where(ConditionExpression
                                        .forColumnExpression(ItemProfileSchemaColumn.ITEMID)
                                        .check(QueryComparison.EQUAL_TO)
                                        .value(itemPack.getItem().getItemID())
                                        .and()
                                        .columnExpression(ItemProfileSchemaColumn.PROFILEID)
                                        .check(QueryComparison.EQUAL_TO)
                                        .value(activeProfile.getProfileID())
                                )
                                .toString()
                );
            }

            statement.executeBatch();

            connection.setAutoCommit(true);
        });
    }

    public void deleteAmount(final Profile activeProfile, final ItemPack itemPack) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.deleteBuilder()
                        .deleteFrom(SchemaTable.ITEMS_PROFILES)
                        .where(ConditionExpression
                                .forColumnExpression(ItemProfileSchemaColumn.ITEMID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(itemPack.getItem().getItemID())
                                .and()
                                .columnExpression(ItemProfileSchemaColumn.PROFILEID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(activeProfile.getProfileID())
                        )
                        .toString()
        ));
    }
}
