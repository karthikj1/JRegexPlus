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
    Integer maxGroupID;
    boolean start = false;

    public QuantifierToken(RegexTokenNames myType, Integer id, boolean quant_start)
        {
            type = myType;
            start = quant_start;
            maxGroupID = id;
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
    
        public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        
        sb.append(getType().name() + " ");
        sb.append((isQuantStop() ? "STOP " : "START "));
        sb.append("GROUPID: [" + maxGroupID + "] ");
        return sb.toString();        
    }    
 }
