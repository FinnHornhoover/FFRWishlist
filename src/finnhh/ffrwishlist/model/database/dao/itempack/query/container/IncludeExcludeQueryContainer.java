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

package finnhh.ffrwishlist.model.database.dao.itempack.query.container;

import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.QueryComparison.NonNumericComparison;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.base.QueryContainer;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.base.QueryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class IncludeExcludeQueryContainer<T> extends QueryContainer<T> {
    protected List<QueryEntry<T>> includeEntryList;
    protected List<QueryEntry<T>> excludeEntryList;

    public IncludeExcludeQueryContainer(QueryableColumn referencedColumn) {
        super(referencedColumn);

        this.includeEntryList = new ArrayList<>();
        this.excludeEntryList = new ArrayList<>();
    }

    @Override
    public void addToEntries(@NonNumericComparison QueryComparison comparison, T value) {
        if (comparison == QueryComparison.EQUAL_TO)
            includeEntryList.add(new QueryEntry<>(comparison, value));
        else
            excludeEntryList.add(new QueryEntry<>(comparison, value));
    }

    @Override
    public String getJoinedString() {
        StringJoiner includeExcludeJoiner = new StringJoiner(" AND ");

        if (!includeEntryList.isEmpty()) {
            includeExcludeJoiner.add(
                    referencedColumn + " IN (" +
                    includeEntryList.stream()
                            .map(QueryEntry::getValueAsString)
                            .collect(Collectors.joining(", ")) +
                    ")"
            );
        }

        if (!excludeEntryList.isEmpty()) {
            includeExcludeJoiner.add(
                    referencedColumn + " NOT IN (" +
                    excludeEntryList.stream()
                            .map(QueryEntry::getValueAsString)
                            .collect(Collectors.joining(", ")) +
                    ")"
            );
        }

        return includeExcludeJoiner.toString();
    }

    @Override
    public void clear() {
        includeEntryList.clear();
        excludeEntryList.clear();
    }
}