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
public class TokenizerException extends MatcherException {
    

    public TokenizerException()
    {
        super();
    }
    
    public TokenizerException(String msg)
    {
        super(msg);
    }

    public TokenizerException(String msg, Throwable t)
    {
        super(msg, t);
    }    
}
