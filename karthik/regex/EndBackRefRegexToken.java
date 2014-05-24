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
class EndBackRefRegexToken extends BackRefRegexToken
    {
// used to mark rows to be deleted in TransitionTable class after processing back ref states    
    private int startRow, endRow;
    /* marks position in search_string at which the backreference string has been matched
       BackRef_String token sets the match_pos so the token knows at what position in the search string
       to match. All this works because, before creating the expanded transition table, we do
       a peek ahead to make sure the back ref string is present. This is primarily a time-saving measure
       to ensure that the transition table gets expanded only if there is going to be a match 
       for the backreference string.
    */
    private int match_pos = -1;

    EndBackRefRegexToken(int start, int end)
        {
        type = RegexTokenNames.ENDBACKREFERENCE;
        startRow = start;
        endRow = end;        
        }

    public boolean isBackReference()
        {
        return true;
        }

        boolean isBackref_start()
        {
        return false;
        }
    
    int get_match_pos(){
        return match_pos;
    }    
    
    void set_match_pos(int pos){
        match_pos = pos;    
    }
    
    int getStartRow()
        {
        return startRow;
        }

    int getEndRow()
        {
        return endRow;
        }


     public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(getType().name() + " ");
        return sb.toString();        
    }    

    }
