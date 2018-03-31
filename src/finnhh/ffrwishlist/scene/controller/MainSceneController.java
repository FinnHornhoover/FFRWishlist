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

package finnhh.ffrwishlist.scene.controller;

import finnhh.ffrwishlist.MainApp;
import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.ItemDAO;
import finnhh.ffrwishlist.model.database.dao.ProfileDAO;
import finnhh.ffrwishlist.model.database.dao.SetDAO;
import finnhh.ffrwishlist.model.database.dao.itempack.ItemPackDAO;
import finnhh.ffrwishlist.resources.ResourceLoader;
import finnhh.ffrwishlist.scene.controller.base.DatabaseConnected;
import finnhh.ffrwishlist.scene.controller.table.ItemPackTableSceneController;
import finnhh.ffrwishlist.scene.holder.MainSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainSceneController extends ItemPackTableSceneController implements DatabaseConnected {
    public static final int     BUTTON_ICONS_SIZE               = 32;
    public static final int     SET_ROW_MIN_WIDTH               = 132;
    public static final int     TABLE_SETS_VBOX_SPACING         = 2;
    public static final String  TABLE_SETS_LABELS_CSS_CLASS     = "setreference";
    public static final String  TOP_TEXT_WISHLIST_TRUE          = "List of items in your wishlist:";
    public static final String  TOP_TEXT_WISHLIST_FALSE         = "List of items you can add to your wishlist:";
    public static final String  TOP_BUTTON_TEXT_WISHLIST_TRUE   = "Add Items";
    public static final String  TOP_BUTTON_TEXT_WISHLIST_FALSE  = "Done";

    private static final String PARAMETER_SPLITTER = "--";

    @FXML
    private TextField searchBar;
    @FXML
    private ComboBox<Profile> profileComboBox;
    @FXML
    private Label topInfoText;
    @FXML
    private Button topAddButton;
    @FXML
    private ImageView topButtonImageView;
    @FXML
    private TableColumn<ItemPack, ObservableList<Set>> setsColumn;
    @FXML
    private Label messageBarSearchText;
    @FXML
    private Label messageBarItemCountsText;
    @FXML
    private Label messageBarErrorText;

    private boolean wishlistMode = true;

    private ProfileDAO  profileDAO;
    private ItemPackDAO itemPackDAO;

    public MainSceneController() { }

    @FXML
    private void onSearchBarQueryEntered() {
        //[0]: always the name, the rest is interpreted as args
        String[] allParams = searchBar.getText().split(PARAMETER_SPLITTER);

        String namePart = allParams[0].trim();
        String[] filterArgs = new String[allParams.length - 1];

        for (int i = 0; i < filterArgs.length; i++)
            filterArgs[i] = allParams[i + 1].trim();

        itemPackTable.getSortOrder().clear();

        itemPackTable.getItems().clear();
        itemPackTable.getItems().addAll(
                itemPackDAO.queryItemPacks(activeProfile, itemMap, wishlistMode, namePart, filterArgs)
        );
        itemPackTable.refresh();
        setMessageBarSearchText(namePart);
        setMessageBarItemCountsText();
        setMessageBarErrorText();
    }

    @FXML
    private void onProfileChanged() {
        Profile newActiveProfile = profileComboBox.getSelectionModel().getSelectedItem();

        profileComboBox.getItems().forEach(p -> p.setActive(false));
        newActiveProfile.setActive(true);

        profileDAO.clearAllActiveProfileStates();
        profileDAO.activateProfile(newActiveProfile);

        setAsActiveProfile(newActiveProfile);

        switchToWishlistMode(true);
    }

    @FXML
    private void onTopAddButtonClicked() {
        switchToWishlistMode(!wishlistMode);
    }

    @FXML
    private void onImportExportButtonClicked() {
        ((MainApp) application).startImportExportStage(this);
    }

    @FXML
    private void onHelpButtonClicked() {
        ((MainApp) application).startHelpStage();
    }

    @FXML
    private void onSetMenuButtonClicked() {
        ((MainApp) application).startSetMenuStage(this);
    }

    private void setMessageBarSearchText(String nameSearched) {
        messageBarSearchText.setText(nameSearched.equals("") ? "" : "Searching for \"" + nameSearched + "\"");
    }

    private void setMessageBarItemCountsText() {
        ObservableList<ItemPack> itemPackList = itemPackTable.getItems();
        int distinctItems = itemPackList.size();

        if (wishlistMode) {
            messageBarItemCountsText.setText(String.format("Showing %d distinct item%s (Total = %d)",
                    distinctItems,
                    (distinctItems == 1) ? "" : "s",
                    itemPackList.stream().mapToInt(ItemPack::getAmount).sum()));
        } else {
            messageBarItemCountsText.setText(String.format("Showing %d item%s",
                    distinctItems,
                    (distinctItems == 1) ? "" : "s"));
        }
    }

    private void setMessageBarErrorText() {
        messageBarErrorText.setText(itemPackDAO.getQueryErrorString());
    }

    @Override
    protected void setTableCellValueFactories() {
        super.setTableCellValueFactories();

        setsColumn.setCellValueFactory(cellData -> cellData.getValue().getItem().setsAssociatedProperty());
    }

    @Override
    protected void setTableCellFactories() {
        super.setTableCellFactories();

        setsColumn.setCellFactory(cfData -> new TableCell<ItemPack, ObservableList<Set>>() {
            private final VBox labelsBox;

            {
                labelsBox = new VBox(TABLE_SETS_VBOX_SPACING);
                labelsBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(ObservableList<Set> item, boolean empty) {
                super.updateItem(item, empty);

                labelsBox.getChildren().clear();

                if (!empty) {
                    if (!item.isEmpty()) {
                        item.forEach(s -> {
                            final Label setLabel = new Label(s.getSetName());
                            setLabel.getStyleClass().add(TABLE_SETS_LABELS_CSS_CLASS);
                            setLabel.setOnMouseClicked(event ->
                                    ((MainApp) application).startSetMenuStage(MainSceneController.this, true, s));
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

    @Override
    protected void onAmountInsertPlusButtonClicked(ItemPack itemPack) {
        itemPack.setAmount(Amount.MINIMUM.intValue());
        itemPackDAO.insertAmount(activeProfile, itemPack);
        itemPackTable.getItems().remove(itemPack);
        itemPackTable.refresh();
        setMessageBarItemCountsText();
    }

    @Override
    protected void onAmountUpdatePlusButtonClicked(ItemPack itemPack) {
        int currentAmount = itemPack.getAmount();

        if (currentAmount < Amount.MAXIMUM.intValue()) {
            itemPack.setAmount(currentAmount + 1);
            itemPackDAO.updateAmount(activeProfile, itemPack, currentAmount + 1);
            itemPackTable.refresh();
            setMessageBarItemCountsText();
        }
    }

    @Override
    protected void onAmountUpdateMinusButtonClicked(ItemPack itemPack) {
        int currentAmount = itemPack.getAmount();

        if (currentAmount > Amount.MINIMUM.intValue()) {
            itemPack.setAmount(currentAmount - 1);
            itemPackDAO.updateAmount(activeProfile, itemPack, currentAmount - 1);
            itemPackTable.refresh();
            setMessageBarItemCountsText();
        } else if (currentAmount == Amount.MINIMUM.intValue()) {
            itemPackDAO.deleteAmount(activeProfile, itemPack);
            itemPackTable.getItems().remove(itemPack);
            itemPackTable.refresh();
            setMessageBarItemCountsText();
        }
    }

    public void profileChoiceBoxSetup() {
        ObservableList<Profile> profileList = profileComboBox.getItems();

        profileList.addAll(profileDAO.getAllProfiles());

        profileComboBox.getSelectionModel().select(profileList.stream()
                                                                .filter(Profile::isActive)
                                                                .findFirst()
                                                                .orElse(profileList.get(0)));

        Profile selectedProfile = profileComboBox.getSelectionModel().getSelectedItem();

        if (!selectedProfile.isActive()) {
            selectedProfile.setActive(true);
            profileDAO.activateProfile(selectedProfile);
        }

        setAsActiveProfile(selectedProfile);
    }

    public void switchToWishlistMode(boolean desiredMode) {
        if (desiredMode) {
            wishlistMode = true;

            topInfoText.setText(TOP_TEXT_WISHLIST_TRUE);

            topAddButton.setText(TOP_BUTTON_TEXT_WISHLIST_TRUE);
            topButtonImageView.setImage(ResourceLoader.PLUS_ICON);
        } else {
            wishlistMode = false;

            topInfoText.setText(TOP_TEXT_WISHLIST_FALSE);

            topAddButton.setText(TOP_BUTTON_TEXT_WISHLIST_FALSE);
            topButtonImageView.setImage(ResourceLoader.DONE_ICON);
        }

        itemPackTable.getSortOrder().clear();

        amountColumn.setSortable(wishlistMode);

        searchBar.setText("");

        itemPackTable.getItems().clear();
        itemPackTable.getItems().addAll(itemPackDAO.defaultQueryItemPacks(activeProfile, itemMap, wishlistMode));
        lateRefreshTable();

        setMessageBarSearchText("");
        setMessageBarItemCountsText();
        setMessageBarErrorText();
    }

    @Override
    public void populateInitialTable() {
        itemPackTable.getItems().addAll(itemPackDAO.defaultQueryItemPacks(activeProfile, itemMap, wishlistMode));
        itemPackTable.refresh();
        setMessageBarItemCountsText();
    }

    @Override
    public void bindHolderData(ControlledSceneHolder sceneHolder) {
        itemPackTable.setItems(((MainSceneHolder) sceneHolder).getItemPackList());
        profileComboBox.setItems(((MainSceneHolder) sceneHolder).getProfileList());
    }

    @Override
    public void setDatabaseConnections(DatabaseManager databaseManager) {
        ItemDAO itemDAO = databaseManager.getItemDAO();
        SetDAO setDAO   = databaseManager.getSetDAO();
        profileDAO      = databaseManager.getProfileDAO();
        itemPackDAO     = databaseManager.getItemPackDAO();

        bindMapData(itemDAO.getAllItemsMap(), setDAO.getAllSetsMap());

        itemDAO.makeItemSetAssociations(itemMap, setMap);
        setDAO.makeSetItemAssociations(setMap, itemMap);
    }
}
