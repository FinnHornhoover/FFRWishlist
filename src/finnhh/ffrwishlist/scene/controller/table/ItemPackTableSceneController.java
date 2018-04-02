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

package finnhh.ffrwishlist.scene.controller.table;

import finnhh.ffrwishlist.model.Item;
import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.constants.item.Level;
import finnhh.ffrwishlist.model.constants.item.Rarity;
import finnhh.ffrwishlist.model.constants.item.Type;
import finnhh.ffrwishlist.resources.ResourceLoader;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.Map;

public abstract class ItemPackTableSceneController extends TableViewSceneController {
    public static final int ICON_SIZE                       = 64;
    public static final int BUTTON_ICONS_SIZE               = 32;
    public static final int ICON_COL_WIDTH                  = ICON_SIZE + 8;
    public static final int NAME_COL_MIN_WIDTH              = 96;
    public static final int AMOUNT_COL_MIN_WIDTH            = 96;
    public static final int TABLE_AMOUNT_TILEPANE_MIN_WIDTH = 140;
    public static final int TABLE_AMOUNT_BUTTONS_SIZE       = BUTTON_ICONS_SIZE + 8;

    @FXML
    protected TableView<ItemPack> itemPackTable;
    @FXML
    protected TableColumn<ItemPack, byte[]>     iconColumn;
    @FXML
    protected TableColumn<ItemPack, String>     nameColumn;
    @FXML
    protected TableColumn<ItemPack, Number>     levelColumn;
    @FXML
    protected TableColumn<ItemPack, Type>       typeColumn;
    @FXML
    protected TableColumn<ItemPack, Rarity>     rarityColumn;
    @FXML
    protected TableColumn<ItemPack, ItemPack>   amountColumn;

    protected Map<Integer, Item>    itemMap;
    protected Map<Integer, Set>     setMap;

    protected ItemPackTableSceneController() { }

    @FXML
    protected void initialize() {
        //refresh late for scrollbar adjustment
        lateRefreshTable();

        setTableCellValueFactories();

        setTableCellFactories();

        setTableColumnComparators();
    }

    protected void populateInitialTable() { }

    protected abstract void onAmountInsertPlusButtonClicked(ItemPack itemPack);

    protected abstract void onAmountUpdatePlusButtonClicked(ItemPack itemPack);

    protected abstract void onAmountUpdateMinusButtonClicked(ItemPack itemPack);

    @Override
    protected void setTableCellValueFactories() {
        iconColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().iconBytesProperty());

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().nameProperty());

        levelColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().levelProperty());

        typeColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().typeProperty());

        rarityColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().rarityProperty());

        amountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    }

    @Override
    protected void setTableCellFactories() {
        iconColumn.setCellFactory(cfData -> new TableCell<ItemPack, byte[]>() {
            private final VBox imageViewVBox;
            private final ImageView imageView;

            {
                imageViewVBox = new VBox();
                imageViewVBox.setAlignment(Pos.CENTER);

                imageView = new ImageView();
                imageView.setFitWidth(ICON_SIZE);
                imageView.setFitHeight(ICON_SIZE);
                imageView.setPreserveRatio(true);
                imageView.setCache(true);

                imageViewVBox.getChildren().add(imageView);
            }

            @Override
            protected void updateItem(byte[] item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    if (item != null)
                        imageView.setImage(new Image(new ByteArrayInputStream(item)));
                    else
                        imageView.setImage(ResourceLoader.NO_PICTURE);
                }

                //setGraphic to imageViewVBox regardless to set the min row size
                setGraphic(imageViewVBox);
            }
        });

        levelColumn.setCellFactory(cfData -> new TableCell<ItemPack, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty)
                    setText(item.intValue() == Level.UNDEFINED.intValue() ? Level.UNDEFINED.toString() : item.toString());
            }
        });

        amountColumn.setCellFactory(cfData -> new TableCell<ItemPack, ItemPack>() {
            private final ImageView plusImageViewInsert;
            private final ImageView plusImageView;
            private final ImageView minusImageView;
            private final Button    plusButtonInsert;
            private final Button    plusButton;
            private final Button    minusButton;
            private final Label     amountLabel;
            private final TilePane  tilePane;

            {
                plusImageViewInsert = new ImageView();
                plusImageViewInsert.setFitWidth(BUTTON_ICONS_SIZE);
                plusImageViewInsert.setPreserveRatio(true);

                plusImageView = new ImageView();
                plusImageView.setFitWidth(BUTTON_ICONS_SIZE);
                plusImageView.setPreserveRatio(true);

                minusImageView = new ImageView();
                minusImageView.setFitWidth(BUTTON_ICONS_SIZE);
                minusImageView.setPreserveRatio(true);

                plusButtonInsert = new Button();
                plusButtonInsert.setMinSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                plusButtonInsert.setMaxSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                plusButtonInsert.setGraphic(plusImageViewInsert);

                plusButton = new Button();
                plusButton.setMinSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                plusButton.setMaxSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                plusButton.setGraphic(plusImageView);

                minusButton = new Button();
                minusButton.setMinSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                minusButton.setMaxSize(TABLE_AMOUNT_BUTTONS_SIZE, TABLE_AMOUNT_BUTTONS_SIZE);
                minusButton.setGraphic(minusImageView);

                amountLabel = new Label();
                amountLabel.setAlignment(Pos.CENTER);

                tilePane = new TilePane();
                tilePane.setPrefTileHeight(0);
                tilePane.setMinWidth(TABLE_AMOUNT_TILEPANE_MIN_WIDTH);
                tilePane.setAlignment(Pos.CENTER);
                tilePane.getChildren().addAll(plusButton, amountLabel, minusButton);
            }

            @Override
            protected void updateItem(ItemPack item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty) {
                    if (item.getAmount() < Amount.MINIMUM.intValue()) {
                        plusImageViewInsert.setImage(ResourceLoader.PLUS_ICON);

                        plusButtonInsert.setOnAction(event -> onAmountInsertPlusButtonClicked(item));

                        setGraphic(plusButtonInsert);
                    } else {
                        plusImageView.setImage(ResourceLoader.PLUS_ICON);

                        plusButton.setOnAction(event -> onAmountUpdatePlusButtonClicked(item));

                        minusImageView.setImage(ResourceLoader.MINUS_ICON);

                        minusButton.setOnAction(event -> onAmountUpdateMinusButtonClicked(item));

                        amountLabel.setText(String.valueOf(item.getAmount()));

                        setGraphic(tilePane);
                    }
                }
            }
        });
    }

    @Override
    protected void setTableColumnComparators() {
        typeColumn.setComparator(Comparator.comparingInt(Type::intValue));

        rarityColumn.setComparator(Comparator.comparingInt(Rarity::intValue));

        amountColumn.setComparator(Comparator.comparingInt(ItemPack::getAmount));
    }

    public void bindMapData(Map<Integer, Item> itemMap, Map<Integer, Set> setMap) {
        this.itemMap = itemMap;
        this.setMap = setMap;
    }

    public Map<Integer, Item> getItemMap() {
        return itemMap;
    }

    public Map<Integer, Set> getSetMap() {
        return setMap;
    }

    @Override
    public void lateRefreshTable() {
        Platform.runLater(itemPackTable::refresh);
    }
}
