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

import java.util.List;
import karthik.regex.Matcher;
import karthik.regex.MatcherException;
import karthik.regex.ParseObject;
import karthik.regex.ParserException;
import karthik.regex.Pattern;
import karthik.regex.TransitionTable;

/**
 *
 * @author karthik
 */
public class LookbehindRegexToken extends RegexToken {

    // defines whether positive or negative lookaround
    private boolean positive = true;

    private Matcher lookbehind_matcher;
    private final RegexToken[] tokArray;

    LookbehindRegexToken(final RegexToken[] tokens, final boolean positive) {
        tokArray = tokens;
        type = RegexTokenNames.LOOKBEHIND;
        this.positive = positive;
    }

    public boolean isBoundaryOrLookaround() {
        return true;
    }

    Matcher createMatcher(List<Integer> groupIDList) throws ParserException {

        ParseObject groupObj = new Pattern(tokArray, groupIDList).parse();
        TransitionTable lookbehind_transition_matrix = groupObj.
            get_transition_matrix();

        lookbehind_matcher = new Matcher(lookbehind_transition_matrix.
            get_transposed_table());

        return lookbehind_matcher;
    }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException {
        /* uses the property that if a regex matches a string, the reversed regex matches the reversed string
         * the regex is reversed by doing a matrix get_transposed_table of the original regex's transition matrix
         * this newly created transition matrix is then used to create a matcher
         * finally, this matcher searches the reversed string to look for a find
         */

        //   System.out.println("\r\nMatching lookbehind for " + search_string.toString().substring(pos, search_string.length()));
        StringBuffer reversed_search_string = new StringBuffer(search_string.
            toString().substring(0, pos));
        reversed_search_string = reversed_search_string.reverse();

        lookbehind_matcher.matchFromStart(reversed_search_string);

        if (lookbehind_matcher.matchCount() <= 0) // no matches
            return (false == positive);
        if (lookbehind_matcher.start(0, 0) != 0)
            return (false == positive);
        return (true == positive);
    }

    public String toString() {

        StringBuffer sb = new StringBuffer("");
        sb.append((positive) ? "POSITIVE " : "NEGATIVE ");
        sb.append("LOOKBEHIND ");
        if (lookbehind_matcher == null)
            sb.append("Lookbehind matcher not yet created ");
        else
            sb.append(lookbehind_matcher.toString());
        return sb.toString();
    }

}
