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

package finnhh.ffrwishlist.model.database.sql.expression;

import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.database.sql.expression.base.SQLExpression;
import finnhh.ffrwishlist.model.database.sql.statement.SelectSQLStatement;

import java.util.Objects;

public class TableExpression extends SQLExpression {

    protected TableExpression(String expressionString) {
        super(expressionString);
    }

    private ConditionalJoinedExpression join(String joinTypeName, Object secondExpressionObject) {
        Objects.requireNonNull(secondExpressionObject, "Second expression cannot be null");

        append(" " + joinTypeName + " " + secondExpressionObject);
        return new ConditionalJoinedExpression(this);
    }

    public TableExpression naturalJoin(Object secondExpressionObject) {
        return join("NATURAL JOIN", secondExpressionObject).noCondition();
    }

    public TableExpression crossJoin(Object secondExpressionObject) {
        return join("CROSS JOIN", secondExpressionObject).noCondition();
    }

    public ConditionalJoinedExpression innerJoin(Object secondExpressionObject) {
        return join("INNER JOIN", secondExpressionObject);
    }

    public ConditionalJoinedExpression join(Object secondExpressionObject) {
        return join("JOIN", secondExpressionObject);
    }

    public ConditionalJoinedExpression leftJoin(Object secondExpressionObject) {
        return join("LEFT JOIN", secondExpressionObject);
    }

    public ConditionalJoinedExpression rightJoin(Object secondExpressionObject) {
        return join("RIGHT JOIN", secondExpressionObject);
    }

    public ConditionalJoinedExpression outerJoin(Object secondExpressionObject) {
        return join("OUTER JOIN", secondExpressionObject);
    }

    public static TableExpression fromTable(SchemaTable schemaTable) {
        return new TableExpression(schemaTable.toString());
    }

    public static TableExpression fromSubQuery(SelectSQLStatement.CompleteSelectStatement subQuery) {
        return new TableExpression("(" + subQuery.getBuiltString() + ")");
    }

    public static class ConditionalJoinedExpression {
        private TableExpression tableExpression;

        ConditionalJoinedExpression(TableExpression tableExpression) {
            this.tableExpression = tableExpression;
        }

        private TableExpression noCondition() {
            return tableExpression;
        }

        public TableExpression using(Object schemaColumnObject) {
            Objects.requireNonNull(schemaColumnObject, "USING column cannot be null");

            tableExpression.append(" USING (" + schemaColumnObject + ")");
            return tableExpression;
        }

        public TableExpression on(Object conditionObject) {
            Objects.requireNonNull(conditionObject, "JOIN ON condition cannot be null");

            tableExpression.append(" ON " + conditionObject);
            return tableExpression;
        }
    }
}
