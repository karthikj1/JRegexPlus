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

class BraceRegexToken extends RegexToken{
    int min;    
    int max;  // value of 0 for max means upper limit is infinity 
    boolean lazy;  // true if this is a lazy quantifier
    
    
    BraceRegexToken(int lower, int upper, boolean is_lazy){
        min = lower;
        max = upper;
        lazy = is_lazy;
        type = (lazy) ? RegexTokenNames.LAZY_BRACE : RegexTokenNames.BRACE;        
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer("BRACE: ");
        sb.append(String.valueOf(min));
        if(max == 0)
            sb.append(", INFINITY ");
        if(max > 0)
            sb.append(", " + String.valueOf(max) + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }
    
}
