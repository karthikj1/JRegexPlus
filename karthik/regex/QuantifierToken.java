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
public class QuantifierToken extends RegexToken
    {
    private Integer maxGroupID;
    private final boolean start;
    private final Integer uniqueID;

    public QuantifierToken(RegexTokenNames myType, Integer id, boolean quant_start, Integer ID)
        {
        // ID is either a unique ID if this is a lazy quantifier or Pattern.GREEDY_ID if it is greedy
            type = myType;
            start = quant_start;
            maxGroupID = id;
            uniqueID = ID;
        }    
    
    boolean isQuantStop(){
        return (start == false);
    }
    
    Integer getQuantifierGroup(){
        return maxGroupID;
    }
    
    public boolean isEpsilon(){
        return false;
    }            
    
    boolean is_lazy(){
        // uniqueID is Pattern.GREEDY_ID if this is greedy, anything else means it is lazy
        return (uniqueID != Pattern.GREEDY_ID);
    }
    
    Integer getUniqueID(){
        return uniqueID;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        
        sb.append(getType().name() + " ");
        sb.append((isQuantStop() ? "STOP " : "START "));
        sb.append("GROUPID: [" + maxGroupID + "] ");
        return sb.toString();        
    }    
 }
