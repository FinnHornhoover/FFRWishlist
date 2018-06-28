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

package finnhh.ffrwishlist.model.parser;

import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.item.*;
import finnhh.ffrwishlist.model.parser.container.IncludeExcludeQueryContainer;
import finnhh.ffrwishlist.model.parser.container.NumericQueryContainer;
import finnhh.ffrwishlist.model.parser.container.SetQueryContainer;
import finnhh.ffrwishlist.model.parser.container.base.QueryContainer;

import java.util.*;
import java.util.stream.Collectors;

public class QueryParser {
    public static final String PARAMETER_SPLITTER = "--";
    public static final String SUPERTYPE_NEGATION = "!";

    private QueryContainer<Integer> levelQueries;
    private QueryContainer<Integer> amountQueries;
    private QueryContainer<Type> typeQueries;
    private QueryContainer<Rarity> rarityQueries;
    private SetQueryContainer setQueries;

    private List<String> rawStringQueries;
    private List<String> invalidQueries;

    private Queue<String> valuesToInsertToQuery;

    public QueryParser() {
        levelQueries = new NumericQueryContainer(QueryableColumn.LEVEL);
        amountQueries = new NumericQueryContainer(QueryableColumn.AMOUNT);
        typeQueries = new IncludeExcludeQueryContainer<>(QueryableColumn.TYPE);
        rarityQueries = new IncludeExcludeQueryContainer<>(QueryableColumn.RARITY);
        setQueries = new SetQueryContainer();

        rawStringQueries = new ArrayList<>();
        invalidQueries = new ArrayList<>();

        valuesToInsertToQuery = new ArrayDeque<>();
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

    private String getWhereStatementChecks() {
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
            if (!check.isEmpty())
                checkJoiner.add(check);
        }

        return checkJoiner.toString();
    }

    private String getErrorString() {
        String invalidQueriesJoin = invalidQueries.stream()
                .map(ep -> "\"" + ep + "\"")
                .collect(Collectors.joining(", "));

        if (invalidQueriesJoin.isEmpty())
            return "";
        else
            return "Error on parsing: " + invalidQueriesJoin;
    }

    private ParsedQueryInformation parse(boolean wishlistModeInformationExists, String stringToParse, boolean wishlistMode) {
        resetQueryParser();

        String[] allParams = stringToParse.split(PARAMETER_SPLITTER);

        //[0]: always the name, the rest is interpreted as args
        String searchString = allParams[0].trim();
        String[] params = new String[allParams.length - 1];

        for (int i = 0; i < params.length; i++)
            params[i] = allParams[i + 1].trim();

        //add name query as a raw string query, add the name value to the queue
        rawStringQueries.add(QueryableColumn.NAME + " LIKE ?");
        valuesToInsertToQuery.add(likeQueryArgument(searchString));

        //decide wishlist mode action
        if (wishlistModeInformationExists && wishlistMode)
            amountQueries.addToEntries(QueryComparison.GREATER_THAN, Amount.NONE.intValue());
        else if (wishlistModeInformationExists)
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
                if (param.startsWith(SUPERTYPE_NEGATION)) {
                    supertype = Supertype.correspondingTo(param.substring(1).trim());

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

                                //account for undefined checks, they're non-numeric (basic) comparisons
                                if (refLevel != Level.INVALID_LEVEL &&
                                        (refLevel != Level.UNDEFINED || comparison.isBasicComparison())) {
                                    levelQueries.addToEntries(comparison, refLevel.intValue());
                                    paramValid = true;
                                }
                            }
                            break;
                        case AMOUNT:
                            if (wishlistModeInformationExists && wishlistMode) {
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

                            if (refType != Type.INVALID_TYPE && comparison.isBasicComparison()) {
                                typeQueries.addToEntries(comparison, refType);
                                paramValid = true;
                            }
                            break;
                        case RARITY:
                            //find rarity referenced
                            Rarity refRarity = Rarity.correspondingTo(valueString);

                            if (refRarity != Rarity.INVALID_RARITY && comparison.isBasicComparison()) {
                                rarityQueries.addToEntries(comparison, refRarity);
                                paramValid = true;
                            }
                            break;
                        case SETNAME:
                            if (comparison.isBasicComparison()) {
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

        return new ParsedQueryInformation(
                searchString,
                getErrorString(),
                getWhereStatementChecks(),
                valuesToInsertToQuery
        );
    }

    public ParsedQueryInformation parse(String stringToParse) {
        return parse(false, stringToParse, false);
    }

    public ParsedQueryInformation parse(String stringToParse, boolean wishlistMode) {
        return parse(true, stringToParse, wishlistMode);
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
                .filter(qCol -> qCol.isFilterColumn() && !qCol.isFlexibleFilterColumn())
                .collect(Collectors.toList());

        QueryableColumn selectedColumn = sampleColumns.get(randomChoice.nextInt(sampleColumns.size()));

        List<QueryComparison> sampleComparisons;

        if (selectedColumn.isNumericFilterColumn()) {
            sampleComparisons = Arrays.stream(QueryComparison.values())
                    .filter(QueryComparison::isNumericComparison)
                    .collect(Collectors.toList());
        } else {
            sampleComparisons = Arrays.stream(QueryComparison.values())
                    .filter(QueryComparison::isBasicComparison)
                    .collect(Collectors.toList());
        }

        QueryComparison selectedComparison = sampleComparisons.get(randomChoice.nextInt(sampleComparisons.size()));

        String sampleQueryForm = PARAMETER_SPLITTER +
                                    selectedColumn.toString().toLowerCase(Locale.ENGLISH) + " " +
                                    selectedComparison + " ";

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
                        sampleTypes.get(randomChoice.nextInt(sampleTypes.size())).toString().toLowerCase(Locale.ENGLISH);
            case RARITY:
                List<Rarity> sampleRarities = Arrays.stream(Rarity.values())
                        .filter(r -> r != Rarity.INVALID_RARITY)
                        .collect(Collectors.toList());

                return sampleQueryForm +
                        sampleRarities.get(randomChoice.nextInt(sampleRarities.size())).toString().toLowerCase(Locale.ENGLISH);
            default:
                return "";
        }
    }
}
