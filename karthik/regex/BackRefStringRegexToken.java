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
class BackRefStringRegexToken extends RegexToken {

    private CharSequence match_text;
    private int match_counter;
    EndBackRefRegexToken end_backref_token;

    BackRefStringRegexToken(CharSequence match, EndBackRefRegexToken end_token) {
        type = RegexTokenNames.BACKREF_STRING;
        match_text = match;
        end_backref_token = end_token;
        match_counter = 0;
    }

    public boolean matches(final CharSequence search_string, final int pos) {
        /* this function could really be a dummy that always returns true
         since the end_backref token has the position at which 
         the back ref string has matched and we can move on
         */
        // once we've matched every character in match_text, match always returns false
        end_backref_token.set_match_pos(
            pos + match_text.length() - match_counter);
        if (match_counter >= match_text.length())
            return false;

        // check if current character is a match        
        if (search_string.charAt(pos) == match_text.charAt(match_counter)) {
            match_counter++;
            return true;
        }

        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + ": ");
        sb.append(match_text);
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }
}
