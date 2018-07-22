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
import finnhh.ffrwishlist.model.constants.stage.StageInfo;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.ItemPackDAO;
import finnhh.ffrwishlist.model.database.dao.SetDAO;
import finnhh.ffrwishlist.model.event.ModelEvent;
import finnhh.ffrwishlist.scene.component.tableview.ItemPackTable;
import finnhh.ffrwishlist.scene.controller.base.AppConnectedSceneController;
import finnhh.ffrwishlist.scene.controller.base.connections.DatabaseConnected;
import finnhh.ffrwishlist.scene.controller.base.connections.WishlistConnected;
import finnhh.ffrwishlist.scene.controller.base.ownership.ItemMapOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.ProfileOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.SetMapOwner;
import finnhh.ffrwishlist.scene.controller.base.ownership.TableOwner;
import finnhh.ffrwishlist.scene.holder.SetMenuSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SetMenuSceneController extends AppConnectedSceneController implements DatabaseConnected, WishlistConnected,
                                                                                    ProfileOwner, ItemMapOwner,
                                                                                    SetMapOwner, TableOwner {
    @FXML
    private TextField setSearchBar;
    @FXML
    private ComboBox<Set> setSelectComboBox;
    @FXML
    private Label profileLabel;
    @FXML
    private ItemPackTable itemPackTable;

    private ItemPackDAO itemPackDAO;
    private SetDAO setDAO;

    private Profile activeProfile;

    private Map<Integer, Item> itemMap;
    private Map<Integer, Set> setMap;

    public SetMenuSceneController() { }

    @FXML
    private void onSetSearchBarQueryEntered() {
        itemPackTable.getItems().clear();
        itemPackTable.refresh();

        setSelectComboBox.getSelectionModel().clearSelection();
        setSelectComboBox.getItems().clear();
        setSelectComboBox.getItems().addAll(setDAO.querySets(setMap, setSearchBar.getText()));
        setSelectComboBox.show();
    }

    @FXML
    private void onSetSelected() {
        itemPackTable.getItems().clear();

        Optional<Set> activeSet = Optional.ofNullable(setSelectComboBox.getSelectionModel().getSelectedItem());

        activeSet.ifPresent(set ->
                itemPackTable.getItems().addAll(itemPackDAO.queryItemPacksBySet(activeProfile, itemMap, set)));

        lateRefreshTable();
    }

    @FXML
    private void onItemPackAdd(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();

        itemPack.setAmount(Amount.MINIMUM.intValue());

        itemPackDAO.insertAmount(activeProfile, itemPack);

        itemPackTable.refresh();

        invalidateWishlist();
    }

    @FXML
    private void onItemPackIncreaseAmount(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();
        int currentAmount = itemPack.getAmount();

        if (currentAmount < Amount.MAXIMUM.intValue()) {
            int nextValue = currentAmount + 1;

            itemPack.setAmount(nextValue);

            itemPackDAO.updateAmount(activeProfile, itemPack, nextValue);

            itemPackTable.refresh();

            invalidateWishlist();
        }
    }

    @FXML
    private void onItemPackDecreaseAmount(ModelEvent<ItemPack> itemPackEvent) {
        ItemPack itemPack = itemPackEvent.getModel();
        int currentAmount = itemPack.getAmount();

        if (currentAmount > Amount.MINIMUM.intValue()) {
            int nextValue = currentAmount - 1;

            itemPack.setAmount(nextValue);

            itemPackDAO.updateAmount(activeProfile, itemPack, nextValue);

            itemPackTable.refresh();

            invalidateWishlist();
        } else if (currentAmount == Amount.MINIMUM.intValue()) {
            itemPack.setAmount(Amount.NONE.intValue());

            itemPackDAO.deleteAmount(activeProfile, itemPack);

            itemPackTable.refresh();

            invalidateWishlist();
        }
    }

    @FXML
    private void onAddAllToWishlistButtonClicked() {
        final List<ItemPack> noneItemPacks =
                itemPackTable.getItems().filtered(ip -> ip.getAmount() == Amount.NONE.intValue());

        noneItemPacks.forEach(nip -> nip.setAmount(Amount.MINIMUM.intValue()));

        Task<Void> batchInsertTask = new Task<Void>() {
            @Override
            protected Void call() {
                itemPackDAO.batchInsertAmount(activeProfile, noneItemPacks);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                lateRefreshTable();
                invalidateWishlist();
            }
        };

        new Thread(batchInsertTask).start();
    }

    @FXML
    private void onIncrementAllItemAmountsButtonClicked() {
        final List<ItemPack> insertItemPacks =
                itemPackTable.getItems().filtered(ip -> ip.getAmount() == Amount.NONE.intValue());
        final List<ItemPack> updateItemPacks =
                itemPackTable.getItems().filtered(ip -> ip.getAmount() >= Amount.MINIMUM.intValue()
                                && ip.getAmount() < Amount.MAXIMUM.intValue());

        insertItemPacks.forEach(iip -> iip.setAmount(Amount.MINIMUM.intValue()));
        updateItemPacks.forEach(uip -> uip.setAmount(uip.getAmount() + 1));

        Task<Void> batchInsertUpdateTask = new Task<Void>() {
            @Override
            protected Void call() {
                itemPackDAO.batchInsertAmount(activeProfile, insertItemPacks);
                itemPackDAO.batchUpdateAmount(activeProfile, updateItemPacks);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                lateRefreshTable();
                invalidateWishlist();
            }
        };

        new Thread(batchInsertUpdateTask).start();
    }

    public void selectSet(boolean setSpecified, Set selectedSet) {
        if (setSpecified) {
            setSelectComboBox.getSelectionModel().select(selectedSet);
            onSetSelected();
        }
    }

    @Override
    public void bindHolderData(ControlledSceneHolder sceneHolder) {
        itemPackTable.setItems(((SetMenuSceneHolder) sceneHolder).getItemPackList());
        setSelectComboBox.setItems(((SetMenuSceneHolder) sceneHolder).getSetList());
    }

    @Override
    public void setDatabaseConnections(DatabaseManager databaseManager) {
        itemPackDAO = databaseManager.getItemPackDAO();
        setDAO = databaseManager.getSetDAO();

        setSelectComboBox.getItems().addAll(setDAO.defaultQuerySets(setMap));
    }

    @Override
    public void invalidateWishlist() {
        ((MainApp) application).wishlistInvalidatedBy(StageInfo.StageState.SET_MENU);
    }

    @Override
    public void wishlistInvalidated() {
        onSetSelected();
    }

    @Override
    public Profile getActiveProfile() {
        return activeProfile;
    }

    @Override
    public void setAsActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;

        profileLabel.setText(activeProfile.toString());
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
