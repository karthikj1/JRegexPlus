/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.List;

/**
 *
 * @author karthik
 */
public interface Matchable {
    public boolean isEpsilon();   
    public boolean isQuantifier();   
    public boolean matches(final CharSequence s, final int pos) throws MatcherException;
    public List<Integer> getGroupID();     
    public boolean isBoundaryOrLookaround();
    public boolean isBackReference();
    public int getFlags();
}
