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

import finnhh.ffrwishlist.model.constants.base.ItemAttribute;
import finnhh.ffrwishlist.model.constants.base.annotation.CorrespondsToColumn;
import finnhh.ffrwishlist.model.constants.database.QueryableColumn;

import java.util.Locale;

@CorrespondsToColumn(QueryableColumn.AMOUNT)
public enum Amount implements ItemAttribute {
    @InvalidConstant
    INVALID_AMOUNT(-1,
            new String[0]),

    NONE(0,
            new String[] {"none", "null"}),
    MINIMUM(1,
            new String[] {"min"}),
    MAXIMUM(255,
            new String[] {"max"});

    private final int value;
    private final String[] allRepresentations;

    Amount(int value, String[] alternateRepresentations) {
        this.value = value;
        this.allRepresentations = new String[alternateRepresentations.length + 1];

        allRepresentations[0] = this.name().toLowerCase(Locale.ENGLISH);
        System.arraycopy(alternateRepresentations, 0, allRepresentations, 1, alternateRepresentations.length);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public String[] getAllRepresentations() {
        return allRepresentations;
    }

    public static boolean isValidAmount(int k) {
        return k >= MINIMUM.intValue() && k <= MAXIMUM.intValue();
    }

    public static Amount correspondingTo(String v) {
        for (Amount a : Amount.values()) {
            if (a.matchesString(v))
                return a;
        }

        return INVALID_AMOUNT;
    }
}
