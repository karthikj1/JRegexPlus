/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author karthik
 */
class Path_to_State {
    /******************************
     * This object stores the path taken to each state to enable submatch recovery
     * Each object contains the string matched so far and the groups to which 
     * each character of that string belongs.
     *         
     *  GroupIDList.get(r) contains the list of group ID's for the r'th character 
     *  in the string being matched.        
     *******************************/
    
    private List<List<Integer>> GroupIDList;
    private int startIndex = -1;
    private int endIndex = -1;

    Path_to_State() {
        GroupIDList = new ArrayList<>();
    }

    Path_to_State(Path_to_State copyObj) {
        List<List<Integer>> groupIDs = copyObj.GroupIDList;
        this.GroupIDList = new ArrayList<>();
        for (List<Integer> groupID : groupIDs) {
            List<Integer> tempGroup = new ArrayList<>();
            tempGroup.addAll(groupID);
            GroupIDList.add(tempGroup);
         this.startIndex = copyObj.startIndex;
         this.endIndex = copyObj.endIndex;
        }
    }

    void append(int index, List<Integer> groupID) {
        GroupIDList.add(groupID);
        // set startIndex if it hasn't been set yet
        if(startIndex == -1)
            startIndex = index;
        
        endIndex = index;
    }

    Integer[][] get_matches_from_state() {        
        
        /*This function extracts start and end index for each submatch by
         * group from the state object. Each character in the string has a list 
         * associated with it of the groups to which it belongs
         * The loop goes through each character in the match and sets the start 
         * index for each group in its list if it is the first character in that group
         * Otherwise, it is the last character seen (so far) in that group, so
         * the group's end Index is set to that character's position
        */
        Map<Integer, Integer[]> groupIndices = new HashMap<>();
        Integer[][] returnArray;
        Integer[] tempArray, empty_string_array;
        int match_length = endIndex - startIndex + 1;
        int max_group_num = -1;
        
        empty_string_array = new Integer[2];
        empty_string_array[0] = empty_string_array[1] = -1;
        
        if(startIndex == -1){
            returnArray = new Integer[1][2];
            returnArray[0] = empty_string_array;
            return returnArray;
        }
        
        
        for (int r = 0; r < match_length; r++) {
            for (int group : GroupIDList.get(r)) {
                if(groupIndices.get(group) == null){
                    tempArray = new Integer[2];
                    tempArray[0] = startIndex + r;
                    groupIndices.put(group, tempArray);                    
                }                                    
                groupIndices.get(group)[1] = startIndex + r;
                max_group_num = (max_group_num > group) ? max_group_num : group;
            }
        }
            // copy values from map to int matrix and return it
            returnArray = new Integer[max_group_num + 1][2];
            for(int r = 0; r <= max_group_num; r++){
                returnArray[r] = groupIndices.get(r);
                if(returnArray[r] == null)
                    returnArray[r] = empty_string_array;
            }
            return returnArray;
    }
    
    Integer[] get_match_for_group(int group_num){
        // used to match backreferences - finds the start and end index 
        // for the string matched so far by a given group
        // returns -1 in each element of the array if no match for that group

        Integer[] returnArray = new Integer[2];        
        
        int match_length = endIndex - startIndex + 1;            
        returnArray[0] = returnArray[1] = -1;
        if(startIndex == -1)
            return returnArray;
        
        for (int r = 0; r < match_length; r++) {
            if(GroupIDList.get(r).indexOf(group_num) != -1) // this character belongs to group group_num 
                {                                       
                if(returnArray[0] == -1)
                    returnArray[0] = startIndex + r;
                
                returnArray[1] = startIndex + r;            
                }
        }
        return returnArray;
    }
   
    int resultStringLength(){
        if(startIndex == -1 || endIndex == -1)
            return 0;
        
        return endIndex - startIndex + 1;
    }
    
    public boolean equals(Path_to_State obj2){
        if((obj2.startIndex != startIndex) || (obj2.endIndex != endIndex))
            return false;
        
        Integer[][] obj2_matches = obj2.get_matches_from_state();
        Integer[][] matches = this.get_matches_from_state();
        
        if(matches.length != obj2_matches.length)
            return false;
        
        for(int r = 0; r < matches.length; r++)
            if((matches[r][0] != obj2_matches[r][0]) || (matches[r][1] != obj2_matches[r][1]))
                return false;
        
        return true;
        }
        
    }
