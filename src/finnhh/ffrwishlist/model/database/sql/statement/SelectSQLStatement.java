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

package finnhh.ffrwishlist.model.database.sql.statement;

import finnhh.ffrwishlist.model.database.sql.statement.base.SQLStatement;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SelectSQLStatement extends SQLStatement {

    private SelectSQLStatement() { }

    public FromSelectPart select(Object... objects) {
        return new SelectPart().select(false, objects);
    }

    public FromSelectPart selectAll() {
        return new SelectPart().select(false);
    }

    public FromSelectPart selectDistinct(Object... objects) {
        return new SelectPart().select(true, objects);
    }

    public FromSelectPart selectDistinctAll() {
        return new SelectPart().select(true);
    }

    public static SelectSQLStatement builder() {
        return new SelectSQLStatement();
    }

    public static class CompleteSelectStatement extends CompleteStatement {

        CompleteSelectStatement(String builtString) {
            super(builtString);
        }
    }

    public static class OffsetSelectPart {
        private LimitSelectPart limitSelect;

        OffsetSelectPart(LimitSelectPart limitSelect) {
            this.limitSelect = limitSelect;
        }

        public CompleteSelectStatement offset(long offset) {
            limitSelect.append(" OFFSET " + offset);
            return limitSelect;
        }

        public CompleteSelectStatement withoutOffset() {
            return limitSelect;
        }
    }

    public static class LimitSelectPart extends CompleteSelectStatement {

        LimitSelectPart(String builtString) {
            super(builtString);
        }

        public OffsetSelectPart limit(long limit) {
            append(" LIMIT " + limit);
            return new OffsetSelectPart(this);
        }
    }

    public static class OrderBySelectPart extends LimitSelectPart {

        OrderBySelectPart(String builtString) {
            super(builtString);
        }

        public LimitSelectPart orderBy(Object... orderExpressionObjects) {
            Objects.requireNonNull(orderExpressionObjects, "Order expression list cannot be null");

            if (orderExpressionObjects.length == 0)
                throw new IllegalArgumentException("ORDER BY must specify at least 1 column");

            append(
                    " ORDER BY " +
                            Arrays.stream(orderExpressionObjects)
                                    .map(o -> Objects.requireNonNull(o, "Order expression cannot be null").toString())
                                    .collect(Collectors.joining(", "))
            );

            return this;
        }
    }

    public static class HavingSelectPart {
        private GroupBySelectPart groupBySelect;

        HavingSelectPart(GroupBySelectPart groupBySelect) {
            this.groupBySelect = groupBySelect;
        }

        public OrderBySelectPart having(Object conditionObject) {
            Objects.requireNonNull(conditionObject, "HAVING condition cannot be null");

            groupBySelect.append(" HAVING " + conditionObject);
            return groupBySelect;
        }

        public OrderBySelectPart withoutHavingClause() {
            return groupBySelect;
        }
    }

    public static class GroupBySelectPart extends OrderBySelectPart {

        GroupBySelectPart(String builtString) {
            super(builtString);
        }

        public HavingSelectPart groupBy(Object... schemaColumnObjects) {
            Objects.requireNonNull(schemaColumnObjects, "Column list cannot be null");

            if (schemaColumnObjects.length == 0)
                throw new IllegalArgumentException("GROUP BY must specify at least 1 column");

            append(
                    " GROUP BY " +
                            Arrays.stream(schemaColumnObjects)
                                    .map(o -> Objects.requireNonNull(o, "Column object cannot be null").toString())
                                    .collect(Collectors.joining(", "))
            );

            return new HavingSelectPart(this);
        }
    }

    public static class WhereSelectPart extends GroupBySelectPart {

        WhereSelectPart(String builtString) {
            super(builtString);
        }

        public GroupBySelectPart where(Object conditionObject) {
            Objects.requireNonNull(conditionObject, "WHERE condition cannot be null");

            append(" WHERE " + conditionObject);
            return this;
        }
    }

    public static class FromSelectPart {
        private SelectPart select;

        FromSelectPart(SelectPart select) {
            this.select = select;
        }

        public WhereSelectPart from(Object tableObject) {
            Objects.requireNonNull(tableObject, "Table expression cannot be null");

            select.append(" FROM " + tableObject);
            return select;
        }
    }

    static class SelectPart extends WhereSelectPart {

        SelectPart() {
            super("SELECT ");
        }

        FromSelectPart select(boolean distinct, Object... columnObjects) {
            Objects.requireNonNull(columnObjects, "Column expression list cannot be null");

            if (distinct)
                append("DISTINCT ");

            if (columnObjects.length == 0) {
                append("*");
            } else {
                append(
                        Arrays.stream(columnObjects)
                                .map(o -> Objects.requireNonNull(o, "Column expression cannot be null").toString())
                                .collect(Collectors.joining(", "))
                );
            }

            return new FromSelectPart(this);
        }
    }
}
