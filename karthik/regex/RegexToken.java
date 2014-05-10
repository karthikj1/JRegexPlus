/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author karthik
 */
class RegexToken implements Matchable{
    
    public enum OpTypes{
        QUANTIFIER, BINARY, NONE;
    }
    protected char c;
    protected RegexTokenNames type;
    protected List<Integer> groupIDList = new ArrayList<>();   
    
    
    protected RegexToken()
    {}
    
    RegexToken(RegexTokenNames itemType, char ch)
    {   
       this(itemType);  
       c = ch;
    }
    
    RegexToken(RegexTokenNames itemType)
    {
       type = itemType;      
    }
    
    public List<Integer> getGroupID() {
        return groupIDList;
    }

    public int getMaxID(){
        if(groupIDList.size() == 0)
            return 0;
        else            
            return groupIDList.get(groupIDList.size() - 1);
    }
    
    List<Integer> addGroupIDList(List<Integer> newGroupID) {
        for(Integer i: newGroupID)
            if(!groupIDList.contains(i))
                groupIDList.add(i);
        return groupIDList;
    }

    public RegexTokenNames getType() {
        return type;
    }
    
    public OpTypes getOpType()
    {
        switch(getType())
        {
            case STAR: case PLUS: case QUESTION: case BRACE:
                return OpTypes.QUANTIFIER;
            case OR: case AND:
                return OpTypes.BINARY;            
            default:
                return OpTypes.NONE;
        }
    }
    
    public boolean isEpsilon(){
        return (type == RegexTokenNames.EPSILON);
    }    
    
    public boolean isBoundaryOrLookaround(){
        // is_boundary defaults to false unless overridden by subclass
        return false;
    }       
    
    public boolean isBackReference(){
        // isBackReference defaults to false unless overridden by subclass
        return false;
    }       
    
    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException
    {
        char current_char = search_string.charAt(pos);
        switch(getType()){
            case CHAR:
                return (current_char == c);
            case DIGIT:
                return Character.isDigit(current_char);
            case WHITESPACE:
                return Character.isWhitespace(current_char);
            case WORD:
                 return (Character.isLetterOrDigit(current_char) || current_char == '_');
            case NONDIGIT:
                return !Character.isDigit(current_char);
            case NONWHITESPACE:
                return !Character.isWhitespace(current_char);
            case NONWORD:
                 return !(Character.isLetterOrDigit(current_char) || current_char == '_');
            case DOT:
                return true;
            case TAB:
                return (current_char == '\t');
            case NEWLINE:
                return (current_char == '\n');
            case FORMFEED:
                return (current_char == '\f');
            case CARR_RETURN:    
                return (current_char == '\r');
            case EPSILON:    
            default:
                return false;
        }
       
    }
    
    protected String groupID_toString(){
        if(groupIDList != null)            
            return "GROUPID: " + Arrays.toString(groupIDList.toArray());
        else return 
                "GROUPID: null";
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        if(getType() == RegexTokenNames.CHAR)
            sb.append(getType().name() + ":" + c + " ");
        else
            sb.append(getType().name() + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();        
    }    
}

