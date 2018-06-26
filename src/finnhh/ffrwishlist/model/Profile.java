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

import javafx.beans.property.*;

import java.util.Objects;

public class Profile {
    private final IntegerProperty   profileID;
    private final StringProperty    profileName;
    private final BooleanProperty   active;

    public Profile(int profileID, String profileName, boolean active) {
        this.profileID              = new SimpleIntegerProperty(profileID);
        this.profileName            = new SimpleStringProperty(profileName);
        this.active                 = new SimpleBooleanProperty(active);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Profile && this.profileID.get() == ((Profile) obj).profileID.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileID.get());
    }

    @Override
    public String toString() {
        return profileName.get();
    }

    public int getProfileID() {
        return profileID.get();
    }

    public IntegerProperty profileIDProperty() {
        return profileID;
    }

    public void setProfileName(String profileName) {
        this.profileName.set(profileName);
    }

    public String getProfileName() {
        return profileName.get();
    }

    public StringProperty profileNameProperty() {
        return profileName;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }
}
