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

import karthik.regex.Pattern;

/**
 *
 * @author karthik
 */
public class QuantifierToken extends RegexToken {

    private Integer maxGroupID;
    private final boolean start;
    private final Integer uniqueID;

    public QuantifierToken(RegexTokenNames myType, Integer id,
        boolean quant_start, Integer ID) {
        // ID is either a unique ID if this is a lazy quantifier or Pattern.GREEDY_ID if it is greedy
        type = myType;
        start = quant_start;
        maxGroupID = id;
        uniqueID = ID;
    }

    boolean isQuantStop() {
        return (start == false);
    }

    Integer getQuantifierGroup() {
        return maxGroupID;
    }

    public boolean isEpsilon() {
        return false;
    }

    boolean is_lazy() {
        // uniqueID is Pattern.GREEDY_ID if this is greedy, anything else means it is lazy
        return (uniqueID != Pattern.GREEDY_ID);
    }

    Integer getUniqueID() {
        return uniqueID;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append(getType().name() + " ");
        sb.append((isQuantStop() ? "STOP " : "START "));
        sb.append("GROUPID: [" + maxGroupID + "] ");
        return sb.toString();
    }
}
