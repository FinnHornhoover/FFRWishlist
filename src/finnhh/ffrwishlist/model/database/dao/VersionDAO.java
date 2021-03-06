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

package finnhh.ffrwishlist.model.database.dao;

import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.schema.column.VersionSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.expression.ConditionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.InstructionExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionDAO extends DataAccessObject {
    private static final int DB_ID = 387466;

    public VersionDAO() { }

    public int getVersion() throws ClassNotFoundException, SQLException {
        final IntegerProperty version = new SimpleIntegerProperty();

        runOnStatement(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(VersionSchemaColumn.VERSION)
                            .from(SchemaTable.VERSIONS)
                            .where(ConditionExpression
                                    .forColumnExpression(VersionSchemaColumn.DBID)
                                    .check(QueryComparison.EQUAL_TO)
                                    .value(DB_ID)
                            )
                            .toString()
            );

            version.setValue(resultSet.getInt(VersionSchemaColumn.VERSION.name()));
        });

        return version.get();
    }

    public void updateVersion(int newVersion) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.updateBuilder()
                        .update(SchemaTable.VERSIONS)
                        .set(InstructionExpression
                                .forColumn(VersionSchemaColumn.VERSION)
                                .setValue(newVersion)
                        )
                        .where(ConditionExpression
                                .forColumnExpression(VersionSchemaColumn.DBID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(DB_ID)
                        )
                        .toString()
        ));
    }
}
