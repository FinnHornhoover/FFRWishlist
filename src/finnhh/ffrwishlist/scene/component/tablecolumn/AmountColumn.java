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
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.resources.ResourceHolder;
import finnhh.ffrwishlist.scene.component.tablecolumn.base.ItemPackTableColumn;
import finnhh.ffrwishlist.model.event.ModelEvent;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import java.util.Comparator;

public class AmountColumn extends ItemPackTableColumn<ItemPack> {
    public static final int     AMOUNT_COL_MIN_WIDTH        = 96;
    public static final int     AMOUNT_TILEPANE_MIN_WIDTH   = 140;
    public static final int     AMOUNT_BUTTONS_SIZE         = ResourceHolder.BUTTON_ICONS_SIZE + 8;
    public static final String  COLUMN_NAME                 = "Amount";

    private EventHandler<ModelEvent<ItemPack>> onItemPackAdd = ItemPack.DEFAULT_ITEMPACK_EVENT_HANDLER;
    private EventHandler<ModelEvent<ItemPack>> onItemPackIncreaseAmount = ItemPack.DEFAULT_ITEMPACK_EVENT_HANDLER;
    private EventHandler<ModelEvent<ItemPack>> onItemPackDecreaseAmount = ItemPack.DEFAULT_ITEMPACK_EVENT_HANDLER;

    public AmountColumn() {
        setMinWidth(AMOUNT_COL_MIN_WIDTH);
        setText(COLUMN_NAME);
    }

    @Override
    protected void setItemPackTableCellValueFactory() {
        setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    }

    @Override
    protected void setItemPackTableCellFactory() {
        setCellFactory(cfData -> new TableCell<ItemPack, ItemPack>() {
            private final ImageView plusImageViewInsert;
            private final ImageView plusImageView;
            private final ImageView minusImageView;
            private final Button plusButtonInsert;
            private final Button plusButton;
            private final Button minusButton;
            private final Label amountLabel;
            private final TilePane tilePane;

            {
                plusImageViewInsert = new ImageView();
                plusImageViewInsert.setFitWidth(ResourceHolder.BUTTON_ICONS_SIZE);
                plusImageViewInsert.setPreserveRatio(true);

                plusImageView = new ImageView();
                plusImageView.setFitWidth(ResourceHolder.BUTTON_ICONS_SIZE);
                plusImageView.setPreserveRatio(true);

                minusImageView = new ImageView();
                minusImageView.setFitWidth(ResourceHolder.BUTTON_ICONS_SIZE);
                minusImageView.setPreserveRatio(true);

                plusButtonInsert = new Button();
                plusButtonInsert.setMinSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                plusButtonInsert.setMaxSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                plusButtonInsert.setGraphic(plusImageViewInsert);

                plusButton = new Button();
                plusButton.setMinSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                plusButton.setMaxSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                plusButton.setGraphic(plusImageView);

                minusButton = new Button();
                minusButton.setMinSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                minusButton.setMaxSize(AMOUNT_BUTTONS_SIZE, AMOUNT_BUTTONS_SIZE);
                minusButton.setGraphic(minusImageView);

                amountLabel = new Label();
                amountLabel.setAlignment(Pos.CENTER);

                tilePane = new TilePane();
                tilePane.setPrefTileHeight(0);
                tilePane.setMinWidth(AMOUNT_TILEPANE_MIN_WIDTH);
                tilePane.setAlignment(Pos.CENTER);
                tilePane.getChildren().addAll(plusButton, amountLabel, minusButton);
            }

            @Override
            protected void updateItem(ItemPack item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    if (item.getAmount() < Amount.MINIMUM.intValue()) {
                        plusImageViewInsert.setImage(ResourceHolder.PLUS_ICON);

                        plusButtonInsert.setOnAction(event -> onItemPackAdd.handle(new ModelEvent<>(event, item)));

                        setGraphic(plusButtonInsert);
                    } else {
                        plusImageView.setImage(ResourceHolder.PLUS_ICON);

                        plusButton.setOnAction(event -> onItemPackIncreaseAmount.handle(new ModelEvent<>(event, item)));

                        minusImageView.setImage(ResourceHolder.MINUS_ICON);

                        minusButton.setOnAction(event -> onItemPackDecreaseAmount.handle(new ModelEvent<>(event, item)));

                        amountLabel.setText(String.valueOf(item.getAmount()));

                        setGraphic(tilePane);
                    }
                }
            }
        });
    }

    @Override
    protected void setItemPackTableColumnComparator() {
        setComparator(Comparator.comparingInt(ItemPack::getAmount));
    }

    public EventHandler<ModelEvent<ItemPack>> getOnItemPackAdd() {
        return onItemPackAdd;
    }

    public void setOnItemPackAdd(EventHandler<ModelEvent<ItemPack>> onItemPackAdd) {
        this.onItemPackAdd = onItemPackAdd;
    }

    public EventHandler<ModelEvent<ItemPack>> getOnItemPackIncreaseAmount() {
        return onItemPackIncreaseAmount;
    }

    public void setOnItemPackIncreaseAmount(EventHandler<ModelEvent<ItemPack>> onItemPackIncreaseAmount) {
        this.onItemPackIncreaseAmount = onItemPackIncreaseAmount;
    }

    public EventHandler<ModelEvent<ItemPack>> getOnItemPackDecreaseAmount() {
        return onItemPackDecreaseAmount;
    }

    public void setOnItemPackDecreaseAmount(EventHandler<ModelEvent<ItemPack>> onItemPackDecreaseAmount) {
        this.onItemPackDecreaseAmount = onItemPackDecreaseAmount;
    }
}
