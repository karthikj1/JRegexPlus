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

import karthik.regex.MatcherException;

/**
 *
 * @author karthik
 */
public class BackRefRegexToken extends RegexToken {

    static Integer INVALID_ID = -1;
    private Integer backRefID = INVALID_ID;
    private CharSequence group_name = "";

    protected BackRefRegexToken() {
    }

    BackRefRegexToken(Integer id) {
        backRefID = id;
        type = RegexTokenNames.BACKREFERENCE;
    }

    BackRefRegexToken(CharSequence name) {
        group_name = name;
        type = RegexTokenNames.BACKREFERENCE;
    }

    public boolean matches(CharSequence s, Integer pos) throws MatcherException {
        return false;
    }

    public boolean isBackReference() {
        return true;
    }

    Integer getBackRefID() {
        return backRefID;
    }

    void setBackRefID(Integer backRefID) {
        this.backRefID = backRefID;
    }

    CharSequence get_group_name() {
        return group_name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + " ");
        sb.append("GROUP " + getBackRefID() + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }

    boolean isBackref_start() {
        return true;
    }

}
