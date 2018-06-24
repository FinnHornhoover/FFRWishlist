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
import finnhh.ffrwishlist.model.constants.stage.StageInfo;
import finnhh.ffrwishlist.model.database.DatabaseManager;
import finnhh.ffrwishlist.scene.controller.base.AppConnectedSceneController;
import finnhh.ffrwishlist.scene.controller.base.database.DatabaseConnected;
import finnhh.ffrwishlist.scene.controller.base.web.WebConnected;
import finnhh.ffrwishlist.scene.holder.base.ControlledSceneHolder;
import finnhh.ffrwishlist.web.WebUpdater;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class UpdateSceneController extends AppConnectedSceneController implements DatabaseConnected, WebConnected {
    private static final int    THREAD_SLEEP_TIME_MILLISECONDS  = 100;
    private static final double DATA_PULL_COMPLETION_PERCENTAGE = 0.5;
    private static final double ALL_COMPLETION_PERCENTAGE       = 1.0;

    @FXML
    private ProgressIndicator progressIndicator;

    private WebUpdater webUpdater;

    private DatabaseManager databaseManager;

    public UpdateSceneController() { }

    private void returnToPrimaryStage() {
        StageInfo.StageState.UPDATE.setScreenLock(false);
        ((MainApp) application).appendToPrimaryStageTitle("." + databaseManager.getDatabaseVersion());
    }

    public void runPopup() {
        final Stage stage = (Stage) progressIndicator.getScene().getWindow();

        Task<Void> fetchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                webUpdater.connectAndFetchData(databaseManager.getDatabaseVersion());
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                int totalUpdates = webUpdater.getUpdatesRemaining();

                if (totalUpdates > 0) {
                    final double updateWorth =
                            (ALL_COMPLETION_PERCENTAGE - DATA_PULL_COMPLETION_PERCENTAGE) / totalUpdates;

                    Task<Void> updateTask = new Task<Void>() {
                        @Override
                        protected Void call() {
                            Platform.runLater(() -> {
                                stage.show();

                                Platform.runLater(() -> {
                                    try {
                                        Thread.sleep(THREAD_SLEEP_TIME_MILLISECONDS);
                                    } catch (InterruptedException ignored) {
                                    } finally {
                                        updateProgress(DATA_PULL_COMPLETION_PERCENTAGE, ALL_COMPLETION_PERCENTAGE);
                                    }
                                });
                            });

                            do {
                                if (isCancelled() && databaseManager.allTablesExist())
                                    break;

                                webUpdater.updateOnce(databaseManager);
                                Platform.runLater(() ->
                                        updateProgress(getWorkDone() + updateWorth, ALL_COMPLETION_PERCENTAGE));

                            } while (webUpdater.getUpdatesRemaining() > 0);

                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            super.succeeded();

                            Platform.runLater(() -> {
                                updateProgress(ALL_COMPLETION_PERCENTAGE, ALL_COMPLETION_PERCENTAGE);

                                try {
                                    Thread.sleep(THREAD_SLEEP_TIME_MILLISECONDS);
                                } catch (InterruptedException ignored) {
                                } finally {
                                    Platform.runLater(() -> {
                                        stage.close();
                                        returnToPrimaryStage();
                                    });
                                }
                            });
                        }
                    };

                    stage.setOnCloseRequest(event -> updateTask.cancel());

                    progressIndicator.progressProperty().bind(updateTask.progressProperty());

                    new Thread(updateTask).start();

                } else {
                    Platform.runLater(UpdateSceneController.this::returnToPrimaryStage);
                }
            }

            @Override
            protected void failed() {
                super.failed();

                Platform.runLater(UpdateSceneController.this::returnToPrimaryStage);
            }
        };

        new Thread(fetchTask).start();
    }

    @Override
    public void bindHolderData(ControlledSceneHolder sceneHolder) { }

    @Override
    public void setWebConnections(WebUpdater webUpdater) {
        this.webUpdater = webUpdater;
    }

    @Override
    public void setDatabaseConnections(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}
