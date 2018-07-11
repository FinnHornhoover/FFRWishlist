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

import finnhh.ffrwishlist.model.database.sql.expression.ValueExpression;
import finnhh.ffrwishlist.model.database.sql.statement.base.SQLStatement;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class InsertSQLStatement extends SQLStatement {

    private InsertSQLStatement() { }

    public ColumnInsertPart insertInto(Object schemaTableObject) {
        return new InsertPart().insertInto(schemaTableObject);
    }

    public ColumnInsertPart insertOrReplaceInto(Object schemaTableObject) {
        return new InsertPart().insertOrReplaceInto(schemaTableObject);
    }

    public ColumnInsertPart insertOrIgnoreInto(Object schemaTableObject) {
        return new InsertPart().insertOrIgnoreInto(schemaTableObject);
    }

    public static InsertSQLStatement builder() {
        return new InsertSQLStatement();
    }

    public static class CompleteInsertStatement extends CompleteStatement {

        CompleteInsertStatement(String builtString) {
            super(builtString);
        }
    }

    public static class ValueInsertPart {
        InsertPart insertPart;

        ValueInsertPart(InsertPart insertPart) {
            this.insertPart = insertPart;
        }

        public CompleteInsertStatement values(Object... objects) {
            Objects.requireNonNull(objects, "Value objects list cannot be null");

            if (objects.length == 0)
                throw new IllegalArgumentException("VALUES clause must specify at least one value");

            insertPart.append(
                    " VALUES (" +
                            Arrays.stream(objects)
                                    .map(o -> ValueExpression.fromObject(o).toString())
                                    .collect(Collectors.joining(", ")) +
                            ")"
            );

            return insertPart;
        }

        public CompleteInsertStatement defaultValues() {
            insertPart.append(" DEFAULT VALUES");
            return insertPart;
        }
    }

    public static class ColumnInsertPart extends ValueInsertPart {

        ColumnInsertPart(InsertPart insertPart) {
            super(insertPart);
        }

        public ValueInsertPart withColumnsSpecified(Object... schemaColumnObjects) {
            Objects.requireNonNull(schemaColumnObjects, "Specified columns list cannot be null");

            if (schemaColumnObjects.length > 0) {
                insertPart.append(
                        " (" +
                        Arrays.stream(schemaColumnObjects)
                                .map(o -> Objects.requireNonNull(o, "Specified column cannot be null").toString())
                                .collect(Collectors.joining(", ")) +
                        ")"
                );
            }

            return this;
        }
    }

    public static class InsertPart extends CompleteInsertStatement {

        InsertPart() {
            super("INSERT ");
        }

        ColumnInsertPart insertInto(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with INSERT cannot be null");

            append("INTO " + schemaTableObject);
            return new ColumnInsertPart(this);
        }

        ColumnInsertPart insertOrReplaceInto(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with INSERT cannot be null");

            append("OR REPLACE INTO " + schemaTableObject);
            return new ColumnInsertPart(this);
        }

        ColumnInsertPart insertOrIgnoreInto(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with INSERT cannot be null");

            append("OR IGNORE INTO " + schemaTableObject);
            return new ColumnInsertPart(this);
        }
    }
}
