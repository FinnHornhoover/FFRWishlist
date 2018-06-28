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

import finnhh.ffrwishlist.model.constants.item.Rarity;
import finnhh.ffrwishlist.model.constants.item.Type;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;

public class Item {
    private final IntegerProperty itemID;
    private final ObjectProperty<byte[]> iconBytes;
    private final StringProperty name;
    private final IntegerProperty level;
    private final ObjectProperty<Type> type;
    private final ObjectProperty<Rarity> rarity;
    private final ListProperty<Set> setsAssociated;

    public Item(int itemID, byte[] iconBytes, String name, int level, Type type, Rarity rarity) {
        this.itemID = new SimpleIntegerProperty(itemID);
        this.iconBytes = new SimpleObjectProperty<>(iconBytes);
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(level);
        this.type = new SimpleObjectProperty<>(type);
        this.rarity = new SimpleObjectProperty<>(rarity);
        this.setsAssociated = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public int getItemID() {
        return itemID.get();
    }

    public IntegerProperty itemIDProperty() {
        return itemID;
    }

    public byte[] getIconBytes() {
        return iconBytes.get();
    }

    public ObjectProperty<byte[]> iconBytesProperty() {
        return iconBytes;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getLevel() {
        return level.get();
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public Rarity getRarity() {
        return rarity.get();
    }

    public ObjectProperty<Rarity> rarityProperty() {
        return rarity;
    }

    public void addAllToSetsAssociated(List<Set> sets) {
        this.setsAssociated.get().addAll(sets);
    }

    public ObservableList<Set> getSetsAssociated() {
        return setsAssociated.get();
    }

    public ListProperty<Set> setsAssociatedProperty() {
        return setsAssociated;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && itemID.get() == ((Item) obj).itemID.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID.get());
    }

    @Override
    public String toString() {
        return name.get();
    }
}
