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
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.SetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.expression.ConditionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.OrderExpression;
import finnhh.ffrwishlist.model.database.sql.expression.column.FunctionExpression;
import finnhh.ffrwishlist.model.parser.QueryParser;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class SetDAO extends DataAccessObject {

    public SetDAO() { }

    public List<Set> querySets(final Map<Integer, Set> setMap, final String name) {
        final List<Set> matchedSets = new ArrayList<>();

        String query = SQLBuilders.selectBuilder()
                .select(SetSchemaColumn.SETID)
                .from(SchemaTable.SETS)
                .where(ConditionExpression
                        .forColumnExpression(QueryableColumn.SETNAME)
                        .like()
                        .placeholderValue()
                )
                .orderBy(OrderExpression.ascending(SetSchemaColumn.SETNAME))
                .toString();

        runOnPreparedStatementNoThrow(query, preparedStatement -> {
            preparedStatement.setString(1, QueryParser.likeQueryArgument(name));

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
                matchedSets.add(setMap.get(resultSet.getInt(SetSchemaColumn.SETID.name())));
        });

        return matchedSets;
    }

    public List<Set> defaultQuerySets(final Map<Integer, Set> setMap) {
        return querySets(setMap, "");
    }

    public Map<Integer, Set> getAllSetsMap() {
        final Map<Integer, Set> allSets = new HashMap<>();

        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    SetSchemaColumn.SETID,
                                    SetSchemaColumn.SETNAME
                            )
                            .from(SchemaTable.SETS)
                            .toString()
            );

            while (resultSet.next()) {
                int setid = resultSet.getInt(SetSchemaColumn.SETID.name());

                allSets.put(setid, new Set(setid, resultSet.getString(SetSchemaColumn.SETNAME.name())));
            }
        });

        return allSets;
    }

    public void makeSetItemAssociations(final Map<Integer, Set> setMap, final Map<Integer, Item> itemMap) {
        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    ItemSetSchemaColumn.SETID,
                                    FunctionExpression.groupConcat(ItemSetSchemaColumn.ITEMID, GROUP_SEPARATOR)
                            )
                            .from(SchemaTable.ITEMS_SETS)
                            .groupBy(ItemSetSchemaColumn.SETID)
                            .withoutHavingClause()
                            .toString()
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
        });
    }
}
