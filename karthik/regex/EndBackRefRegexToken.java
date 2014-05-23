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
