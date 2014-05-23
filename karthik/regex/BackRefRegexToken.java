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
public class BackRefRegexToken extends RegexToken
    {
    private int backRefID;

    public BackRefRegexToken()
        {
        }
    
    BackRefRegexToken(int id){
        backRefID = id;
        type = RegexTokenNames.BACKREFERENCE;
    }
    
    public boolean matches(CharSequence s, int pos) throws MatcherException{
            return false;        
    }
            
    public boolean isBackReference(){
        return true;
    }

    int getBackRefID()
        {
        return backRefID;
        }
    
     public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + " ");
        sb.append("GROUP " + getBackRefID() + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();        
    }    

    boolean isBackref_start()
        {
        return true;
        }

    }