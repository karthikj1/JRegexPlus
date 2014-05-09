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
public class ParserException extends MatcherException{      

    public ParserException()
    {
        super();
    }
    
    public ParserException(String msg)
    {
        super(msg);
    }

    public ParserException(String msg, Throwable t)
    {
        super(msg, t);
    }    
}

    

