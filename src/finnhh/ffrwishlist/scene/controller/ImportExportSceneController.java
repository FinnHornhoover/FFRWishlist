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

import finnhh.ffrwishlist.model.Item;
import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.model.constants.item.Amount;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.model.database.dao.itempack.ItemPackDAO;
import finnhh.ffrwishlist.scene.controller.base.DatabaseConnected;
import finnhh.ffrwishlist.scene.controller.profile.ProfileSceneController;
import finnhh.ffrwishlist.scene.holder.ImportExportSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.*;

public class ImportExportSceneController extends ProfileSceneController implements DatabaseConnected {
    public static final String PARSE_FAIL_MESSAGE       = "There have been errors, please check the import code.";
    public static final String AMOUNT_EXCEEDED_MESSAGE  = "One or more of the specified amounts are invalid.";
    public static final String IMPORT_SUCCESS_MESSAGE   = "Successfully added the items in the list!";

    @FXML
    private Label importProfileLabel;
    @FXML
    private Label importParseFailLabel;
    @FXML
    private Label importSuccessLabel;
    @FXML
    private Label exportProfileLabel;
    @FXML
    private TextArea importBBCodeTextArea;
    @FXML
    private TextArea exportBBCodeTextArea;
    @FXML
    private ListView<ItemPack> importConfirmationListView;

    private ItemPackDAO itemPackDAO;

    private Map<Integer, Item> itemMap;

    private Map<Item, Integer> wishlistItemsMap;

    private boolean wishlistAltered = false;

    public ImportExportSceneController() {
        this.wishlistItemsMap = new HashMap<>();
    }

    private void setWishlistItems() {
        List<ItemPack> wishlistItemPacks = itemPackDAO.defaultQueryItemPacks(activeProfile, itemMap, true);

        wishlistItemsMap.clear();
        wishlistItemPacks.forEach(wip -> wishlistItemsMap.put(wip.getItem(), wip.getAmount()));

        final StringJoiner exportBBCodeJoiner = new StringJoiner("\n");

        exportBBCodeJoiner.add("[list]");
        wishlistItemPacks.forEach(ip -> exportBBCodeJoiner.add(
                "[li]" + ip.getItem().getName() + (ip.getAmount() > 1 ? " (x" + ip.getAmount() + ")" : "") + "[/li]"
        ));
        exportBBCodeJoiner.add("[/list]");

        exportBBCodeTextArea.setText(exportBBCodeJoiner.toString());
    }

    @FXML
    private void onParseItemsButtonClicked() {
        importParseFailLabel.setText("");
        importConfirmationListView.getItems().clear();

        String BBCodeInput = importBBCodeTextArea.getText().trim();
        String[] listValidation = BBCodeInput.split("]", 2);

        if (listValidation.length == 2) {
            if (listValidation[0].startsWith("[list") && listValidation[1].endsWith("[/list]")) {
                String listItemsString =
                        listValidation[1].substring(0, listValidation[1].length() - 7).trim();

                String[] listItems = listItemsString.split("\\[/li]");

                for (int i = 0; i < listItems.length; i++) {
                    listItems[i] = listItems[i].trim();

                    if (listItems[i].startsWith("[li]")) {
                        listItems[i] = listItems[i].substring(4);

                        final String[] nameAmountParts = listItems[i].split("\\(\\s*x");

                        Optional<Item> referencedItem = itemMap.values().stream()
                                .filter(it -> it.getName().equals(nameAmountParts[0].trim()))
                                .findFirst();

                        int desiredAmount = Amount.NONE.intValue();

                        if (nameAmountParts.length == 2) {
                            int parenthesisIndex = nameAmountParts[1].indexOf(')');

                            if (parenthesisIndex != -1) {
                                try {
                                    desiredAmount =
                                            Integer.parseInt(nameAmountParts[1].substring(0, parenthesisIndex).trim());
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        } else if (nameAmountParts.length == 1) {
                            desiredAmount = Amount.MINIMUM.intValue();
                        } else {
                            importParseFailLabel.setText(PARSE_FAIL_MESSAGE);
                        }

                        if (referencedItem.isPresent()) {
                            Item item = referencedItem.get();

                            int totalAmount = desiredAmount;
                            if (wishlistItemsMap.containsKey(item))
                                totalAmount += wishlistItemsMap.get(item);

                            if (Amount.isValidAmount(desiredAmount) && Amount.isValidAmount(totalAmount))
                                importConfirmationListView.getItems().add(new ItemPack(item, desiredAmount));
                            else
                                importParseFailLabel.setText(AMOUNT_EXCEEDED_MESSAGE);
                        } else {
                            importParseFailLabel.setText(PARSE_FAIL_MESSAGE);
                        }

                    } else {
                        importParseFailLabel.setText(PARSE_FAIL_MESSAGE);
                    }
                }
            }
        } else {
            importParseFailLabel.setText(PARSE_FAIL_MESSAGE);
        }

        importConfirmationListView.refresh();
    }

    @FXML
    private void onAppendToWishlistButtonClicked() {
        importSuccessLabel.setText("");

        ObservableList<ItemPack> parsedItemPacks = importConfirmationListView.getItems();

        if (!parsedItemPacks.isEmpty()) {
            final List<ItemPack> insertItemPackList =
                    parsedItemPacks.filtered(ip -> !wishlistItemsMap.containsKey(ip.getItem()));
            final List<ItemPack> updateItemPackList =
                    parsedItemPacks.filtered(ip -> wishlistItemsMap.containsKey(ip.getItem()));

            updateItemPackList.forEach(uip -> uip.setAmount(wishlistItemsMap.get(uip.getItem()) + uip.getAmount()));

            if (!insertItemPackList.isEmpty() || !updateItemPackList.isEmpty())
                wishlistAltered = true;

            Task<Void> batchInsertUpdateTask = new Task<Void>() {
                @Override
                protected Void call() {
                    itemPackDAO.batchInsertAmount(activeProfile, insertItemPackList);
                    itemPackDAO.batchUpdateAmount(activeProfile, updateItemPackList);
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();

                    setWishlistItems();

                    Platform.runLater(() -> {
                        importSuccessLabel.setText(IMPORT_SUCCESS_MESSAGE);
                        importConfirmationListView.getItems().clear();
                        importConfirmationListView.refresh();
                    });
                }
            };

            new Thread(batchInsertUpdateTask).start();
        }
    }

    public boolean alteredWishlist() {
        return wishlistAltered;
    }

    public void bindMapData(Map<Integer, Item> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public void bindHolderData(ControlledSceneHolder sceneHolder) {
        importConfirmationListView.setItems(((ImportExportSceneHolder) sceneHolder).getImportItemPackList());
    }

    @Override
    public void setAsActiveProfile(Profile activeProfile) {
        super.setAsActiveProfile(activeProfile);

        String activeProfileString = activeProfile.toString();

        importProfileLabel.setText(activeProfileString);
        exportProfileLabel.setText(activeProfileString);
    }

    @Override
    public void setDatabaseConnections(DatabaseManager databaseManager) {
        itemPackDAO = databaseManager.getItemPackDAO();

        setWishlistItems();
    }
}
