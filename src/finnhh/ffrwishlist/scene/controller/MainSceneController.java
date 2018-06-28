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
import finnhh.ffrwishlist.model.Item;
import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.ItemDAO;
import finnhh.ffrwishlist.model.database.dao.ItemPackDAO;
import finnhh.ffrwishlist.model.database.dao.ProfileDAO;
import finnhh.ffrwishlist.model.database.dao.SetDAO;
import finnhh.ffrwishlist.model.event.ModelEvent;
import finnhh.ffrwishlist.model.parser.ParsedQueryInformation;
import finnhh.ffrwishlist.model.parser.QueryParser;
import finnhh.ffrwishlist.resources.ResourceHolder;
import finnhh.ffrwishlist.scene.component.tablecolumn.AmountColumn;
import finnhh.ffrwishlist.scene.component.tableview.ItemPackTable;
import finnhh.ffrwishlist.scene.component.textfield.AutoCompleteItemSearchBar;
import finnhh.ffrwishlist.scene.controller.base.AppConnectedSceneController;
import finnhh.ffrwishlist.scene.controller.base.connections.DatabaseConnected;
import finnhh.ffrwishlist.scene.controller.base.ownership.ItemMapOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.ProfileOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.SetMapOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.TableOwner;
import finnhh.ffrwishlist.scene.holder.MainSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.Map;

