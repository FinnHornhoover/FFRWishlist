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

package finnhh.ffrwishlist.model.constants.item;

import finnhh.ffrwishlist.model.constants.base.IntegerValued;
import finnhh.ffrwishlist.model.constants.base.StringMatcher;

public enum Rarity implements IntegerValued, StringMatcher {
    INVALID_RARITY(-1, "Invalid Rarity",
            new String[0]),

    UNDEFINED(0, "Undefined",
            new String[] {"und", "null", "none", "nan", "uk", "unknown"}),
    COMMON(1, "Common",
            new String[] {"c", "co"}),
    UNCOMMON(2, "Uncommon",
            new String[] {"uc", "unc"}),
    RARE(3, "Rare",
            new String[] {"r", "ra"}),
    ULTRA_RARE(4, "Ultra Rare",
            new String[] {"ur", "ultra", "ultra-rare", "ultrarare"});

    private final int value;
    private final String name;
    private final String[] alternateRepresentations;

    Rarity(int value, String name, String[] alternateRepresentations) {
        this.value = value;
        this.name = name;
        this.alternateRepresentations = alternateRepresentations;
    }

    @Override
    public boolean matchesString(String v) {
        if (this.toString().equalsIgnoreCase(v) || this.name().equalsIgnoreCase(v))
            return true;

        for (String s : alternateRepresentations) {
            if (s.equalsIgnoreCase(v))
                return true;
        }

        return false;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Rarity correspondingTo(int k) {
        for (Rarity r: Rarity.values()) {
            if (r.intValue() == k)
                return r;
        }

        return INVALID_RARITY;
    }

    public static Rarity correspondingTo(String v) {
        for (Rarity r : Rarity.values()) {
            if (r.matchesString(v))
                return r;
        }

        return INVALID_RARITY;
    }
}
