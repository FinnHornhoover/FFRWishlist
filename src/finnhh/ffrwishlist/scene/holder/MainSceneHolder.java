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

package finnhh.ffrwishlist.scene.holder;

import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Profile;
import finnhh.ffrwishlist.scene.controller.MainSceneController;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;

public class MainSceneHolder extends ControlledSceneHolder {
    private ObservableList<ItemPack> itemPackList;
    private ObservableList<Profile> profileList;

    public MainSceneHolder(Parent root, int sceneWidth, int sceneHeight, String sceneCSS) {
        super(root, sceneWidth, sceneHeight, sceneCSS);

        scene.widthProperty().addListener((observable, oldValue, newValue) ->
                ((MainSceneController) sceneController).lateRefreshTable());
        scene.heightProperty().addListener((observable, oldValue, newValue) ->
                ((MainSceneController) sceneController).lateRefreshTable());

        itemPackList = FXCollections.observableArrayList();
        profileList = FXCollections.observableArrayList();
    }

    @Override
    protected void atFirstStageAccess() {
        ((MainSceneController) sceneController).profileChoiceBoxSetup();
        ((MainSceneController) sceneController).populateInitialTable();
    }

    public ObservableList<ItemPack> getItemPackList() {
        return itemPackList;
    }

    public ObservableList<Profile> getProfileList() {
        return profileList;
    }
}
