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

import finnhh.ffrwishlist.model.constants.base.CorrespondsToColumn;
import finnhh.ffrwishlist.model.constants.base.ItemAttribute;
import finnhh.ffrwishlist.model.constants.base.SingularFilters;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;

import java.util.Locale;

@SingularFilters
@CorrespondsToColumn(QueryableColumn.TYPE)
public enum Supertype implements ItemAttribute {
    @InvalidConstant
    INVALID_SUPERTYPE(-1, "Invalid Supertype",
            new String[0],
            new Type[0]),

    MISCELLANEOUS(0, "Miscellaneous",
            new String[] {"misc"},
            new Type[] {Type.GUMBALL}),
    WEAPON(1, "Weapon",
            new String[] {"weapons"},
            new Type[] {Type.THROWN, Type.MELEE, Type.PISTOL, Type.RIFLE, Type.ROCKET, Type.SHATTERGUN}),
    ARMOR(2, "Armor",
            new String[] {"armors"},
            new Type[] {Type.BODY, Type.LEGS, Type.SHOES}),
    ACCESSORY(3, "Accessory",
            new String[] {"accessories", "cosmetic", "cosmetics"},
            new Type[] {Type.BACKPACK, Type.GLASSES, Type.HAT}),
    VEHICLE(4, "Vehicle",
            new String[] {"vehicles"},
            new Type[] {Type.VEHICLE});

    private final int value;
    private final String name;
    private final String[] allRepresentations;
    private final Type[] types;

    Supertype(int value, String name, String[] alternateRepresentations, Type[] types) {
        this.value = value;
        this.name = name;
        this.types = types;
        this.allRepresentations = new String[alternateRepresentations.length + 2];

        allRepresentations[0] = this.name().toLowerCase(Locale.ENGLISH);
        allRepresentations[1] = name.toLowerCase(Locale.ENGLISH);
        System.arraycopy(alternateRepresentations, 0, allRepresentations, 2, alternateRepresentations.length);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String[] getAllRepresentations() {
        return allRepresentations;
    }

    public Type[] getTypes() {
        return types;
    }

    public static Supertype correspondingTo(String v) {
        for (Supertype st : Supertype.values()) {
            if (st.matchesString(v))
                return st;
        }

        return INVALID_SUPERTYPE;
    }
}
