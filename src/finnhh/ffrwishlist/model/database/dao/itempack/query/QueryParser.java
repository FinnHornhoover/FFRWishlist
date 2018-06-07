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

package finnhh.ffrwishlist.model.database.dao.itempack.query;

import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.item.*;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.IncludeExcludeQueryContainer;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.NumericQueryContainer;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.SetQueryContainer;
import finnhh.ffrwishlist.model.database.dao.itempack.query.container.base.QueryContainer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class QueryParser {
    private QueryContainer<Integer> levelQueries;
    private QueryContainer<Integer> amountQueries;
    private QueryContainer<Type>    typeQueries;
    private QueryContainer<Rarity>  rarityQueries;
    private SetQueryContainer       setQueries;

    private List<String>            rawStringQueries;
    private List<String>            invalidQueries;

    private Queue<String>           valuesToInsertToQuery;

    public QueryParser() {
        this.levelQueries           = new NumericQueryContainer(QueryableColumn.LEVEL);
        this.amountQueries          = new NumericQueryContainer(QueryableColumn.AMOUNT);
        this.typeQueries            = new IncludeExcludeQueryContainer<>(QueryableColumn.TYPE);
        this.rarityQueries          = new IncludeExcludeQueryContainer<>(QueryableColumn.RARITY);
        this.setQueries             = new SetQueryContainer();

        this.rawStringQueries       = new ArrayList<>();
        this.invalidQueries         = new ArrayList<>();

        this.valuesToInsertToQuery  = new ArrayDeque<>();
    }

    private void resetQueryParser() {
        levelQueries.clear();
        amountQueries.clear();
        typeQueries.clear();
        rarityQueries.clear();
        setQueries.clear();

        rawStringQueries.clear();
        invalidQueries.clear();

        valuesToInsertToQuery.clear();
    }

    public void parse(boolean wishlistMode, String name, String... params) {
        resetQueryParser();

        //add name query as a raw string query, add the name value to the queue
        rawStringQueries.add(QueryableColumn.NAME + " LIKE ?");
        valuesToInsertToQuery.add(likeQueryArgument(name));

        //decide wishlist mode action
        if (wishlistMode)
            amountQueries.addToEntries(QueryComparison.GREATER_THAN, Amount.NONE.intValue());
        else
            rawStringQueries.add(QueryableColumn.AMOUNT + " IS NULL");

        for (String param : params) {
            //flag to determine the validity of the parameter
            boolean paramValid = false;
            //check if arg contains an operator
            QueryComparison comparison = QueryComparison.containsOperator(param);

            //check for args that are entered without an operator
            if (comparison == QueryComparison.NO_COMPARISON) {
                //supertype check
                Supertype supertype;

                //supertype negation check
                if (param.startsWith("!")) {
                    supertype = Supertype.correspondingTo(param.substring(1));

                    if (supertype != Supertype.INVALID_SUPERTYPE) {
                        Arrays.stream(supertype.getTypes())
                                .forEach(t -> typeQueries.addToEntries(QueryComparison.NOT_EQUAL_TO, t));
                        paramValid = true;
                    }
                } else {
                    supertype = Supertype.correspondingTo(param);

                    if (supertype != Supertype.INVALID_SUPERTYPE) {
                        Arrays.stream(supertype.getTypes())
                                .forEach(t -> typeQueries.addToEntries(QueryComparison.EQUAL_TO, t));
                        paramValid = true;
                    }
                }

            } else {
                //[0]: referenced column
                //[1]: value
                String[] parsedOnOp = param.split(comparison.toString());

                if (parsedOnOp.length == 2) {
                    //trim both
                    String columnString = parsedOnOp[0].trim();
                    String valueString  = parsedOnOp[1].trim();

                    //parse referenced column
                    QueryableColumn refColumn = QueryableColumn.correspondingTo(columnString);

                    switch (refColumn) {
                        case LEVEL:
                            try {
                                levelQueries.addToEntries(comparison, Integer.parseInt(valueString));
                                paramValid = true;
                            } catch (NumberFormatException e) {
                                //if level is not a number, check for special level names
                                Level refLevel = Level.correspondingTo(valueString);

                                //account for undefined checks, they're non-numeric comparisons
                                if (refLevel != Level.INVALID_LEVEL &&
                                        (refLevel != Level.UNDEFINED || comparison.isNonNumericComparison())) {
                                    levelQueries.addToEntries(comparison, refLevel.intValue());
                                    paramValid = true;
                                }
                            }
                            break;
                        case AMOUNT:
                            if (wishlistMode) {
                                try {
                                    amountQueries.addToEntries(comparison, Integer.parseInt(valueString));
                                    paramValid = true;
                                } catch (NumberFormatException e) {
                                    //if amount is not a number, check for special amount names
                                    Amount refAmount = Amount.correspondingTo(valueString);

                                    if (refAmount != Amount.INVALID_AMOUNT) {
                                        amountQueries.addToEntries(comparison, refAmount.intValue());
                                        paramValid = true;
                                    }
                                }
                            }
                            break;
                        case TYPE:
                            //find type referenced
                            Type refType = Type.correspondingTo(valueString);

                            if (refType != Type.INVALID_TYPE && comparison.isNonNumericComparison()) {
                                typeQueries.addToEntries(comparison, refType);
                                paramValid = true;
                            }
                            break;
                        case RARITY:
                            //find rarity referenced
                            Rarity refRarity = Rarity.correspondingTo(valueString);

                            if (refRarity != Rarity.INVALID_RARITY && comparison.isNonNumericComparison()) {
                                rarityQueries.addToEntries(comparison, refRarity);
                                paramValid = true;
                            }
                            break;
                        case SETNAME:
                            if (comparison.isNonNumericComparison()) {
                                setQueries.addToEntries(comparison, likeQueryArgument(valueString));
                                paramValid = true;
                            }
                            break;
                    }
                }
            }

            if (!paramValid)
                invalidQueries.add(param);
        }

        //add queued set query values to the general values queue
        valuesToInsertToQuery.addAll(setQueries.getInputQueue());
    }

    public String getWhereStatementChecks() {
        StringJoiner checkJoiner = new StringJoiner(" AND ");

        String[] allChecks = new String[] {
                String.join(" AND ", rawStringQueries),
                amountQueries.getJoinedString(),
                levelQueries.getJoinedString(),
                typeQueries.getJoinedString(),
                rarityQueries.getJoinedString(),
                setQueries.getJoinedString()
        };

        for (String check : allChecks) {
            if (!check.equals(""))
                checkJoiner.add(check);
        }

        return checkJoiner.toString();
    }

    public String getErrorString() {
        String invalidQueriesJoin = invalidQueries.stream()
                                                .map(ep -> "\"" + ep + "\"")
                                                .collect(Collectors.joining(", "));

        if (invalidQueriesJoin.equals(""))
            return "";
        else
            return "Error on parsing: " + invalidQueriesJoin;
    }

    public Queue<String> getValuesToInsertToQuery() {
        return valuesToInsertToQuery;
    }

    public static String likeQueryArgument(String s) {
        if (s.startsWith("\"") && s.endsWith("\""))
            return s.substring(1, s.length() - 1);
        else if (s.startsWith("\""))
            return s.substring(1) + "%";
        else if (s.endsWith("\""))
            return "%" + s.substring(0, s.length() - 1);
        else
            return "%" + s + "%";
    }

    public static String getSampleQuery() {
        Random randomChoice = new Random();

        List<QueryableColumn> sampleColumns = Arrays.stream(QueryableColumn.values())
                .filter(qCol -> {
                    try {
                        Field f = qCol.getClass().getField(qCol.name());

                        return f.isAnnotationPresent(QueryableColumn.ParameterQueryable.class)
                                && !f.isAnnotationPresent(QueryableColumn.FlexibleQueries.class);
                    } catch (NoSuchFieldException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        QueryableColumn selectedColumn = sampleColumns.get(randomChoice.nextInt(sampleColumns.size()));

        List<QueryComparison> sampleComparisons;
        boolean hasNonNumericQueries = false;

        try {
            Field f = selectedColumn.getClass().getField(selectedColumn.name());

            hasNonNumericQueries = f.isAnnotationPresent(QueryableColumn.NonNumericQueries.class);
        } catch (NoSuchFieldException ignored) {
        }

        if (hasNonNumericQueries) {
            sampleComparisons = Arrays.stream(QueryComparison.values())
                    .filter(qComp -> {
                        try {
                            Field f = qComp.getClass().getField(qComp.name());

                            return f.isAnnotationPresent(QueryComparison.NonNumericComparison.class);
                        } catch (NoSuchFieldException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            sampleComparisons = Arrays.asList(QueryComparison.values());
        }

        QueryComparison selectedComparison = sampleComparisons.get(randomChoice.nextInt(sampleComparisons.size()));

        String sampleQueryForm = "--" + selectedColumn.toString().toLowerCase() + " " + selectedComparison + " ";

        switch (selectedColumn) {
            case LEVEL:
                return sampleQueryForm + randomChoice.nextInt(Level.MAXIMUM.intValue() + 1);
            case AMOUNT:
                return sampleQueryForm + (randomChoice.nextInt(Amount.MAXIMUM.intValue()) + 1);
            case TYPE:
                List<Type> sampleTypes = Arrays.stream(Type.values())
                        .filter(t -> t != Type.INVALID_TYPE)
                        .collect(Collectors.toList());

                return sampleQueryForm +
                        sampleTypes.get(randomChoice.nextInt(sampleTypes.size())).toString().toLowerCase();
            case RARITY:
                List<Rarity> sampleRarities = Arrays.stream(Rarity.values())
                        .filter(r -> r != Rarity.INVALID_RARITY)
                        .collect(Collectors.toList());

                return sampleQueryForm +
                        sampleRarities.get(randomChoice.nextInt(sampleRarities.size())).toString().toLowerCase();
            default:
                return "";
        }
    }
}
