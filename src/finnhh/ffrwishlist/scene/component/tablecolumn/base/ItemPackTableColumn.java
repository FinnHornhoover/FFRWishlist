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

package finnhh.ffrwishlist.scene.component.tablecolumn.base;

import finnhh.ffrwishlist.model.ItemPack;
import finnhh.ffrwishlist.model.Set;
import finnhh.ffrwishlist.model.event.ModelEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;

public abstract class ItemPackTableColumn<T> extends TableColumn<ItemPack, T> {
    public static final EventHandler<ModelEvent<ItemPack>>  DEFAULT_ITEMPACK_EVENT_HANDLER  = Event::consume;
    public static final EventHandler<ModelEvent<Set>>       DEFAULT_SET_EVENT_HANDLER       = Event::consume;

    private BooleanProperty enabled;

    public ItemPackTableColumn() {
        enabled = new SimpleBooleanProperty(this, "enabled", true);

        setEditable(false);

        setItemPackTableCellValueFactory();

        setItemPackTableCellFactory();

        setItemPackTableColumnComparator();
    }

    protected abstract void setItemPackTableCellValueFactory();

    protected void setItemPackTableCellFactory() { }

    protected void setItemPackTableColumnComparator() { }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}
