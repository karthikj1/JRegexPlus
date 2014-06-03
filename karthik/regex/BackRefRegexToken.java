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
    static Integer INVALID_ID = -1;
    private Integer backRefID = INVALID_ID;
    private CharSequence group_name = "";

    protected BackRefRegexToken() {}
    
    BackRefRegexToken(Integer id){
        backRefID = id;
        type = RegexTokenNames.BACKREFERENCE;
    }

    BackRefRegexToken(CharSequence name)
        {
        group_name = name;
        type = RegexTokenNames.BACKREFERENCE;
        }

    public boolean matches(CharSequence s, Integer pos) throws MatcherException{
            return false;        
    }
            
    public boolean isBackReference(){
        return true;
    }

    Integer getBackRefID()
        {
        return backRefID;
        }
    
    void setBackRefID(Integer backRefID)
        {
        this.backRefID = backRefID;
        }

    CharSequence get_group_name()
        {
        return group_name;
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
