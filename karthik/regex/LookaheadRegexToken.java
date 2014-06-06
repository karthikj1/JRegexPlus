/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.List;

/**
 *
 * @author karthik
 */
class LookaheadRegexToken extends RegexToken
    {

    // defines whether positive or negative lookaround
    private boolean positive = true;

    private Matcher lookahead_matcher;
    private final RegexToken[] tokArray;

    LookaheadRegexToken(final RegexToken[] tokens, final boolean positive)
        {
        tokArray = tokens;
        type = RegexTokenNames.LOOKAHEAD;
        this.positive = positive;
        }

    public boolean isBoundaryOrLookaround()
        {
        return true;
        }

    Matcher createMatcher(List<Integer> groupIDList) throws ParserException
        {
        ParseObject groupObj = new Pattern(tokArray, groupIDList).parse();

        TransitionTable lookahead_transition_matrix = groupObj.get_transition_matrix();
        lookahead_matcher = new Matcher(lookahead_transition_matrix);
        return lookahead_matcher;
        }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException
        {
        lookahead_matcher.matchFromStart(search_string.toString().substring(pos, search_string.length()));
        if (lookahead_matcher.matchCount() <= 0)
            { // no matches
            return (false == positive);
            }
        if (lookahead_matcher.start(0, 0) != 0)
            {
            return (false == positive);
            }
        return (true == positive);
        }

    public String toString()
        {

        StringBuffer sb = new StringBuffer("");
        sb.append((positive) ? "POSITIVE " : "NEGATIVE ");
        sb.append("LOOKAHEAD ");
        if(lookahead_matcher == null)
            sb.append(" Lookahead matcher not yet created ");
        else
            sb.append(lookahead_matcher.toString());
        return sb.toString();
        }

    }
