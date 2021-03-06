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

import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.schema.column.ProfileSchemaColumn;
import finnhh.ffrwishlist.model.constants.database.schema.table.SchemaTable;
import finnhh.ffrwishlist.model.constants.profile.ProfileState;
import finnhh.ffrwishlist.model.database.dao.base.DataAccessObject;
import finnhh.ffrwishlist.model.database.sql.SQLBuilders;
import finnhh.ffrwishlist.model.database.sql.expression.ConditionExpression;
import finnhh.ffrwishlist.model.database.sql.expression.InstructionExpression;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO extends DataAccessObject {

    public ProfileDAO() { }

    public List<Profile> getAllProfiles() {
        final List<Profile> listOfProfiles = new ArrayList<>();

        runOnStatementNoThrow(statement -> {
            ResultSet resultSet = statement.executeQuery(
                    SQLBuilders.selectBuilder()
                            .select(
                                    ProfileSchemaColumn.PROFILEID,
                                    ProfileSchemaColumn.PROFILENAME,
                                    ProfileSchemaColumn.ACTIVE
                            )
                            .from(SchemaTable.PROFILES)
                            .toString()
            );

            while (resultSet.next()) {
                listOfProfiles.add(new Profile(
                        resultSet.getInt(ProfileSchemaColumn.PROFILEID.name()),
                        resultSet.getString(ProfileSchemaColumn.PROFILENAME.name()),
                        resultSet.getInt(ProfileSchemaColumn.ACTIVE.name()) == ProfileState.ACTIVE.intValue()
                ));
            }
        });

        return listOfProfiles;
    }

    public void activateProfile(final Profile profile) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.updateBuilder()
                        .update(SchemaTable.PROFILES)
                        .set(InstructionExpression
                                .forColumn(ProfileSchemaColumn.ACTIVE)
                                .setValue(ProfileState.ACTIVE)
                        )
                        .where(ConditionExpression
                                .forColumnExpression(ProfileSchemaColumn.PROFILEID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(profile.getProfileID())
                        )
                        .toString()
        ));
    }

    public void deactivateProfile(final Profile profile) {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.updateBuilder()
                        .update(SchemaTable.PROFILES)
                        .set(InstructionExpression
                                .forColumn(ProfileSchemaColumn.ACTIVE)
                                .setValue(ProfileState.INACTIVE)
                        )
                        .where(ConditionExpression
                                .forColumnExpression(ProfileSchemaColumn.PROFILEID)
                                .check(QueryComparison.EQUAL_TO)
                                .value(profile.getProfileID())
                        )
                        .toString()
        ));
    }

    public void clearAllActiveProfileStates() {
        runOnStatementNoThrow(statement -> statement.executeUpdate(
                SQLBuilders.updateBuilder()
                        .update(SchemaTable.PROFILES)
                        .set(InstructionExpression
                                .forColumn(ProfileSchemaColumn.ACTIVE)
                                .setValue(ProfileState.INACTIVE)
                        )
                        .toString()
        ));
    }
}
