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
public class QuantifierRegexToken extends RegexToken
    {
    Integer maxGroupID = -1;
    boolean start = false;

    public QuantifierRegexToken(RegexTokenNames myType, boolean quantStart)
        {
            type = myType;
            start = quantStart;
        }
    
    List<Integer> addGroupIDList(List<Integer> newGroupID) {    
        
        groupIDList = newGroupID;
        
        for(Integer i: groupIDList)
            maxGroupID = (maxGroupID < i) ? i : maxGroupID;
            
        return groupIDList;
    }
    
    boolean isQuantStart(){
        return start;
    }
    
    Integer getQuantifierGroup(){
        return maxGroupID;
    }
    
    public boolean isEpsilon(){
        return true;
    }            
    
    }