public class MainSceneController extends AppConnectedSceneController implements DatabaseConnected, ProfileOwner,
                                                                                ItemMapOwner, SetMapOwner,
                                                                                TableOwner {
    public static final String  TOP_TEXT_WISHLIST_TRUE          = "List of items in your wishlist:";
    public static final String  TOP_TEXT_WISHLIST_FALSE         = "List of items you can add to your wishlist:";
    public static final String  TOP_BUTTON_TEXT_WISHLIST_TRUE   = "Add Items";
    public static final String  TOP_BUTTON_TEXT_WISHLIST_FALSE  = "Done";

    @FXML
    private Button infoIconButton;
    @FXML
    private AutoCompleteItemSearchBar searchBar;
    @FXML
    private ComboBox<Profile> profileComboBox;
    @FXML
    private Label topInfoText;
    @FXML
    private Button topAddButton;
    @FXML
    private ImageView topButtonImageView;
    @FXML
    private ItemPackTable itemPackTable;
    @FXML
    private AmountColumn amountColumn;
    @FXML
    private Label messageBarSearchText;
    @FXML
    private Label messageBarItemCountsText;
    @FXML
    private Label messageBarErrorText;

    private ProfileDAO profileDAO;
    private ItemPackDAO itemPackDAO;

    private Profile activeProfile;

    private Map<Integer, Item> itemMap;
    private Map<Integer, Set> setMap;

    private QueryParser queryParser;

    private boolean wishlistMode = true;

    public MainSceneController() {
        queryParser = new QueryParser();
    }

    private void setMessageBarSearchText(String nameSearched) {
        messageBarSearchText.setText(nameSearched.isEmpty() ? "" : "Searching for \"" + nameSearched + "\"");
    }

    private void setMessageBarItemCountsText() {
        ObservableList<ItemPack> itemPackList = itemPackTable.getItems();
        int distinctItems = itemPackList.size();

        if (wishlistMode) {
            messageBarItemCountsText.setText(
                    String.format("Showing %d distinct item%s (Total = %d)",
                            distinctItems,
                            (distinctItems == 1) ? "" : "s",
                            itemPackList.stream().mapToInt(ItemPack::getAmount).sum())
            );
        } else {
            messageBarItemCountsText.setText(
                    String.format("Showing %d item%s",
                            distinctItems,
                            (distinctItems == 1) ? "" : "s")
            );
        }
    }

    private void setMessageBarErrorText(String errorText) {
        messageBarErrorText.setText(errorText);
    }

    @FXML
    private void onSearchBarQueryEntered() {
        ParsedQueryInformation queryInformation = queryParser.parse(searchBar.getText(), wishlistMode);

        itemPackTable.getSortOrder().clear();

        itemPackTable.getItems().clear();
        itemPackTable.getItems().addAll(itemPackDAO.queryItemPacks(activeProfile, itemMap, queryInformation));
        itemPackTable.refresh();
        setMessageBarSearchText(queryInformation.getSearchString());
        setMessageBarItemCountsText();
        setMessageBarErrorText(queryInformation.getErrorString());
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
    private void onItemPackAdd(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();
        itemPack.setAmount(Amount.MINIMUM.intValue());
        itemPackDAO.insertAmount(activeProfile, itemPack);
        itemPackTable.getItems().remove(itemPack);
        itemPackTable.refresh();
        setMessageBarItemCountsText();
    }

    @FXML
    private void onItemPackIncreaseAmount(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();
        int currentAmount = itemPack.getAmount();

        if (currentAmount < Amount.MAXIMUM.intValue()) {
            itemPack.setAmount(currentAmount + 1);
            itemPackDAO.updateAmount(activeProfile, itemPack, currentAmount + 1);
            itemPackTable.refresh();
            setMessageBarItemCountsText();
        }
    }

    @FXML
    private void onItemPackDecreaseAmount(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();
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

    @FXML
    private void onSetSelectedFromTable(ModelEvent<Set> setEvent) {
        ((MainApp) application).startSetMenuStage(this, true, setEvent.getModel());
    }

    @FXML
    private void onInformationButtonClicked() {
        ((MainApp) application).startInformationStage();
    }

    @FXML
    private void onSetMenuButtonClicked() {
        ((MainApp) application).startSetMenuStage(this);
    }

    @FXML
    private void onImportExportButtonClicked() {
        ((MainApp) application).startImportExportStage(this);
    }

    @FXML
    private void onHelpButtonClicked() {
        ((MainApp) application).startHelpStage();
    }

    public void showInfoIconButton() {
        infoIconButton.setVisible(true);
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
        wishlistMode = desiredMode;

        if (wishlistMode) {
            topInfoText.setText(TOP_TEXT_WISHLIST_TRUE);

            topAddButton.setText(TOP_BUTTON_TEXT_WISHLIST_TRUE);
            topButtonImageView.setImage(ResourceHolder.PLUS_ICON);
        } else {
            topInfoText.setText(TOP_TEXT_WISHLIST_FALSE);

            topAddButton.setText(TOP_BUTTON_TEXT_WISHLIST_FALSE);
            topButtonImageView.setImage(ResourceHolder.DONE_ICON);
        }

        itemPackTable.getSortOrder().clear();

        amountColumn.setSortable(wishlistMode);

        ParsedQueryInformation queryInformation = queryParser.parse("", wishlistMode);

        itemPackTable.getItems().clear();
        itemPackTable.getItems().addAll(itemPackDAO.queryItemPacks(activeProfile, itemMap, queryInformation));
        lateRefreshTable();

        setMessageBarSearchText(queryInformation.getSearchString());
        setMessageBarItemCountsText();
        setMessageBarErrorText(queryInformation.getErrorString());
    }

    public void populateInitialTable() {
        itemPackTable.getItems().addAll(itemPackDAO.queryItemPacks(activeProfile, itemMap, queryParser.parse("", wishlistMode)));
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

        setItemMap(itemDAO.getAllItemsMap());
        setSetMap(setDAO.getAllSetsMap());

        itemDAO.makeItemSetAssociations(itemMap, setMap);
        setDAO.makeSetItemAssociations(setMap, itemMap);
    }

    @Override
    public Profile getActiveProfile() {
        return activeProfile;
    }

    @Override
    public void setAsActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }

    @Override
    public Map<Integer, Item> getItemMap() {
        return itemMap;
    }

    @Override
    public void setItemMap(Map<Integer, Item> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public Map<Integer, Set> getSetMap() {
        return setMap;
    }

    @Override
    public void setSetMap(Map<Integer, Set> setMap) {
        this.setMap = setMap;
    }

    @Override
    public void lateRefreshTable() {
        Platform.runLater(itemPackTable::refresh);
    }
}
