/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

/**
 *
 * @author karthik
 */
class BackRefString_RegexToken extends RegexToken
    {

    private CharSequence match_text;
    private int match_counter;
    EndBackRefRegexToken end_backref_token;

    BackRefString_RegexToken(CharSequence match, EndBackRefRegexToken end_token)
        {
        type = RegexTokenNames.BACKREF_STRING;
        match_text = match;
        end_backref_token = end_token;
        match_counter = 0;
        }

    public boolean matches(final CharSequence search_string, final int pos)
        {
        /* this function could really be a dummy that always returns true
            since the end_backref token has the position at which 
            the back ref string has matched and we can move on
        */
        // once we've matched every character in match_text, match always returns false
        end_backref_token.set_match_pos(pos + match_text.length() - match_counter);
        if (match_counter >= match_text.length())
            return false;

        // check if current character is a match        
        if (search_string.charAt(pos) == match_text.charAt(match_counter))
            {
            match_counter++;            
            return true;
            }

        return false;
        }

    public String toString()
        {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + ": ");
        sb.append(match_text);
        sb.append(groupID_toString() + " ");
        return sb.toString();
        }
    }
