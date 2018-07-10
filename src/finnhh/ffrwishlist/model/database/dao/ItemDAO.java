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
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.constants.item.Rarity;
import finnhh.ffrwishlist.model.constants.item.Type;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.statement.SelectSQLStatement;
import finnhh.ffrwishlist.model.database.sql.expression.column.FunctionExpression;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemDAO extends DataAccessObject {

    public ItemDAO() { }

    public Map<Integer, Item> getAllItemsMap() {
        final Map<Integer, Item> allItems = new HashMap<>();

        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    ItemSchemaColumn.ITEMID,
                                    ItemSchemaColumn.ICON,
                                    ItemSchemaColumn.NAME,
                                    ItemSchemaColumn.LEVEL,
                                    ItemSchemaColumn.TYPE,
                                    ItemSchemaColumn.RARITY
                            )
                            .from(SchemaTable.ITEMS)
                            .toString()
            );

            while (resultSet.next()) {
                int itemid = resultSet.getInt(ItemSchemaColumn.ITEMID.name());

                allItems.put(itemid, new Item(
                        itemid,
                        resultSet.getBytes(ItemSchemaColumn.ICON.name()),
                        resultSet.getString(ItemSchemaColumn.NAME.name()),
                        resultSet.getInt(ItemSchemaColumn.LEVEL.name()),
                        Type.correspondingTo(resultSet.getInt(ItemSchemaColumn.TYPE.name())),
                        Rarity.correspondingTo(resultSet.getInt(ItemSchemaColumn.RARITY.name()))
                ));
            }
        });

        return allItems;
    }

    public void makeItemSetAssociations(final Map<Integer, Item> itemMap, final Map<Integer, Set> setMap) {
        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    ItemSetSchemaColumn.ITEMID,
                                    FunctionExpression.groupConcat(ItemSetSchemaColumn.SETID, GROUP_SEPARATOR)
                            )
                            .from(SchemaTable.ITEMS_SETS)
                            .groupBy(ItemSetSchemaColumn.ITEMID)
                            .withoutHavingClause()
                            .toString()
            );

            while (resultSet.next()) {
                Item curItem = itemMap.get(resultSet.getInt(ItemSetSchemaColumn.ITEMID.name()));
                String[] setids = resultSet.getString(2).split(GROUP_SEPARATOR);

                curItem.addAllToSetsAssociated(
                        Arrays.stream(setids)
                                .map(sid -> setMap.get(Integer.parseInt(sid)))
                                .collect(Collectors.toList())
                );
            }
        });
    }
}
