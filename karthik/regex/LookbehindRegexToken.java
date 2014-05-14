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
public class LookbehindRegexToken extends RegexToken
    {

    // defines whether positive or negative lookaround
    private boolean positive = true;

    private Matcher lookbehind_matcher;
    private final RegexToken[] tokArray;

    LookbehindRegexToken(final RegexToken[] tokens, final boolean positive)
        {
        tokArray = tokens;
        type = RegexTokenNames.LOOKBEHIND;
        this.positive = positive;
        }  

    
    public boolean isBoundaryOrLookaround(){
        return true;
    }       
    
    Matcher createMatcher(List<Integer> groupIDList) throws ParserException
        {

        ParseObject groupObj = new Pattern(tokArray, groupIDList).parse();
        TransitionTable lookbehind_transition_matrix = groupObj.get_transition_matrix();

        lookbehind_matcher = new Matcher(lookbehind_transition_matrix.get_transposed_table());

        return lookbehind_matcher;
        }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException
        {
       /* uses the property that if a regex matches a string, the reversed regex matches the reversed string
        * the regex is reversed by doing a matrix get_transposed_table of the original regex's transition matrix
        * this newly created transition matrix is then used to create a matcher
        * finally, this matcher searches the reversed string to look for a find
        */
        
        //   System.out.println("\r\nMatching lookbehind for " + search_string.toString().substring(pos, search_string.length()));
        StringBuffer reversed_search_string = new StringBuffer(search_string.toString().substring(0, pos));
        reversed_search_string = reversed_search_string.reverse();

        lookbehind_matcher.matchFromStart(reversed_search_string);

        if (lookbehind_matcher.matchCount() <= 0)
            { // no matches
            return (false == positive);
            }
           if (lookbehind_matcher.start(0, 0) != 0)
                {
                return (false == positive);
                }
        return (true == positive);
        }

    public String toString()
        {

        StringBuffer sb = new StringBuffer("");
        sb.append((positive) ? "POSITIVE " : "NEGATIVE ");
        sb.append("LOOKBEHIND ");
        sb.append(lookbehind_matcher.toString());
        return sb.toString();
        }

    }
