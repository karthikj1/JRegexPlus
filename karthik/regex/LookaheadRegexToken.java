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
class LookaheadRegexToken extends RegexToken {

    // defines whether positive or negative lookaround
    private boolean positive = true;

    private Matcher lookahead_matcher;
    private final RegexToken[] tokArray;

    LookaheadRegexToken(final RegexToken[] tokens, final boolean positive) {
        tokArray = tokens;
        type = RegexTokenNames.LOOKAHEAD;
        this.positive = positive;
    }

    public boolean isBoundaryOrLookaround() {
        return true;
    }

    Matcher createMatcher(List<Integer> groupIDList) throws ParserException {
        ParseObject groupObj = new Pattern(tokArray, groupIDList).parse();

        TransitionTable lookahead_transition_matrix = groupObj.
            get_transition_matrix();
        lookahead_matcher = new Matcher(lookahead_transition_matrix);
        return lookahead_matcher;
    }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException {
        lookahead_matcher.matchFromStart(search_string.toString().substring(pos,
            search_string.length()));
        if (lookahead_matcher.matchCount() <= 0) // no matches
            return (false == positive);
        if (lookahead_matcher.start(0, 0) != 0)
            return (false == positive);
        return (true == positive);
    }

    public String toString() {

        StringBuffer sb = new StringBuffer("");
        sb.append((positive) ? "POSITIVE " : "NEGATIVE ");
        sb.append("LOOKAHEAD ");
        if (lookahead_matcher == null)
            sb.append(" Lookahead matcher not yet created ");
        else
            sb.append(lookahead_matcher.toString());
        return sb.toString();
    }

}
