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

package finnhh.ffrwishlist.model.parser.container;

import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.database.schema.column.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.expression.ConditionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.TableExpression;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class SetQueryContainer extends IncludeExcludeQueryContainer<String> {

    public SetQueryContainer() {
        super(QueryableColumn.SETNAME);
    }

    public Queue<String> getInputQueue() {
        Queue<String> inputQueue = new ArrayDeque<>();

        inputQueue.addAll(includeEntryList);
        inputQueue.addAll(excludeEntryList);

        return inputQueue;
    }

    @Override
    public String getJoinedString() {
        final StringJoiner setQueryJoiner = new StringJoiner(" AND ");

        if (!includeEntryList.isEmpty()) {
            setQueryJoiner.add(
                    ConditionExpression
                            .forColumnExpression(QueryableColumn.ITEMID)
                            .in()
                            .valuesFromSubQuery(SQLBuilders.selectBuilder()
                                    .select(ItemSetSchemaColumn.ITEMID)
                                    .from(TableExpression
                                            .fromTable(SchemaTable.ITEMS_SETS)
                                            .join(SchemaTable.SETS)
                                            .using(ItemSetSchemaColumn.SETID)
                                    )
                                    .where(
                                            includeEntryList.stream()
                                                    .map(ie ->
                                                            ConditionExpression
                                                                    .forColumnExpression(referencedColumn)
                                                                    .like()
                                                                    .placeholderValue()
                                                                    .toString()
                                                    )
                                                    .collect(Collectors.joining(" OR "))
                                    )

                            )
                            .toString()
            );
        }

        if (!excludeEntryList.isEmpty()) {
            setQueryJoiner.add(
                    ConditionExpression
                            .forColumnExpression(QueryableColumn.ITEMID)
                            .notIn()
                            .valuesFromSubQuery(SQLBuilders.selectBuilder()
                                    .select(ItemSetSchemaColumn.ITEMID)
                                    .from(TableExpression
                                            .fromTable(SchemaTable.ITEMS_SETS)
                                            .join(SchemaTable.SETS)
                                            .using(ItemSetSchemaColumn.SETID)
                                    )
                                    .where(
                                            excludeEntryList.stream()
                                                    .map(ee ->
                                                            ConditionExpression
                                                                    .forColumnExpression(referencedColumn)
                                                                    .like()
                                                                    .placeholderValue()
                                                                    .toString()
                                                    )
                                                    .collect(Collectors.joining(" OR "))
                                    )

                            )
                            .toString()
            );
        }

        return setQueryJoiner.toString();
    }
}
