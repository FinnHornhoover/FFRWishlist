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

import java.util.Objects;

public final class DeleteSQLStatement extends SQLStatement {

    private DeleteSQLStatement() { }

    public WhereDeletePart deleteFrom(Object schemaTableObject) {
        return new DeletePart().deleteFrom(schemaTableObject);
    }

    public static DeleteSQLStatement builder() {
        return new DeleteSQLStatement();
    }

    public static class CompleteDeleteStatement extends CompleteStatement {

        CompleteDeleteStatement(String builtString) {
            super(builtString);
        }
    }

    public static class WhereDeletePart extends CompleteDeleteStatement {

        WhereDeletePart(String builtString) {
            super(builtString);
        }

        public CompleteDeleteStatement where(Object conditionObject) {
            Objects.requireNonNull(conditionObject, "WHERE condition cannot be null");

            append(" WHERE " + conditionObject);
            return this;
        }
    }

    static class DeletePart extends WhereDeletePart {

        DeletePart() {
            super("DELETE ");
        }

        WhereDeletePart deleteFrom(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with DELETE cannot be null");

            append("FROM " + schemaTableObject);
            return this;
        }
    }
}
