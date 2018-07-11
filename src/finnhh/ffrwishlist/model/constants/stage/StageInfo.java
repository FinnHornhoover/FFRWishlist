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

package finnhh.ffrwishlist.model.constants.stage;

import finnhh.ffrwishlist.MainApp;
import finnhh.ffrwishlist.scene.holder.ImportExportSceneHolder;
import finnhh.ffrwishlist.scene.holder.MainSceneHolder;
import finnhh.ffrwishlist.scene.holder.SetMenuSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.SceneHolder;

public final class StageInfo {
    public static final String  ALL_STAGES_CSS              = "application.css";

    public static final int     MAIN_STAGE_WIDTH            = 1050;
    public static final int     MAIN_STAGE_HEIGHT           = 600;
    public static final String  MAIN_STAGE_TITLE            = "FFR Wishlist v" + MainApp.VERSION;
    public static final String  MAIN_STAGE_FXML             = "MainView.fxml";

    public static final int     UPDATE_STAGE_WIDTH          = 300;
    public static final int     UPDATE_STAGE_HEIGHT         = 200;
    public static final String  UPDATE_STAGE_TITLE          = "Updater";
    public static final String  UPDATE_STAGE_FXML           = "UpdateView.fxml";

    public static final int     IMPORT_EXPORT_STAGE_WIDTH   = 600;
    public static final int     IMPORT_EXPORT_STAGE_HEIGHT  = 600;
    public static final String  IMPORT_EXPORT_STAGE_TITLE   = "Import / Export Wishlist";
    public static final String  IMPORT_EXPORT_STAGE_FXML    = "ImportExportView.fxml";

    public static final int     HELP_STAGE_WIDTH            = 900;
    public static final int     HELP_STAGE_HEIGHT           = 600;
    public static final String  HELP_STAGE_TITLE            = "Help";
    public static final String  HELP_STAGE_FXML             = "HelpView.fxml";

    public static final int     SET_MENU_STAGE_WIDTH        = 800;
    public static final int     SET_MENU_STAGE_HEIGHT       = 600;
    public static final String  SET_MENU_STAGE_TITLE        = "Set Menu";
    public static final String  SET_MENU_STAGE_FXML         = "SetMenuView.fxml";

    public static final int     INFORMATION_STAGE_WIDTH     = 300;
    public static final int     INFORMATION_STAGE_HEIGHT    = 200;
    public static final String  INFORMATION_STAGE_TITLE     = "Information";
    public static final String  INFORMATION_STAGE_FXML      = "InformationView.fxml";

    //private constructor to disable instantiations
    private StageInfo() { }

    public enum StageState {
        MAIN(MAIN_STAGE_WIDTH, MAIN_STAGE_HEIGHT, MAIN_STAGE_TITLE,
                MAIN_STAGE_FXML, true, MainSceneHolder.class),
        UPDATE(UPDATE_STAGE_WIDTH, UPDATE_STAGE_HEIGHT, UPDATE_STAGE_TITLE,
                UPDATE_STAGE_FXML, false, ControlledSceneHolder.class),
        IMPORT_EXPORT(IMPORT_EXPORT_STAGE_WIDTH, IMPORT_EXPORT_STAGE_HEIGHT, IMPORT_EXPORT_STAGE_TITLE,
                IMPORT_EXPORT_STAGE_FXML, false, ImportExportSceneHolder.class),
        HELP(HELP_STAGE_WIDTH, HELP_STAGE_HEIGHT, HELP_STAGE_TITLE,
                HELP_STAGE_FXML, false, SceneHolder.class),
        SET_MENU(SET_MENU_STAGE_WIDTH, SET_MENU_STAGE_HEIGHT, SET_MENU_STAGE_TITLE,
                SET_MENU_STAGE_FXML, true, SetMenuSceneHolder.class),
        INFORMATION(INFORMATION_STAGE_WIDTH, INFORMATION_STAGE_HEIGHT, INFORMATION_STAGE_TITLE,
                INFORMATION_STAGE_FXML, false, ControlledSceneHolder.class);

        private final int stageWidth;
        private final int stageHeight;
        private final String title;
        private final String FXMLName;
        private final String CSSName;
        private final boolean resizable;
        private final Class<? extends SceneHolder> sceneHolderClass;

        private boolean screenLock;

        StageState(int stageWidth, int stageHeight, String title, String FXMLName, boolean resizable,
                   Class<? extends SceneHolder> sceneHolderClass) {
            this.stageWidth = stageWidth;
            this.stageHeight = stageHeight;
            this.title = title;
            this.FXMLName = FXMLName;
            this.CSSName = ALL_STAGES_CSS;
            this.resizable = resizable;
            this.sceneHolderClass = sceneHolderClass;
            this.screenLock = false;
        }

        public int getStageWidth() {
            return stageWidth;
        }

        public int getStageHeight() {
            return stageHeight;
        }

        public String getTitle() {
            return title;
        }

        public String getFXMLName() {
            return FXMLName;
        }

        public String getCSSName() {
            return CSSName;
        }

        public boolean isResizable() {
            return resizable;
        }

        public Class<? extends SceneHolder> getSceneHolderClass() {
            return sceneHolderClass;
        }

        public boolean hasScreenLock() {
            return screenLock;
        }

        public void setScreenLock(boolean screenLock) {
            this.screenLock = screenLock;
        }
    }
}
