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

import finnhh.ffrwishlist.model.constants.base.StringMatcher;

public enum QueryableColumn implements StringMatcher {
    INVALID_COLUMN(new String[0]),

    ITEMID(new String[0]),
    SETID(new String[0]),
    NAME(new String[0]),

    @ParameterQueryable
    LEVEL(new String[] {"l", "lv", "lvl"}),
    @ParameterQueryable
    TYPE(new String[] {"t", "ty"}),
    @ParameterQueryable
    RARITY(new String[] {"r", "rar"}),
    @ParameterQueryable
    SETNAME(new String[] {"s", "set"}),
    @ParameterQueryable
    AMOUNT(new String[] {"a", "ct", "count", "num", "number"});

    private final String[] alternateRepresentations;

    QueryableColumn(String[] alternateRepresentations) {
        this.alternateRepresentations = alternateRepresentations;
    }

    @Override
    public boolean matchesString(String v) {
        if (this.name().equalsIgnoreCase(v))
            return true;

        for (String s : alternateRepresentations) {
            if (s.equalsIgnoreCase(v))
                return true;
        }

        return false;
    }

    public static QueryableColumn correspondingTo(String s) {
        for (QueryableColumn sc : QueryableColumn.values()) {
            if (sc.matchesString(s))
                return sc;
        }

        return INVALID_COLUMN;
    }

    public @interface ParameterQueryable {
    }
}
