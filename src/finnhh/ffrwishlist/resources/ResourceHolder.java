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

package finnhh.ffrwishlist.resources;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;

public final class ResourceHolder {
    private static final String DB_SQL_DIR          = "db/";
    private static final String IMAGES_DIR          = "images/";
    private static final String BUTTONICONS_DIR     = IMAGES_DIR + "buttonicons/";
    private static final String SCENE_FXML_DIR      = "view/";
    private static final String STYLE_CSS_DIR       = "styles/";

    public static final int     BUTTON_ICONS_SIZE   = 32;

    //pre-loaded static final resources
    public static final Image   PROGRAM_ICON        = getImage("icon.png");
    public static final Image   NO_PICTURE          = getImage("No_Picture.jpg");

    public static final Image   DONE_ICON           = getButtonIcon("done.png");
    public static final Image   MINUS_ICON          = getButtonIcon("minus.png");
    public static final Image   PLUS_ICON           = getButtonIcon("plus.png");
    public static final Image   SET_ICON            = getButtonIcon("set.png");
    public static final Image   INFO_ICON           = getButtonIcon("info.png");

    //private constructor to disable instantiations
    private ResourceHolder() { }

    private static Image getImage(String name) {
        return new Image(ResourceHolder.class.getResourceAsStream(IMAGES_DIR + name));
    }

    private static Image getButtonIcon(String name) {
        return new Image(ResourceHolder.class.getResourceAsStream(BUTTONICONS_DIR + name));
    }

    public static InputStream getSQLFileResourceAsStream(String name) {
        return ResourceHolder.class.getResourceAsStream(DB_SQL_DIR + name);
    }

    public static URL getSceneFXMLResource(String name) {
        return ResourceHolder.class.getResource(SCENE_FXML_DIR + name);
    }

    public static URL getStyleCSSResource(String name) {
        return ResourceHolder.class.getResource(STYLE_CSS_DIR + name);
    }
}
