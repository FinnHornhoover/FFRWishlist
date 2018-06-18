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
import finnhh.ffrwishlist.model.constants.database.tables.ItemSetSchemaColumn;
import finnhh.ffrwishlist.model.database.DatabaseManager;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public final class SetQueryContainer extends IncludeExcludeQueryContainer<String> {

    public SetQueryContainer() {
        super(QueryableColumn.SETNAME);
    }

    public Queue<String> getInputQueue() {
        final Queue<String> inputQueue = new ArrayDeque<>();

        includeEntryList.forEach(ie -> inputQueue.add(ie.getValueAsString()));
        excludeEntryList.forEach(ee -> inputQueue.add(ee.getValueAsString()));

        return inputQueue;
    }

    @Override
    public String getJoinedString() {
        String generalSubquery =
                "SELECT " +
                        ItemSetSchemaColumn.ITEMID + " " +
                "FROM " +
                        DatabaseManager.Table.ITEMS_SETS + " " +
                        "JOIN " +
                        DatabaseManager.Table.SETS + " " +
                        "USING (" + ItemSetSchemaColumn.SETID + ") " +
                "WHERE ";

        final StringJoiner setQueryJoiner = new StringJoiner(" AND ");

        if (!includeEntryList.isEmpty()) {
            setQueryJoiner.add(
                    QueryableColumn.ITEMID + " IN (" +
                    generalSubquery +
                    includeEntryList.stream()
                            .map(ie -> referencedColumn + " LIKE ?")
                            .collect(Collectors.joining(" OR ")) +
                    ")"
            );
        }

        if (!excludeEntryList.isEmpty()) {
            setQueryJoiner.add(
                    QueryableColumn.ITEMID + " NOT IN (" +
                    generalSubquery +
                    excludeEntryList.stream()
                            .map(ee -> referencedColumn + " LIKE ?")
                            .collect(Collectors.joining(" OR ")) +
                    ")"
            );
        }

        return setQueryJoiner.toString();
    }
}
