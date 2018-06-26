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

package finnhh.ffrwishlist.model;

import finnhh.ffrwishlist.model.event.ModelEvent;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;

import java.util.List;
import java.util.Objects;

public class Set {
    public static final EventHandler<ModelEvent<Set>> DEFAULT_SET_EVENT_HANDLER = Event::consume;

    private final IntegerProperty       setID;
    private final StringProperty        setName;
    private final ListProperty<Item>    itemsAssociated;

    public Set(int setID, String setName) {
        this.setID              = new SimpleIntegerProperty(setID);
        this.setName            = new SimpleStringProperty(setName);
        this.itemsAssociated    = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Set && setID.get() == ((Set) obj).setID.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(setID.get());
    }

    @Override
    public String toString() {
        return setName.get();
    }

    public int getSetID() {
        return setID.get();
    }

    public IntegerProperty setIDProperty() {
        return setID;
    }

    public String getSetName() {
        return setName.get();
    }

    public StringProperty setNameProperty() {
        return setName;
    }

    public void addAllToItemsAssociated(List<Item> items) {
        this.itemsAssociated.get().addAll(items);
    }

    public ObservableList<Item> getItemsAssociated() {
        return itemsAssociated.get();
    }

    public ListProperty<Item> itemsAssociatedProperty() {
        return itemsAssociated;
    }
}
