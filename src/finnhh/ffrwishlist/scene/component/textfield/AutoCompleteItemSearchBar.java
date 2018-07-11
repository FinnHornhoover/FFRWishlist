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

package finnhh.ffrwishlist.scene.component.textfield;

import finnhh.ffrwishlist.model.constants.base.ItemAttribute;
import finnhh.ffrwishlist.model.constants.database.QueryComparison;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;
import finnhh.ffrwishlist.model.constants.item.*;
import finnhh.ffrwishlist.model.parser.QueryParser;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoCompleteItemSearchBar extends TextField {
    public static final String SEARCH_BAR_PROMPT_TEXT_DEFAULT = "Search for an item";

    private static final int SUGGESTION_LIMIT = 6;

    private ContextMenu suggestionsMenu;
    private Set<String> suggestedStrings;

    private Set<String> columnAutoCompleteStrings;
    private Set<AutoCompleteValue> valueAutoCompleteStrings;

    public AutoCompleteItemSearchBar() {
        suggestionsMenu = new ContextMenu();
        suggestedStrings = new TreeSet<>((s1, s2) ->
                (s1.length() == s2.length()) ? s1.compareToIgnoreCase(s2) : Integer.compare(s2.length(), s1.length()));

        columnAutoCompleteStrings = new TreeSet<>();
        valueAutoCompleteStrings = new TreeSet<>(Comparator.comparing(AutoCompleteValue::getString));

        configurePromptTexts();

        fillColumnAutoCompleteStrings();

        fillValueAutoCompleteStrings();

        addAutoCompleteListener();
    }

    private void configurePromptTexts() {
        focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                String sampleQuery = QueryParser.getSampleQuery();

                if (sampleQuery.isEmpty())
                    setPromptText(SEARCH_BAR_PROMPT_TEXT_DEFAULT);
                else
                    setPromptText(SEARCH_BAR_PROMPT_TEXT_DEFAULT + " or use a filter e.g. " + sampleQuery);
            }
        });
    }

    private void fillColumnAutoCompleteStrings() {
        Arrays.stream(QueryableColumn.values())
                .filter(QueryableColumn::isFilterColumn)
                .map(QueryableColumn::getAllRepresentations)
                .flatMap(Arrays::stream)
                .map(s -> s.toLowerCase(Locale.ENGLISH))
                .forEach(columnAutoCompleteStrings::add);
    }

    private void fillValueAutoCompleteStrings() {
        List<ItemAttribute> allItemAttributes = new ArrayList<>();

        allItemAttributes.addAll(Arrays.asList(Amount.values()));
        allItemAttributes.addAll(Arrays.asList(Level.values()));
        allItemAttributes.addAll(Arrays.asList(Rarity.values()));
        allItemAttributes.addAll(Arrays.asList(Supertype.values()));
        allItemAttributes.addAll(Arrays.asList(Type.values()));

        allItemAttributes.stream()
                .filter(ia -> !ia.isInvalid())
                .map(ia -> Arrays.stream(ia.getAllRepresentations())
                        .map(s -> new AutoCompleteValue(s.toLowerCase(Locale.ENGLISH), ia))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .forEach(valueAutoCompleteStrings::add);

    }

    private void handleSuggestionsMenuFor(String inputString, int caretPosition) {
        if (!suggestedStrings.isEmpty() && !inputString.isEmpty()) {
            int suggestionCount = suggestedStrings.size();
            int shownSuggestionsCount = (suggestionCount > SUGGESTION_LIMIT) ? SUGGESTION_LIMIT : suggestionCount;
            final String lowercaseInputString = inputString.toLowerCase(Locale.ENGLISH);

            suggestedStrings.stream()
                    .sorted(Comparator.comparingInt(s -> s.indexOf(lowercaseInputString)))
                    .limit(shownSuggestionsCount)
                    .map(ss -> {
                        MenuItem menuItem = new MenuItem(ss);
                        menuItem.setOnAction(event -> Platform.runLater(() -> onSuggestionSelected(ss)));
                        menuItem.setMnemonicParsing(false);
                        return menuItem;
                    })
                    .forEachOrdered(mi -> suggestionsMenu.getItems().add(mi));

            if (shownSuggestionsCount > 1
                    || !inputString.toLowerCase(Locale.ENGLISH).equals(suggestionsMenu.getItems().get(0).getText())) {
                Point2D point = getInputMethodRequests().getTextLocation(0);
                suggestionsMenu.show(this, point.getX(), point.getY());

                caretPositionProperty().addListener((observable, oldValue, newValue) -> {
                    if (caretPosition != newValue.intValue()) {
                        suggestionsMenu.hide();
                        suggestionsMenu.getItems().clear();
                    }
                });
            }
        }
    }

    private void addAutoCompleteListener() {
        textProperty().addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> {
                    suggestionsMenu.hide();
                    suggestionsMenu.getItems().clear();

                    int caretPosition = getCaretPosition();
                    String textConsidered = newValue.substring(0, caretPosition);

                    boolean suggestionsEnabled = !textConsidered.isEmpty()
                            && Character.isLetter(textConsidered.charAt(textConsidered.length() - 1));

                    int lastFilterIndex = textConsidered.lastIndexOf(QueryParser.PARAMETER_SPLITTER);

                    if (suggestionsEnabled && lastFilterIndex >= 0) {
                        String prefixedText =
                                textConsidered.substring(lastFilterIndex + QueryParser.PARAMETER_SPLITTER.length()).trim();
                        String filterText;

                        if (prefixedText.startsWith(QueryParser.SUPERTYPE_NEGATION))
                            filterText = prefixedText.substring(1).trim();
                        else
                            filterText = prefixedText;

                        QueryComparison comparison = QueryComparison.containsOperator(filterText);

                        suggestedStrings.clear();

                        if (comparison == QueryComparison.NO_COMPARISON) {
                            columnAutoCompleteStrings.stream()
                                    .filter(s -> s.contains(filterText.toLowerCase(Locale.ENGLISH)))
                                    .forEach(suggestedStrings::add);

                            valueAutoCompleteStrings.stream()
                                    .filter(acv -> acv.getString().contains(filterText.toLowerCase(Locale.ENGLISH))
                                            && acv.getValue().usesSingularFilters())
                                    .forEach(acv -> suggestedStrings.add(acv.getString()));

                            handleSuggestionsMenuFor(filterText, caretPosition);
                        } else {
                            int firstComparison = filterText.indexOf(comparison.toString());
                            String columnText = filterText.substring(0, firstComparison).trim();
                            String valueText = filterText.substring(firstComparison + comparison.toString().length()).trim();

                            QueryableColumn columnReferenced = QueryableColumn.correspondingTo(columnText);

                            if (columnReferenced != QueryableColumn.INVALID_COLUMN) {
                                valueAutoCompleteStrings.stream()
                                        .filter(acv -> acv.getString().contains(valueText.toLowerCase(Locale.ENGLISH))
                                                && acv.getValue().correspondingColumn() == columnReferenced
                                                && !acv.getValue().usesSingularFilters())
                                        .forEach(acv -> suggestedStrings.add(acv.getString()));
                            }

                            handleSuggestionsMenuFor(valueText, caretPosition);
                        }
                    }
                })
        );
    }

    private void onSuggestionSelected(String selectedSuggestion) {
        String currentSearchString = getText();
        int currentCaretPosition = getCaretPosition();

        String relevantPart = currentSearchString.substring(0, currentCaretPosition);
        String irrelevantPart = currentSearchString.substring(currentCaretPosition);

        QueryComparison comparison = QueryComparison.containsOperator(relevantPart);

        int[] indexes = {
                //end of last operand for comparisons
                (comparison == QueryComparison.NO_COMPARISON) ? -1 : relevantPart.lastIndexOf(comparison.toString()),
                //end of last supertype negation
                relevantPart.lastIndexOf(QueryParser.SUPERTYPE_NEGATION),
                //end of last parameter splitter
                relevantPart.lastIndexOf(QueryParser.PARAMETER_SPLITTER)
        };

        int lengths[] = {
                comparison.toString().length(),
                QueryParser.SUPERTYPE_NEGATION.length(),
                QueryParser.PARAMETER_SPLITTER.length()
        };

        int replacementIndex = -1;

        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] >= 0 && indexes[i] + lengths[i] > replacementIndex)
                replacementIndex = indexes[i] + lengths[i];
        }

        if (replacementIndex >= 0) {
            //also calculate a value based on the first incomplete column or value string of the last filter
            String lookup = relevantPart.substring(replacementIndex);
            int j = 0;

            while (j < lookup.length()) {
                if (Character.isLetter(lookup.charAt(j)))
                    break;

                j++;
            }

            replacementIndex += j;

            String relevantPartReplacement = currentSearchString.substring(0, replacementIndex) + selectedSuggestion;
            setText(relevantPartReplacement + irrelevantPart);
            positionCaret(relevantPartReplacement.length());
        }
    }

    private static class AutoCompleteValue {
        private String string;
        private ItemAttribute value;

        AutoCompleteValue(String string, ItemAttribute value) {
            this.string = string;
            this.value = value;
        }

        String getString() {
            return string;
        }

        ItemAttribute getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AutoCompleteValue
                    && this.string.equals(((AutoCompleteValue) obj).string)
                    && this.value == ((AutoCompleteValue) obj).value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(string, value);
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
