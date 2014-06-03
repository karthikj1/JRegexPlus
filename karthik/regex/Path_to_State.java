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
     *******************************/
    private Map<Integer, Integer[]> Group_locations;
    private List<Integer> quantifier_flags;
    
    // keeps track of how many characters have been matched by each lazy quantifier
    private List<Integer> lazy_quantifier_flags;
    private Map<Integer, Integer> lazy_quantifier_count;
    
    private Integer startIndex = -1;
    private Integer endIndex = -1;
    private Integer max_group_num = -1;
    
    private static final int START = 0;
    private static final int END = 1;
    

    Path_to_State() {
        Group_locations = new HashMap<>();
        quantifier_flags = new ArrayList<>();
        lazy_quantifier_flags = new ArrayList<>();
        lazy_quantifier_count = new HashMap<>();
    }

    Path_to_State(Path_to_State copyObj) {
        Integer[] tempArray;
        
        Group_locations = new HashMap<>();
        quantifier_flags = new ArrayList<>(copyObj.quantifier_flags);
        lazy_quantifier_flags = new ArrayList<>(copyObj.lazy_quantifier_flags);
        lazy_quantifier_count = new HashMap<>();
        
        // copy map with locations of groups
        for(Integer group_num : copyObj.Group_locations.keySet()){    
            tempArray = copyObj.Group_locations.get(group_num);
            Group_locations.put(group_num, Arrays.copyOf(tempArray, 2));
        }                 
      
        // copy map with lazy quantifiers counters
        for(Integer group_num : copyObj.lazy_quantifier_count.keySet()){    
            int count = copyObj.lazy_quantifier_count.get(group_num);
            lazy_quantifier_count.put(group_num, new Integer(count));
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
        // update counter for any lazy quantifiers that are active
        for(Integer uniqueID: lazy_quantifier_flags){
            Integer current_count = lazy_quantifier_count.get(uniqueID);
            if(current_count == null) // first character using this lazy quantifier
                lazy_quantifier_count.put(uniqueID, 1);
            else
                lazy_quantifier_count.put(uniqueID, current_count + 1);
        }
        
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
        Integer uniqueID = quant_token.getUniqueID();
                        
            if (quant_token.isQuantStop()){
                quantifier_flags.remove(quantID);
                if (quant_token.is_lazy())
                    lazy_quantifier_flags.remove(uniqueID);
            }
            else{
                if(!quantifier_flags.contains(quantID))
         // TODO: seems to work without it but not sure if above if is needed - check this      
                        quantifier_flags.add(quantID);
                if(quant_token.is_lazy() && (!lazy_quantifier_flags.contains(uniqueID)))
                    lazy_quantifier_flags.add(uniqueID);
            }
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
   
    Path_to_State compare_finish_states(Path_to_State new_finish){
        /* compares this object to another Path_to_State and returns the object
           that has the longer result after accounting for lazy quantifiers
           IF the objects have the same length, it returns this object
           NOTE: this method is only meaningful when called on states that are finish states
        */
   
        int num_lazy_quantifiers = Pattern.get_num_lazy_quantifiers();
        if(num_lazy_quantifiers == 0){
            if (new_finish.resultStringLength() > this.resultStringLength())
                return new_finish;
            else
                return this;
        }
        
        for(Integer lazy_flag_ID = 0; lazy_flag_ID < num_lazy_quantifiers; lazy_flag_ID++){
            Integer this_count = this.lazy_quantifier_count.get(lazy_flag_ID);
            Integer new_finish_count = new_finish.lazy_quantifier_count.get(lazy_flag_ID);
            // nothing to compare if both are null
            if((this_count == null) && (new_finish_count== null))
                continue;
            // if only one of them is null, it wins since it's lazy quantifier count is 0 
            // and the other one is non-zero
            if(this_count == null)
                return this;
            if(new_finish_count == null)
                return new_finish;
            /* if both of them used this lazy quantifier to match, the smaller one wins */
            if(this_count < new_finish_count)
                return this;
            if(new_finish_count < this_count)
                return new_finish;
            /* if we got here, both paths used the same number of characters for the lazy
               quantifier with ID lazy_flag_ID, so we do nothing and the loop continues to 
               check the next lazy ID, if any
            */
        }
        
        /* If we got here, both paths used the same number of characters to match 
            each lazy quantifier, so we choose the one with the longest match        
        */
            if (new_finish.resultStringLength() > this.resultStringLength())
                return new_finish;
            else
                return this;       
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
        for (Integer group_num : Group_locations.keySet())
            {
            if(group_num == 0)
                continue;
            
            Integer[] obj2_indices = obj2.Group_locations.get(group_num);
            if(obj2_indices == null)
                return false;
            
            Integer[] indices = this.Group_locations.get(group_num);

            if ((indices[START] != obj2_indices[START]) || (indices[END] != obj2_indices[END]))
                return false;
            }
        
            return true;            
    }
}
