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
    
    Matcher(TransitionTable m){
        
        if(m.contains_backref())            
            transitions = new Enhanced_NFASimulator(m);
        else
            transitions = new NFASimulator(m);
        
        results = new ArrayList<>();
    }

    boolean matchFromStart(final CharSequence s) throws MatcherException{
        // used to match lookarounds
        return match(s, true);
    }
    
    public boolean match(final CharSequence s) throws MatcherException{
        return match(s, false);
    }
    
    private boolean match(final CharSequence s, boolean match_from_start) throws MatcherException
        {        
        Path_to_State result;
               
        results.clear();
        search_string = s;
        int end = s.length();
        
        int startPos = 0;
        do{
//            System.out.println("Calling with start pos = " + startPos);
//            Path_to_State.debug_numPathtoStates = 0;
            result = transitions.findOneMatch(search_string, startPos, end);
//            System.out.println("Num objects created = " + Path_to_State.debug_numPathtoStates);
            if (result == null) {
                startPos++;                
            } else {                
                                
                results.add(result.get_matches_from_state());
                startPos = startPos + result.resultStringLength();
                // prevent engine from endless loop if there is a zero-length match
                if(result.resultStringLength() == 0)
                    startPos++;
            }
        } while ((startPos < end) && (!match_from_start)); 
                
        return (results.size() > 0);
    }
   
    
    public boolean matchEntireString(final String s) throws MatcherException{
        // fix match function so it doesn't keep trying for matchEntireString case
        
        if(!match(s, true))  // no match found
            return false;
        
        if(group(0,0).equals(s)) // does the match equal the entire search string
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
        return results.get(index).length - 1; // -1 because group 0 is the entire match               
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
