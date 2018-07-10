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

import finnhh.ffrwishlist.model.constants.base.IntegerValued;
import finnhh.ffrwishlist.model.database.sql.expression.base.SQLExpression;
import finnhh.ffrwishlist.model.database.sql.statement.SelectSQLStatement;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ValueExpression extends SQLExpression {

    protected ValueExpression(String expressionString) {
        super(expressionString);
    }

    public static ValueExpression fromObject(Object object) {
        if (object instanceof ValueExpression)
            return (ValueExpression) object;
        else if (object instanceof IntegerValued)
            return new ValueExpression(String.valueOf(((IntegerValued) object).intValue()));
        else if (object instanceof String)
            return new ValueExpression("\'" + object + "\'");
        else if (object instanceof Boolean)
            return new ValueExpression(((Boolean) object) ? "1" : "0");
        else if (object instanceof byte[])
            return new ValueExpression("X\'" + DatatypeConverter.printHexBinary((byte[]) object) + "\'");
        else
            return new ValueExpression(String.valueOf(object));
    }

    public static ValueExpression fromObjects(Object... objects) {
        Objects.requireNonNull(objects, "Value lists cannot be null");

        if (objects.length == 0)
            throw new IllegalArgumentException("Value lists must contain at least 1 object");

        return new ValueExpression(
                "(" +
                        Arrays.stream(objects)
                                .map(o -> ValueExpression.fromObject(o).toString())
                                .collect(Collectors.joining(", ")) +
                ")"
        );
    }

    public static ValueExpression fromSubQuery(Object selectStatementObject) {
        Objects.requireNonNull(selectStatementObject, "Sub query object cannot be null");

        if (selectStatementObject instanceof SelectSQLStatement.CompleteSelectStatement) {
            return new ValueExpression(
                    "(" + ((SelectSQLStatement.CompleteSelectStatement) selectStatementObject).getBuiltString() + ")"
            );
        } else {
            return new ValueExpression("(" + selectStatementObject + ")");
        }
    }
}
