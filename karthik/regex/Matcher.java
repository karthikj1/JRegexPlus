/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author karthik
 */
public class Matcher {
    
    private final NFASimulator transitions;
    private List<Integer[][]> results;    
    private CharSequence search_string;
    private int startPos;
    private int region_end;
    
    Matcher(TransitionTable m){
        
        if(m.contains_backref())            
            transitions = new Enhanced_NFASimulator(m);
        else
            transitions = new NFASimulator(m);
        
        results = new ArrayList<>();
        startPos = 0;
    }

    public Matcher reset(){        
        startPos = 0;
        return this;
    }
            
    boolean matchFromStart(final CharSequence s) throws MatcherException{
        // used to find lookarounds
        return find(s, true);
    }
    
    public boolean find(final CharSequence s) throws MatcherException{
        return find(s, false);
    }
    
    private boolean find(final CharSequence s, boolean match_from_start) throws MatcherException
        {        
        Path_to_State result;        
        
        results = new ArrayList<>();
        search_string = s;
        region_end = s.length();
        
        if(match_from_start)
            startPos = 0;
        
        do{
            result = transitions.findOneMatch(search_string, startPos, region_end - 1);
            if (result == null) {
                startPos++;                
            } else {                                                
                results.add(result.get_matches_from_state());                
                startPos = startPos + result.resultStringLength();                
                // prevent engine from endless loop if there is a zero-length find
                if(result.resultStringLength() == 0)
                    startPos++;
                break;
            }
        } while ((startPos < region_end) && (!match_from_start)); 
                
        return (results.size() > 0);
    }
   
    
    public boolean matchEntireString(final String s) throws MatcherException{
        
        if(!find(s, true))  // no find found
            return false;
        
        if(group(0,0).equals(s)) // does the find equal the entire search string
            return true;
        else
            results.clear();
        return false;
    }

    public int start(int matchIndex, int groupIndex) throws MatcherException{
        check_size(matchIndex, groupIndex);
        int start = results.get(matchIndex)[groupIndex][0];
        return start;
    }
    
    
    public int end(int matchIndex, int groupIndex) throws MatcherException{
        check_size(matchIndex, groupIndex);
        int end = results.get(matchIndex)[groupIndex][1];
        return end;
    }
    
    
    public String group(int matchIndex, int groupIndex) throws MatcherException{
        // finds the groupIndex group in the match_string numbered matchIndex
        check_size(matchIndex, groupIndex);
        Integer start = results.get(matchIndex)[groupIndex][0];
        Integer end = results.get(matchIndex)[groupIndex][1];
        if(start == -1)
            return "";
        
        return search_string.toString().substring(start, end + 1);        
    }
    
    public String group(int... groupIndex) throws MatcherException{
        int index = 0;
        if(groupIndex.length > 0){
            index = groupIndex[0];
            check_size(0,groupIndex[0]);
        }
        else
            check_size(0,0);
        return group(0, index);
    }        

    public int groupCount(int... matchIndex) throws MatcherException{
        int index = 0;
        if(matchIndex.length > 0){
            index = matchIndex[0];
            check_size(index, 0);
        }
        else
            check_size(0,0);
        return results.get(index).length - 1; // -1 because group 0 is the entire find               
    }
    
    public int matchCount(){
        return results.size();
    }
    
    private boolean check_size(int matchIndex, int groupIndex) throws MatcherException{
        if (results.size() > matchIndex){
            if(results.get(matchIndex).length > groupIndex)
                return true;
            else
                throw new MatcherException("Match " + matchIndex + "contains less than "
                    + groupIndex + "groups");
        }
        else
            throw new MatcherException("There are less than " + matchIndex + "matches");            
        
    }
    
    public CharSequence get_regex_string(){
        return transitions.getRegexString();
    }
    
    public String toString(){
        return transitions.toString();
    }
}
