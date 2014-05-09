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
public class BackReferenceRegexToken extends RegexToken
    {
    private int backRefID;
    
    BackReferenceRegexToken(int id){
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
    
    
    }
