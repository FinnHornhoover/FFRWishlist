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

import finnhh.ffrwishlist.model.database.sql.expression.base.SQLExpression;

import java.util.Objects;

public class ConditionExpression extends SQLExpression {

    protected ConditionExpression(String expressionString) {
        super(expressionString);
    }

    public static ChainConditionExpression withParentheses(CompleteConditionExpression completeConditionExpression) {
        return new ChainConditionExpression(new CompleteConditionExpression("(" + completeConditionExpression + ")"));
    }

    public static CheckConditionExpressionPart forColumnExpression(Object columnObject) {
        return new ColumnConditionExpressionPart().columnExpression(columnObject);
    }

    public static class CompleteConditionExpression extends ConditionExpression {

        CompleteConditionExpression(String expressionString) {
            super(expressionString);
        }
    }

    public static class ChainConditionExpression extends CompleteConditionExpression {

        ChainConditionExpression(CompleteConditionExpression completeConditionExpression) {
            super(completeConditionExpression.toString());
        }

        public ColumnConditionExpressionPart and() {
            append(" AND ");
            return new ColumnConditionExpressionPart(this);
        }

        public ColumnConditionExpressionPart or() {
            append(" OR ");
            return new ColumnConditionExpressionPart(this);
        }
    }

    public static class ValueConditionExpressionPart {
        private ColumnConditionExpressionPart columnConditionExpressionPart;

        ValueConditionExpressionPart(ColumnConditionExpressionPart columnConditionExpressionPart) {
            this.columnConditionExpressionPart = columnConditionExpressionPart;
        }

        public ChainConditionExpression value(Object valueObject) {
            columnConditionExpressionPart.append(" " + ValueExpression.fromObject(valueObject));
            return new ChainConditionExpression(columnConditionExpressionPart);
        }

        public ChainConditionExpression placeholderValue() {
            columnConditionExpressionPart.append(" ?");
            return new ChainConditionExpression(columnConditionExpressionPart);
        }

        public ChainConditionExpression nullValue() {
            columnConditionExpressionPart.append(" NULL");
            return new ChainConditionExpression(columnConditionExpressionPart);
        }
    }

    public static class ValueListConditionExpressionPart {
        private ColumnConditionExpressionPart columnConditionExpressionPart;

        ValueListConditionExpressionPart(ColumnConditionExpressionPart columnConditionExpressionPart) {
            this.columnConditionExpressionPart = columnConditionExpressionPart;
        }

        public ChainConditionExpression values(Object... valueObjects) {
            columnConditionExpressionPart.append(" " + ValueExpression.fromObjects(valueObjects));
            return new ChainConditionExpression(columnConditionExpressionPart);
        }

        public ChainConditionExpression valuesFromSubQuery(Object subQueryObject) {
            columnConditionExpressionPart.append(" " + ValueExpression.fromSubQuery(subQueryObject));
            return new ChainConditionExpression(columnConditionExpressionPart);
        }
    }

    public static class CheckConditionExpressionPart {
        private ColumnConditionExpressionPart columnConditionExpressionPart;

        CheckConditionExpressionPart(ColumnConditionExpressionPart columnConditionExpressionPart) {
            this.columnConditionExpressionPart = columnConditionExpressionPart;
        }

        public ValueConditionExpressionPart is() {
            columnConditionExpressionPart.append(" IS");
            return new ValueConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueConditionExpressionPart isNot() {
            columnConditionExpressionPart.append(" IS NOT");
            return new ValueConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueConditionExpressionPart like() {
            columnConditionExpressionPart.append(" LIKE");
            return new ValueConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueConditionExpressionPart notLike() {
            columnConditionExpressionPart.append(" NOT LIKE");
            return new ValueConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueConditionExpressionPart check(Object checkObject) {
            Objects.requireNonNull(checkObject);

            columnConditionExpressionPart.append(" " + checkObject);
            return new ValueConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueListConditionExpressionPart in() {
            columnConditionExpressionPart.append(" IN");
            return new ValueListConditionExpressionPart(columnConditionExpressionPart);
        }

        public ValueListConditionExpressionPart notIn() {
            columnConditionExpressionPart.append(" NOT IN");
            return new ValueListConditionExpressionPart(columnConditionExpressionPart);
        }
    }

    public static class ColumnConditionExpressionPart extends CompleteConditionExpression {

        ColumnConditionExpressionPart() {
            super("");
        }

        ColumnConditionExpressionPart(ChainConditionExpression chainConditionExpression) {
            super(chainConditionExpression.toString());
        }

        public CheckConditionExpressionPart columnExpression(Object columnObject) {
            Objects.requireNonNull(columnObject, "Column expression cannot be null");

            append(columnObject.toString());
            return new CheckConditionExpressionPart(this);
        }
    }
}
