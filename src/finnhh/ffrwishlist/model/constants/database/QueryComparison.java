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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public enum QueryComparison implements EnumSubcategories, InvalidEnumConstants {
    @InvalidConstant
    NO_COMPARISON(""),

    @BasicComparison
    @NumericComparison
    EQUAL_TO("="),
    @BasicComparison
    @NumericComparison
    NOT_EQUAL_TO("!="),
    @NumericComparison
    LESS_THAN("<"),
    @NumericComparison
    LESS_THAN_OR_EQUAL_TO("<="),
    @NumericComparison
    GREATER_THAN_OR_EQUAL_TO(">="),
    @NumericComparison
    GREATER_THAN(">");

    private final String operator;

    QueryComparison(String operator) {
        this.operator = operator;
    }

    public boolean isBasicComparison() {
        return fitsSubcategory(BasicComparison.class);
    }

    public boolean isNumericComparison() {
        return fitsSubcategory(NumericComparison.class);
    }

    @Override
    public String toString() {
        return operator;
    }

    public static QueryComparison containsOperator(String v) {
        if (v.contains(EQUAL_TO.toString())) {
            if (v.contains(NOT_EQUAL_TO.toString()))
                return NOT_EQUAL_TO;
            else if (v.contains(LESS_THAN_OR_EQUAL_TO.toString()))
                return LESS_THAN_OR_EQUAL_TO;
            else if (v.contains(GREATER_THAN_OR_EQUAL_TO.toString()))
                return GREATER_THAN_OR_EQUAL_TO;
            else
                return EQUAL_TO;
        } else if (v.contains(LESS_THAN.toString())) {
            return LESS_THAN;
        } else if (v.contains(GREATER_THAN.toString())) {
            return GREATER_THAN;
        } else {
            return NO_COMPARISON;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface BasicComparison {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NumericComparison {
    }
}
