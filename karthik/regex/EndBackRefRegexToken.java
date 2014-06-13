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
class EndBackRefRegexToken extends BackRefRegexToken {
// used to mark rows to be deleted in TransitionTable class after processing back ref states    

    private int startRow, endRow;
    /* marks position in search_string at which the backreference string has been matched
     BackRef_String token sets the match_pos so the token knows at what position in the search string
     to match. All this works because, before creating the expanded transition table, we do
     a peek ahead to make sure the back ref string is present. This is primarily a time-saving measure
     to ensure that the transition table gets expanded only if there is going to be a match 
     for the backreference string.
     */
    private int match_pos = -1;

    EndBackRefRegexToken(int start, int end) {
        type = RegexTokenNames.ENDBACKREFERENCE;
        startRow = start;
        endRow = end;
    }

    public boolean isBackReference() {
        return true;
    }

    boolean isBackref_start() {
        return false;
    }

    public boolean matches(CharSequence s, int pos) {
        return (pos == get_match_pos());
    }

    int get_match_pos() {
        return match_pos;
    }

    void set_match_pos(int pos) {
        match_pos = pos;
    }

    int getStartRow() {
        return startRow;
    }

    int getEndRow() {
        return endRow;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + " ");
        return sb.toString();
    }

}
