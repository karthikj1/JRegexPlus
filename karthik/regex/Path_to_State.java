/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.Arrays;
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
    private Map<Integer, Integer[]> Group_locations;
    private Integer startIndex = -1;
    private Integer endIndex = -1;
    private Integer max_group_num = -1;

    Path_to_State() {
        Group_locations = new HashMap<>();
    }

    Path_to_State(Path_to_State copyObj) {
        Integer[] tempArray;
        
        Group_locations = new HashMap<>();
               
        for(Integer group_num : copyObj.Group_locations.keySet()){    
            tempArray = copyObj.Group_locations.get(group_num);
            Group_locations.put(group_num, Arrays.copyOf(tempArray, 2));
        }
            
         this.startIndex = copyObj.startIndex;
         this.endIndex = copyObj.endIndex;      
         this.max_group_num = copyObj.max_group_num;
    }

    void append(final Integer index, final List<Integer> groupID)
        {
        Integer[] tempArray;        
        // first update global start and end index
        if(startIndex == -1)
            startIndex = index;
        
        endIndex = index;
        
        // now update indices for each group
        for (Integer group_num : groupID)
            {
            tempArray = Group_locations.get(group_num);
            // set start and end index if this is first character in this group
            if (tempArray == null)
                {
                tempArray = new Integer[2];
                tempArray[0] = tempArray[1] = index;
                Group_locations.put(group_num, tempArray);
                } else    // just update the ending index 
                {
                tempArray[1] = index;
                }
            max_group_num = (max_group_num > group_num)? max_group_num : group_num;
            }
        }

    Integer[][] get_matches_from_state() {        
        
        /*This function extracts start and end index for each submatch by
         * group from the state object.       
        */
        Integer[][] returnArray;
        Integer[] tempArray, empty_string_array;
        
        empty_string_array = new Integer[2];
        empty_string_array[0] = empty_string_array[1] = -1;
        
        if(startIndex == -1){
            returnArray = new Integer[1][2];
            returnArray[0] = empty_string_array;
            return returnArray;
        }
        
           // copy values from map to Integer matrix and return it
            returnArray = new Integer[max_group_num + 1][2];
            for(Integer r = 0; r <= max_group_num; r++){
                tempArray = Group_locations.get(r);
                if(tempArray == null)
                    returnArray[r] = empty_string_array;
                else
                    // make a copy of the array in the map so that a user cannot accidentally change 
                    // the data in the map
                    returnArray[r] = Arrays.copyOf(tempArray, 2);
            }
            return returnArray;
    }
    
    Integer[] get_match_for_group(Integer group_num){
        // used to match backreferences - finds the start and end index 
        // for the string matched so far by a given group
        // returns -1 in each element of the array if no match for that group

        Integer[] emptyArray = new Integer[2];        
        Integer[] tempArray;
        
        emptyArray[0] = emptyArray[1] = -1;
        if(startIndex == -1)
            return emptyArray;
        
        tempArray = Group_locations.get(group_num);
        if(tempArray == null)
            return emptyArray;
                
        return Arrays.copyOf(tempArray, 2);
    }
   
    int resultStringLength(){
        if(startIndex == -1 || endIndex == -1)
            return 0;
        
        return endIndex - startIndex + 1;
    }
    
    public boolean equals(Path_to_State obj2){
        // first check if obj2 references the same object as this
        if(obj2 == this)
            return true;
        
        if((obj2.startIndex != startIndex) || (obj2.endIndex != endIndex))
            return false;
        
        if(obj2.max_group_num != this.max_group_num)
            return false;
        
        for (Integer group_num = 1; group_num <= max_group_num; group_num++)
            {
            Integer[] obj2_indices = obj2.get_match_for_group(group_num);
            Integer[] indices = this.get_match_for_group(group_num);

            if ((indices[0] != obj2_indices[0]) || (indices[1] != obj2_indices[1]))
                return false;
            }
        
            return true;            
    }
}
