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
    private Map<Integer, Integer[]> quantifier_locations;
    private Integer startIndex = -1;
    private Integer endIndex = -1;
    private Integer max_group_num = -1;
    private final int START = 0;
    private final int END = 1;
    

    Path_to_State() {
        Group_locations = new HashMap<>();
        quantifier_locations = new HashMap<>();
    }

    Path_to_State(Path_to_State copyObj) {
        Integer[] tempArray;
        
        Group_locations = new HashMap<>();
        quantifier_locations = new HashMap<>();
        
        for(Integer group_num : copyObj.Group_locations.keySet()){    
            tempArray = copyObj.Group_locations.get(group_num);
            Group_locations.put(group_num, Arrays.copyOf(tempArray, 2));
        }
      
        for(Integer group_num : copyObj.quantifier_locations.keySet()){    
            tempArray = copyObj.quantifier_locations.get(group_num);
            quantifier_locations.put(group_num, Arrays.copyOf(tempArray, 2));
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
                tempArray[START] = tempArray[END] = index;
                Group_locations.put(group_num, tempArray);
                } else    // just update the ending index 
                {                
/*              reason for below is a bit subtle - 
                if index is not continuous with the current end index for this group as stored in tempArray[END]
                that means we are within a quantifier loop and there are elements of other groups in between
                tempArray[END] and index. So we reset the start index as well so that we only capture members of this group
  */              if(index != tempArray[END] + 1)
                    tempArray[START] = index;
                // just update the ending index                
                tempArray[END] = index;
                }
            max_group_num = (max_group_num > group_num)? max_group_num : group_num;
            }
        }
    
    void processQuantifier(final Integer index, QuantifierToken quant_token){
        Integer quantID = quant_token.getQuantifierGroup();
        
        processQuantStart(index, quantID);
        if(quant_token.isQuantStop())            
            processQuantStop(index, quantID);
    }

    private void processQuantStart(final Integer index, final Integer quantID)
        {
        Integer[] tempArray;

        tempArray = quantifier_locations.get(quantID);
        if (tempArray == null)
            {
            tempArray = new Integer[2];
            tempArray[START] = tempArray[END] = index;
            quantifier_locations.put(quantID, tempArray);
            } else    // just update the ending index 
            {
            // just update the ending index                
            tempArray[END] = index;
            }
        }
    
    private void processQuantStop(final Integer index, final Integer quantID){
        Integer[] group_indices, quant_indices;
        
        for(Integer group:Group_locations.keySet()){            
            if(quantID >= group)
                continue;
            
            group_indices = Group_locations.get(group);
            if(!has_intersection(quantID, group_indices))
                continue;
            
            quant_indices = quantifier_locations.get(quantID);
            group_indices[END] = (quant_indices[START] > group_indices[START]) ? quant_indices[START] : group_indices[START];   
            
        }
        quantifier_locations.remove(quantID);
    }
    
    private boolean has_intersection(Integer quantID, Integer[] group_indices){
        // only called when quantID exists in the quantifier_locations map so
        // no need to check for null on looking up in map
        
        Integer[] quant_locations = quantifier_locations.get(quantID);
        // is this group strictly outside the quantifiers limits
        if((group_indices[END] < quant_locations[START]) || (group_indices[START] > quant_locations[END]))
            return false;
        
        // is this group strictly *inside* the quantifiers location
        if((group_indices[START] > quant_locations[START]) && (group_indices[END] < quant_locations[END]))
            return false;
        
        // if we got here, the group and the quantifier intersect
            return true;
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
