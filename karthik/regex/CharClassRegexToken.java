/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */

 class CharClassRegexToken extends RegexToken{
    private String charClassStrings;  
    private int numClasses;
    private boolean isNegated;
    
    private final int MAXCLASSES = 10;  // arbitrarily allows up to 10 char classes at each nesting level
    CharClassRegexToken[] nestedClasses = new CharClassRegexToken[MAXCLASSES];
    private boolean[] isUnion = new boolean[MAXCLASSES];
    
    protected CharClassRegexToken(){}
    
     CharClassRegexToken(String s, boolean negative)
    {
        numClasses = 0;
        type = RegexTokenNames.CHAR_CLASS;        
        charClassStrings = s;
        isNegated = negative;        
    }

        
     CharClassRegexToken(String s)
    {
        this(s, false);
    }
    
    
     CharClassRegexToken append(char c){
    // appends a character to existing character class

        charClassStrings = charClassStrings.concat(String.valueOf(c));
        
        return this;
    }
     
     CharClassRegexToken append(CharSequence s){
    // appends a sequence of characters to existing character class

        charClassStrings = charClassStrings.concat(s.toString());        
        return this;
    }
     
     CharClassRegexToken add(CharClassRegexToken tok, boolean union) throws TokenizerException{    
         
        if(numClasses >= MAXCLASSES)
            throw new TokenizerException("Too many nested character classes: max allowed is " + MAXCLASSES);
         nestedClasses[numClasses] = tok;
         isUnion[numClasses++] = union;
         return this;         
     }
     
    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException{
        //checks if character parameter is matched by this character class
        int r = 0;
        char c = search_string.charAt(pos);
        
        boolean charClass_contains_c = charClassStrings.contains(String.valueOf(c));
        Stack<Boolean> expression_stack = new Stack<>();
        Boolean operand1, operand2;
        
        Boolean match = true;
           if ((charClass_contains_c) && (isNegated))                   
                        match =  false;
            
           if ((!charClass_contains_c) && (!isNegated))                                    
                    match = false;                    
        expression_stack.push(match);
           
        while ((r < MAXCLASSES) && (nestedClasses[r] != null)) {
            if(isUnion[r]){
                operand1 = nestedClasses[r].matches(search_string, pos);
                operand2 = expression_stack.pop();
                expression_stack.push(operand1 | operand2);                    
            }
            else 
                expression_stack.push(nestedClasses[r].matches(search_string, pos));
            r++;
        }                  
        
        match = true;
        while(!expression_stack.isEmpty())
            match = match & expression_stack.pop();
        
        return match;   
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer("CHARCLASS:");
        sb.append((isNegated) ? " [NOT " : "[");
        sb.append(charClassStrings + " ");
        
        int r = 0;
        while(nestedClasses[r] != null)
        {
            sb.append(isUnion[r] ? " OR " : " AND ");
            sb.append(nestedClasses[r].toString());
            r++;
        }
        
        sb.append(groupID_toString() + " ");
        sb.append("] "); 
        return sb.toString();
    }
    
}
