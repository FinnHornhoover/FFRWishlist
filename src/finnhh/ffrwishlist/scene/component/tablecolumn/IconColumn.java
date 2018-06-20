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
import finnhh.ffrwishlist.resources.ResourceLoader;
import finnhh.ffrwishlist.scene.component.tablecolumn.base.ItemPackTableColumn;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;

public class IconColumn extends ItemPackTableColumn<byte[]> {
    public static final int     ICON_SIZE       = 64;
    public static final int     ICON_COL_WIDTH  = ICON_SIZE + 8;
    public static final String  COLUMN_NAME     = "Icon";


    public IconColumn() {
        setMinWidth(ICON_COL_WIDTH);
        setMaxWidth(ICON_COL_WIDTH);
        setResizable(false);
        setSortable(false);
        setText(COLUMN_NAME);
    }

    @Override
    protected void setItemPackTableCellValueFactory() {
        setCellValueFactory(cellData -> cellData.getValue().getItem().iconBytesProperty());
    }

    @Override
    protected void setItemPackTableCellFactory() {
        setCellFactory(cfData -> new TableCell<ItemPack, byte[]>() {
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
    }
}
