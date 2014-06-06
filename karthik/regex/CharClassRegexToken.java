/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.ArrayList;
import java.util.List;
import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */
class CharClassRegexToken extends RegexToken
    {

    private StringBuffer charClassStrings = new StringBuffer("");
    // list of nested character classes in this class eg. [a-f[t-z]]
    List<NestedCharClass> nestedClasses = new ArrayList<>();    
    // list of ranges in this character class eg. [a-fm-p]
    List<char[]> rangeList = new ArrayList<>();

    private boolean isNegated;
    private boolean case_insensitive;

    protected CharClassRegexToken()
        {
        }

    private CharClassRegexToken(boolean negative, int flags){
        type = RegexTokenNames.CHAR_CLASS;
        isNegated = negative;
        this.flags = flags;   // flags variable inherited from RegexToken
        case_insensitive = ((flags & Pattern.CASE_INSENSITIVE) != 0);      
    }
    
    CharClassRegexToken(String s, boolean negative, int flags)
        {
        this(negative, flags);
        charClassStrings = new StringBuffer(s);
        }

    CharClassRegexToken(char range_start, char range_end, boolean negative, int flags)
        {
        this(negative, flags);
        append(range_start, range_end);
        }

    CharClassRegexToken append(char c)
        {
        // appends a character to existing character class

        charClassStrings = charClassStrings.append(String.valueOf(c));

        return this;
        }

    CharClassRegexToken append(CharSequence s)
        {
        // appends a sequence of characters to existing character class

        charClassStrings = charClassStrings.append(s.toString());
        return this;
        }

    CharClassRegexToken append(char range_start, char range_end)
        {
        // appends a character range to existing character class
        char[] rangeArray = new char[2];
        rangeArray[0] = range_start;
        rangeArray[1] = range_end;
        rangeList.add(rangeArray);
        return this;
        }

    CharClassRegexToken add(CharClassRegexToken tok, boolean union) throws TokenizerException
        {
        nestedClasses.add(new NestedCharClass(tok, union));
        return this;
        }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException
        {
        //checks if character parameter is matched by this character class        
        char c = search_string.charAt(pos);
        boolean charClass_contains_c = contains(c);
        Stack<Boolean> expression_stack = new Stack<>();
        Boolean operand1, operand2;

        Boolean match = true;
        if ((charClass_contains_c) && (isNegated))
            match = false;

        if ((!charClass_contains_c) && (!isNegated))
            match = false;
        expression_stack.push(match);

        for (NestedCharClass charClass : nestedClasses)
            {
            if (charClass.isUnion)
                {
                operand1 = charClass.matches(search_string, pos);
                operand2 = expression_stack.pop();
                expression_stack.push(operand1 | operand2);
                } else
                expression_stack.push(charClass.matches(search_string, pos));
            }

        match = true;
        while (!expression_stack.isEmpty())
            match = match & expression_stack.pop();

        return match;
        }

    private boolean contains(char c)
        {
        char case_switched_c = Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);        

        if (charClassStrings.indexOf(String.valueOf(c)) != -1)
            return true;
        
        // check for case_insensitive match if needed
        if (case_insensitive && (charClassStrings.indexOf(String.valueOf(case_switched_c)) != -1))
            return true;

        for (char[] range : rangeList)
            {
            if ((Character.compare(range[0], c) <= 0)
                    && (Character.compare(range[1], c) >= 0))
                return true;  // true if c is within the range 
            if (case_insensitive)
                {
                if ((Character.compare(range[0], case_switched_c) <= 0)
                        && (Character.compare(range[1], case_switched_c) >= 0))
                    return true;
                // true if case_insensitive matching and c is within the range with its case switched                
                }
            }
        return false;
        }

    public String toString()
        {
        StringBuffer sb = new StringBuffer("CHARCLASS:");
        sb.append((isNegated) ? " [NOT " : "[");
        sb.append(charClassStrings + " ");
        for (char[] rangeArray : rangeList)
            {
            sb.append(rangeArray[0] + "-" + rangeArray[1]);
            }

        for (NestedCharClass charClass : nestedClasses)
            {
            sb.append(charClass.isUnion ? " OR " : " AND ");
            sb.append(charClass.toString());
            }

        sb.append(groupID_toString() + " ");
        sb.append("] ");
        return sb.toString();
        }

    private class NestedCharClass
        {

        CharClassRegexToken charClassToken;
        boolean isUnion;

        NestedCharClass(CharClassRegexToken tok, boolean union)
            {
            charClassToken = tok;
            isUnion = union;
            }

        boolean matches(CharSequence search_string, int pos) throws MatcherException
            {
            return charClassToken.matches(search_string, pos);
            }
        }

    }
