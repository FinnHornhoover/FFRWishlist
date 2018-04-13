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

package finnhh.ffrwishlist;

import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.constants.stage.StageInfo;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.resources.ResourceLoader;
import finnhh.ffrwishlist.scene.controller.ImportExportSceneController;
import finnhh.ffrwishlist.scene.controller.MainSceneController;
import finnhh.ffrwishlist.scene.controller.SetMenuSceneController;
import finnhh.ffrwishlist.scene.controller.UpdateSceneController;
import finnhh.ffrwishlist.scene.controller.base.SceneController;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import finnhh.ffrwishlist.scene.holder.base.SceneHolder;
import finnhh.ffrwishlist.web.WebUpdater;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {
    public static final String VERSION = "1.1";

    private DatabaseManager appDatabaseManager;
    private WebUpdater appDBUpdater;

    private StageHolder primaryStageHolder;

    private void setSingletons() throws Exception {
        appDatabaseManager = new DatabaseManager();
        appDBUpdater = new WebUpdater();
    }

    private void startUpdateStage() {
        StageHolder stageHolder = new StageHolder(new Stage(), StageInfo.StageState.UPDATE) {
            @Override
            void controllerSetup() {
                UpdateSceneController updateSceneController = (UpdateSceneController) getSceneController();

                updateSceneController.setApp(MainApp.this);
                updateSceneController.setWebConnections(appDBUpdater);
                updateSceneController.setDatabaseConnections(appDatabaseManager);
            }

            @Override
            void onStageExit(WindowEvent event) {
                event.consume();
            }

            @Override
            void showStage() {
                ((UpdateSceneController) getSceneController()).runPopup();
            }
        };

        try {
            stageHolder.startStage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize singleton objects
        setSingletons();

        primaryStageHolder = new StageHolder(primaryStage, StageInfo.StageState.MAIN) {
            @Override
            void controllerSetup() {
                MainSceneController mainSceneController = (MainSceneController) getSceneController();

                mainSceneController.setApp(MainApp.this);
                mainSceneController.setDatabaseConnections(appDatabaseManager);
            }

            @Override
            void onStageExit(WindowEvent event) {
                super.onStageExit(event);

                Platform.exit();
            }

            @Override
            void showStage() throws Exception {
                super.showStage();

                Platform.runLater(MainApp.this::startUpdateStage);
            }
        };

        primaryStageHolder.startStage();
    }

    public void startImportExportStage(final MainSceneController sourceSceneController) {
        StageHolder stageHolder = new StageHolder(new Stage(), StageInfo.StageState.IMPORT_EXPORT) {
            @Override
            void controllerSetup() {
                ImportExportSceneController importExportSceneController = (ImportExportSceneController) getSceneController();

                importExportSceneController.bindMapData(sourceSceneController.getItemMap());
                importExportSceneController.setAsActiveProfile(sourceSceneController.getActiveProfile());
                importExportSceneController.setDatabaseConnections(appDatabaseManager);
            }

            @Override
            void onStageExit(WindowEvent event) {
                super.onStageExit(event);

                if (((ImportExportSceneController) getSceneController()).alteredWishlist())
                    sourceSceneController.switchToWishlistMode(true);
            }
        };

        try {
            stageHolder.startStage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startHelpStage() {
        StageHolder stageHolder = new StageHolder(new Stage(), StageInfo.StageState.HELP);

        try {
            stageHolder.startStage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSetMenuStage(final MainSceneController sourceSceneController) {
        startSetMenuStage(sourceSceneController, false, null);
    }

    public void startSetMenuStage(final MainSceneController sourceSceneController, boolean setSpecified,
                                  Set selectedSet) {
        StageHolder stageHolder = new StageHolder(new Stage(), StageInfo.StageState.SET_MENU) {
            @Override
            void controllerSetup() {
                SetMenuSceneController setMenuSceneController = (SetMenuSceneController) getSceneController();

                setMenuSceneController.bindMapData(sourceSceneController.getItemMap(), sourceSceneController.getSetMap());
                setMenuSceneController.setDatabaseConnections(appDatabaseManager);
                setMenuSceneController.setAsActiveProfile(sourceSceneController.getActiveProfile());
                setMenuSceneController.selectSet(setSpecified, selectedSet);
            }

            @Override
            void onStageExit(WindowEvent event) {
                super.onStageExit(event);

                if (((SetMenuSceneController) getSceneController()).alteredWishlist())
                    sourceSceneController.switchToWishlistMode(true);
            }
        };

        try {
            stageHolder.startStage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToPrimaryStageTitle(String s) {
        Stage primaryStage = primaryStageHolder.getStage();

        primaryStage.setTitle(primaryStage.getTitle() + s);
    }

    private static class StageHolder {
        private final Stage stage;
        private final StageInfo.StageState stageState;
        private SceneHolder sceneHolder;
        private SceneController sceneController;

        StageHolder(Stage stage, StageInfo.StageState stageState) {
            this.stage = stage;
            this.stageState = stageState;
        }

        final void startStage() throws Exception {
            if (!stageState.hasScreenLock()) {
                stageState.setScreenLock(true);

                FXMLLoader fxmlLoader = new FXMLLoader(ResourceLoader.getSceneFXMLResource(stageState.getFXMLName()));
                Parent root = fxmlLoader.load();

                sceneHolder = stageState.getSceneHolderClass()
                        .getConstructor(Parent.class, int.class, int.class, String.class)
                        .newInstance(root,
                                stageState.getStageWidth(),
                                stageState.getStageHeight(),
                                stageState.getCSSName());

                if (sceneHolder instanceof ControlledSceneHolder) {
                    ControlledSceneHolder controlledSceneHolder = (ControlledSceneHolder) sceneHolder;
                    sceneController = fxmlLoader.getController();

                    controlledSceneHolder.setController(sceneController);
                    sceneController.bindHolderData(controlledSceneHolder);

                    controllerSetup();
                }

                stage.setMinWidth(stageState.getStageWidth());
                stage.setMinHeight(stageState.getStageHeight());
                stage.setResizable(stageState.isResizable());
                stage.setTitle(stageState.getTitle());
                stage.getIcons().add(ResourceLoader.PROGRAM_ICON);
                stage.setOnCloseRequest(this::onStageExit);
                stage.setScene(sceneHolder.getScene());

                showStage();
            }
        }

        void controllerSetup() { }

        void onStageExit(WindowEvent event) {
            stageState.setScreenLock(false);
        }

        void showStage() throws Exception {
            stage.show();
        }

        Stage getStage() {
            return stage;
        }

        StageInfo.StageState getStageState() {
            return stageState;
        }

        SceneHolder getSceneHolder() {
            return sceneHolder;
        }

        SceneController getSceneController() {
            return sceneController;
        }
    }
}
