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
import java.util.Set;

/**
 *
 * @author karthik
 */
class TransitionTable implements Cloneable
    {
    private int start, finish;
    private final EpsClass eps = EpsClass.getEpsClass();    

    private List<Map<Integer, Matchable>> trans_table_list = new ArrayList<>();

    private TransitionTable(int numStates) {
        expand_table(numStates);
    }

    TransitionTable(Matchable token)
        {
            elementary(token);
        }
        
       int getStart() {
        return start;
    }

    int getFinish() {
        return finish;
    }     
        
    Matchable getTransition(int row, int col)
        {
        return trans_table_list.get(row).get(col);
        }

    Matchable setTransition(int row, int col, Matchable tok)
        {
        removeTransition(row, col);
        trans_table_list.get(row).put(col, tok);
        return tok;
        }

    private void removeTransition(int row, int col){
        Map<Integer, Matchable> rowMap = trans_table_list.get(row);
        rowMap.remove(col);        
    }
    
    Set<Integer> getKeySet(int row){
        return trans_table_list.get(row).keySet();
    }
    
    private void expand_table(int num_states_to_add){
        for(int r = 0; r < num_states_to_add; r++)
            trans_table_list.add(new HashMap<Integer, Matchable>());
    }
    
    int getNumStates()
        {
        return trans_table_list.size();
        }
    
    TransitionTable elementary(final Matchable token){
       expand_table(2);
       
       start = 0;
       finish = 1;
       setTransition(0, 1, token);       
       return this;
    }
    
    TransitionTable concat(final TransitionTable n2)
        {
        Matchable token;

        int starting_num_states = getNumStates();
        int n2States = n2.getNumStates();
        int oldFinish = getFinish();
        int n2Start = n2.getStart();
        int oldn2finish = n2.getFinish();

        expand_table(n2States - 1);
        // clone matrix from TransitionTable n2 to new matrix
        // copying is done in 9 cases to cover rows before n2 start row, n2 start row
        // and rows after n2 start row and same 3 divisions for columns
        
        for (int i = 0; i < n2Start; i++)
            for (int j = 0; j < n2Start; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states + i, starting_num_states + j, token);
                }

        for (int i = 0; i < n2Start; i++)
            {
            token = n2.getTransition(i, n2Start);
            if (token != null)
                setTransition(starting_num_states + i, oldFinish, token);
            }

        for (int i = 0; i < n2Start; i++)
            for (int j = n2Start + 1; j < n2States; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states + i, starting_num_states - 1 + j, token);
                }

        for (int j = 0; j < n2Start; j++)
            {
            token = n2.getTransition(n2Start, j);
            if (token != null)
                setTransition(oldFinish, starting_num_states + j, token);
            }

        token = n2.getTransition(n2Start, n2Start);
        if (token != null)
            setTransition(oldFinish, oldFinish, token);

        for (int j = n2Start + 1; j < n2States; j++)
            {
            token = n2.getTransition(n2Start, j);
            if (token != null)
                setTransition(oldFinish, starting_num_states - 1 + j, token);
            }

        for (int i = n2Start + 1; i < n2States; i++)
            for (int j = 0; j < n2Start; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states - 1 + i, starting_num_states + j, token);
                }

        for (int i = n2Start + 1; i < n2States; i++)
            {
            token = n2.getTransition(i, n2Start);
            if (token != null)
                setTransition(starting_num_states - 1 + i, oldFinish, token);
            }

        for (int i = n2Start + 1; i < n2States; i++)
            for (int j = n2Start + 1; j < n2States; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states - 1 + i, starting_num_states - 1 + j, token);
                }

        if (oldn2finish < n2Start)
            finish = starting_num_states + oldn2finish;

        if (oldn2finish > n2Start)
            finish = starting_num_states + oldn2finish - 1;

        if (oldn2finish == n2Start)
            finish = oldFinish;

        return this;
        }
    
    TransitionTable union(final TransitionTable n2){        
        Matchable token;
        
        int starting_num_states = getNumStates();
        int n2States = n2.getNumStates();
  
        // clone matrix from TransitionTable n2 to new matrix
        expand_table(n2States);
        for(int i = 0; i < n2States; i++)
            for(int j : n2.getKeySet(i)){
                 token = n2.getTransition(i,j);             
                 setTransition(starting_num_states + i, starting_num_states + j,token);
            }
          
        // set e-transitions for new start state
         setTransition(start, n2.getStart() + starting_num_states, eps);        
        
      // set e-transition to go to new finish state
         setTransition(n2.getFinish() + starting_num_states, finish, eps);     
                
        return this;
    }
    
    TransitionTable plus(){
        TransitionTable temp = this.clone();
        star();
        concat(temp);
        temp = null;
        return this;
    }
    
    TransitionTable star(){
        // same as question but with one e-transition more
        int oldFinish = getFinish();
        int oldStart = getStart();
        question();
        setTransition(oldFinish, oldStart, eps); 
        
       return this;
    }
    
    TransitionTable question(){
        
        int oldFinish = getFinish();
        
        expand_table(1);
        finish = getNumStates() - 1;
        // set e-transitions for new start state
                
        setTransition(start, finish, eps);        
        
        // set e-transition to go to new finish state
        setTransition(oldFinish, finish, eps);    
                        
        return this;
    }
    
   TransitionTable brace(final int min,final int max){ 
        // max = 0 implies max is infinity       
               
       TransitionTable temp;
       
        if ((min == 0) && (max == 0)){
            star();
            return this;
        }         
            
        if(min == 0)
           question();
        temp = this.clone();   
        temp.removeTransition(temp.getFinish(), temp.getFinish());
        
        for(int r = 2; r < min; r++)
            concat(temp);        
            
        if(max == 0){   // n1 is repeated min or more times, no upper limit
            concat(temp.plus());
            return this;
        }
        
        expand_table(1);  // add finish state
        int brace_finish = getNumStates() - 1;
        int adjusted_min = min;
        if(min == 1){
            setTransition(finish, brace_finish, eps);
          adjusted_min = 2;   
        }
        for(int r = adjusted_min; r <= max; r++){
            // n1 is repeated between min and max times               
            // set e-transitions so that NFA state can transition to finish
            // anywhere between min and max times
            concat(temp);
            setTransition(getFinish(), brace_finish, eps);
        }
        finish = brace_finish;
        return this;
    }
   
    TransitionTable get_new_table_with_expanded_backref(final TransitionTable backref_table, 
            int backref_token_row, int backref_token_col){    
        
        int newNFAStates;
        Matchable token;
        
        int n1States = getNumStates();
        int n2States = backref_table.getNumStates(); 
        backref_table.removeTransition(backref_table.getFinish(), backref_table.getFinish());
        
        newNFAStates = n1States + n2States;
        TransitionTable newTransMatrix = new TransitionTable(newNFAStates);
        newTransMatrix.start = start;
        newTransMatrix.finish = finish;
                
        // copy matrix from TransitionTable in <this> object to new matrix
        for(int i = 0; i < n1States; i++)
            for(int j : getKeySet(i)){
                token = getTransition(i,j);                
                newTransMatrix.setTransition(i,j,token);             
            }
        // copy matrix from TransitionTable backref_table to new matrix
        for(int i = 0; i < n2States; i++)
            for(int j : backref_table.getKeySet(i)){
                token = backref_table.getTransition(i,j);                
                newTransMatrix.setTransition(n1States + i, n1States + j, token);
            }
        
        newTransMatrix.removeTransition(backref_token_row, backref_token_col);
        newTransMatrix.setTransition(backref_token_row, n1States, newTransMatrix.eps);
        newTransMatrix.setTransition(n1States + backref_table.getFinish(), backref_token_col, newTransMatrix.eps);
        
        return newTransMatrix;
    }
   

    TransitionTable get_transposed_table()
        {
        int numStates = getNumStates();
        TransitionTable transposeMatrix = new TransitionTable(numStates);
        Matchable token;

        for (int transMatrixRow = 0; transMatrixRow < numStates; transMatrixRow++)
            for (int col : getKeySet(transMatrixRow))
                {
                token = this.getTransition(transMatrixRow, col);
                transposeMatrix.setTransition(col, transMatrixRow, token);
                }
                
        transposeMatrix.start = this.getFinish();
        transposeMatrix.finish = this.getStart();
        
        return transposeMatrix;
        }

    public TransitionTable clone()
        {
        Matchable token;
        try
            {
            TransitionTable newTable = (TransitionTable) super.clone();            
            newTable.start = start;
            newTable.finish = finish;
            newTable.trans_table_list = new ArrayList<>();
            
            int numStates = getNumStates();
            newTable.expand_table(numStates);
            
            for (int transMatrixRow = 0; transMatrixRow < numStates; transMatrixRow++)
                for (Integer col : getKeySet(transMatrixRow))
                    {
                    token = this.getTransition(transMatrixRow, col);                    
                    newTable.setTransition(transMatrixRow, col, token);
                    }

            return newTable;
            } catch (CloneNotSupportedException cnse)
            {
            // can only get here if an error in cloning the transition table
            throw new Error("Error when cloning transition table");
            }
        }

    public String toString()
        {
        Matchable tok;
        int r = 0;
        StringBuilder sb = new StringBuilder("\r\n");

        for (Map<Integer, Matchable> transMatrixRows : trans_table_list)
            {
            sb.append("Row " + r++ + ":; ");
            for (int i : transMatrixRows.keySet())
                {
                tok = transMatrixRows.get(i);
                if (tok == null)   // should not get to this branch since map will only contain valid tokens
                    {
                    sb.append(i + ":");
                    sb.append(transMatrixRows.containsKey(i) ? "contains null; " : "empty; ");
                    } else
                    sb.append(i + ":" + tok.toString()).append("; ");
                }
            sb.append("\r\n");
            }
        return sb.toString();

        }

    }
