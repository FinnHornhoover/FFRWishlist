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

package finnhh.ffrwishlist.scene.component.tablecolumn;

import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.scene.component.tablecolumn.base.ItemPackTableColumn;
import finnhh.ffrwishlist.model.event.ModelEvent;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

public class SetsColumn extends ItemPackTableColumn<ObservableList<Set>> {
    public static final int     SETS_COL_MIN_WIDTH      = 132;
    public static final int     SETS_VBOX_SPACING       = 2;
    public static final String  SETS_LABELS_CSS_CLASS   = "setreference";
    public static final String  COLUMN_NAME             = "Sets";

    private EventHandler<ModelEvent<Set>> onSetSelectedFromTable = Set.DEFAULT_SET_EVENT_HANDLER;

    public SetsColumn() {
        setMinWidth(SETS_COL_MIN_WIDTH);
        setSortable(false);
        setText(COLUMN_NAME);
    }

    @Override
    protected void setItemPackTableCellValueFactory() {
        setCellValueFactory(cellData -> cellData.getValue().getItem().setsAssociatedProperty());
    }

    @Override
    protected void setItemPackTableCellFactory() {
        setCellFactory(cfData -> new TableCell<ItemPack, ObservableList<Set>>() {
            private final VBox labelsBox;

            {
                labelsBox = new VBox(SETS_VBOX_SPACING);
                labelsBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(ObservableList<Set> item, boolean empty) {
                super.updateItem(item, empty);

                labelsBox.getChildren().clear();

                if (!empty) {
                    if (!item.isEmpty()) {
                        item.forEach(set -> {
                            final Label setLabel = new Label(set.getSetName());
                            setLabel.getStyleClass().add(SETS_LABELS_CSS_CLASS);
                            setLabel.setOnMouseClicked(event -> onSetSelectedFromTable.handle(new ModelEvent<>(event, set)));
                            labelsBox.getChildren().add(setLabel);
                        });
                    } else {
                        labelsBox.getChildren().add(new Label("No Sets"));
                    }

                    setGraphic(labelsBox);
                }
            }
        });
    }

    public EventHandler<ModelEvent<Set>> getOnSetSelectedFromTable() {
        return onSetSelectedFromTable;
    }

    public void setOnSetSelectedFromTable(EventHandler<ModelEvent<Set>> onSetSelectedFromTable) {
        this.onSetSelectedFromTable = onSetSelectedFromTable;
    }
}
