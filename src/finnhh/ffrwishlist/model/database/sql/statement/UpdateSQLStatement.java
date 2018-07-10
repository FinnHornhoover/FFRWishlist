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

public final class UpdateSQLStatement extends SQLStatement {

    private UpdateSQLStatement() { }

    public SetUpdatePart update(Object schemaTableObject) {
        return new UpdatePart().update(schemaTableObject);
    }

    public SetUpdatePart updateOrReplace(Object schemaTableObject) {
        return new UpdatePart().updateOrReplace(schemaTableObject);
    }

    public SetUpdatePart updateOrIgnore(Object schemaTableObject) {
        return new UpdatePart().updateOrIgnore(schemaTableObject);
    }

    public static UpdateSQLStatement builder() {
        return new UpdateSQLStatement();
    }

    public static class CompleteUpdateStatement extends CompleteStatement {

        CompleteUpdateStatement(String builtString) {
            super(builtString);
        }
    }

    public static class WhereUpdatePart extends CompleteUpdateStatement {

        WhereUpdatePart(String builtString) {
            super(builtString);
        }

        public CompleteUpdateStatement where(Object conditionObject) {
            Objects.requireNonNull(conditionObject, "WHERE condition cannot be null");

            append(" WHERE " + conditionObject);
            return this;
        }
    }

    public static class SetUpdatePart {
        private UpdatePart updatePart;

        SetUpdatePart(UpdatePart updatePart) {
            this.updatePart = updatePart;
        }

        public WhereUpdatePart set(Object... updateObjects) {
            Objects.requireNonNull(updateObjects, "Update specification list cannot be null");

            if (updateObjects.length == 0)
                throw new IllegalArgumentException("SET clause requires at least one update expression");

            updatePart.append(
                    " SET " +
                            Arrays.stream(updateObjects)
                                    .map(o -> Objects.requireNonNull(o, "Update specification cannot be null").toString())
                                    .collect(Collectors.joining(", "))
            );

            return updatePart;
        }
    }

    public static class UpdatePart extends WhereUpdatePart {

        UpdatePart() {
            super("UPDATE ");
        }

        SetUpdatePart update(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with UPDATE cannot be null");

            append(schemaTableObject.toString());
            return new SetUpdatePart(this);
        }

        SetUpdatePart updateOrReplace(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with UPDATE cannot be null");

            append("OR REPLACE " + schemaTableObject);
            return new SetUpdatePart(this);
        }

        SetUpdatePart updateOrIgnore(Object schemaTableObject) {
            Objects.requireNonNull(schemaTableObject, "Table to use with UPDATE cannot be null");

            append("OR IGNORE " + schemaTableObject);
            return new SetUpdatePart(this);
        }
    }
}
