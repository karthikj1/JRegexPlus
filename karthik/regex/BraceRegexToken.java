/*
 * Copyright (C) 2014 karthik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package karthik.regex;

/**
 *
 * @author karthik
 */
class BraceRegexToken extends RegexToken {

    int min;
    int max;  // value of 0 for max means upper limit is infinity 
    boolean lazy;  // true if this is a lazy quantifier

    BraceRegexToken(int lower, int upper, boolean is_lazy) {
        min = lower;
        max = upper;
        lazy = is_lazy;
        type = (lazy) ? RegexTokenNames.LAZY_BRACE : RegexTokenNames.BRACE;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("BRACE: ");
        sb.append(String.valueOf(min));
        if (max == 0)
            sb.append(", INFINITY ");
        if (max > 0)
            sb.append(", " + String.valueOf(max) + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }

}
