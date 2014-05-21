/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.ArrayList;
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
    private List<Integer> quantifier_flags;
    private Integer startIndex = -1;
    private Integer endIndex = -1;
    private Integer max_group_num = -1;
    
    private final int START = 0;
    private final int END = 1;
    

    Path_to_State() {
        Group_locations = new HashMap<>();
        quantifier_flags = new ArrayList<>();
    }

    Path_to_State(Path_to_State copyObj) {
        Integer[] tempArray;
        
        Group_locations = new HashMap<>();
        quantifier_flags = new ArrayList<>(copyObj.quantifier_flags);
        
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
        
        Integer last_quantifier_flag = get_max_quantifier_flag();
        
        // now update indices for each group
        for (Integer group_num : groupID)
            {
            tempArray = Group_locations.get(group_num);
            max_group_num = (max_group_num > group_num)? max_group_num : group_num;
            // set start and end index if this is first character in this group
            if (tempArray == null)
                {
                tempArray = new Integer[2];
                tempArray[START] = tempArray[END] = index;
                Group_locations.put(group_num, tempArray);
                continue;
                }
                            
/*              reason for below if statement is a bit subtle - 
                if index is not continuous with the current end index for this group
                that means we are within a quantifier loop and there are elements of other groups in between
                So we reset the start index as well so that we only capture members of this group
  */            if(index != tempArray[END] + 1)
                    tempArray[START] = index;
                
                /* if group_num is greater than the highest quantifier ID in the flag list
                 * we are in a quantifier loop, so we reset the start ID's for all groups affected
                 * by the flag. eg. in (\w(\d)+)*, * is in quant group 0 and + in quant group 1 
                 * so when running on string a2b, we hit 'b'(matched by \w in group 0 and 1
                 * after hitting * in group 0, so we adjust the start index for group 1
                 * so that it resets and only captures the last thing captured in that group which is 'b'
                */    
                if (group_num > last_quantifier_flag)
                    tempArray[START] = index;
                // just update the ending index                
                tempArray[END] = index;            
            }
        quantifier_flags.remove(last_quantifier_flag);
        }
    
    private Integer get_max_quantifier_flag(){
        Integer max = -1;
        for(Integer quantID: quantifier_flags)
            max = (max > quantID) ? max : quantID;
        
        return (max == -1) ? Integer.MAX_VALUE : max;       
    }
    
    void processQuantifier(final Integer index, QuantifierToken quant_token){
        Integer quantID = quant_token.getQuantifierGroup();
                        
            if (quant_token.isQuantStop())
                quantifier_flags.remove(quantID);
            else
                if(!quantifier_flags.contains(quantID))
         // seems to work without it but not sure if above if is needed - check this       
                        quantifier_flags.add(quantID);
            return;
        }

    Integer[][] get_matches_from_state() {        
        
        /*This function extracts start and end index for each submatch by
         * group from the state object.       
        */
        Integer[][] returnArray;
        Integer[] tempArray, empty_string_array;
        
        empty_string_array = new Integer[2];
        empty_string_array[START] = empty_string_array[END] = -1;
        
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
        
        emptyArray[START] = emptyArray[END] = -1;
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
        
      /*  if(!obj2.quantifier_flags.equals(this.quantifier_flags))
            return false;
        */
        for (Integer group_num = 1; group_num <= max_group_num; group_num++)
            {
            Integer[] obj2_indices = obj2.get_match_for_group(group_num);
            Integer[] indices = this.get_match_for_group(group_num);

            if ((indices[START] != obj2_indices[START]) || (indices[END] != obj2_indices[END]))
                return false;
            }
        
            return true;            
    }
}
