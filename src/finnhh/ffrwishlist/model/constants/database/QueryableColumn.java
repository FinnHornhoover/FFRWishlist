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

package finnhh.ffrwishlist.model.constants.database;

import finnhh.ffrwishlist.model.constants.base.EnumSubcategories;
import finnhh.ffrwishlist.model.constants.base.InvalidEnumConstants;
import finnhh.ffrwishlist.model.constants.base.ItemAttribute;
import finnhh.ffrwishlist.model.constants.base.MultipleRepresentations;
import finnhh.ffrwishlist.model.constants.item.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

public enum QueryableColumn implements MultipleRepresentations, EnumSubcategories, InvalidEnumConstants {
    @InvalidConstant
    INVALID_COLUMN(new String[0]),

    ITEMID(new String[0]),
    SETID(new String[0]),
    NAME(new String[0]),

    @FilterColumn
    @NumericFilterColumn
    LEVEL(new String[] {"l", "lv", "lvl"}),
    @FilterColumn
    TYPE(new String[] {"t", "ty"}),
    @FilterColumn
    RARITY(new String[] {"r", "rar"}),
    @FilterColumn
    @FlexibleFilterColumn
    SETNAME(new String[] {"s", "set"}),
    @FilterColumn
    @NumericFilterColumn
    AMOUNT(new String[] {"a", "ct", "count", "num", "number"});

    private final String[] allRepresentations;

    QueryableColumn(String[] alternateRepresentations) {
        this.allRepresentations = new String[alternateRepresentations.length + 1];

        allRepresentations[0] = this.name().toLowerCase(Locale.ENGLISH);
        System.arraycopy(alternateRepresentations, 0, allRepresentations, 1, alternateRepresentations.length);
    }

    public boolean isFilterColumn() {
        return fitsSubcategory(FilterColumn.class);
    }

    public boolean isNumericFilterColumn() {
        return fitsSubcategory(NumericFilterColumn.class);
    }

    public boolean isFlexibleFilterColumn() {
        return fitsSubcategory(FlexibleFilterColumn.class);
    }

    @Override
    public String[] getAllRepresentations() {
        return allRepresentations;
    }

    public static QueryableColumn correspondingTo(String s) {
        for (QueryableColumn sc : QueryableColumn.values()) {
            if (sc.matchesString(s))
                return sc;
        }

        return INVALID_COLUMN;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FilterColumn {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NumericFilterColumn {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FlexibleFilterColumn {
    }
}
