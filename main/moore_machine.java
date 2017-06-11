/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


import java.util.*;


/**
 *
 * @author user
 */
public class moore_machine {
    public int count=0;
    public String[] new_state = new String[100];  
    public long[] output= new long[100]; 
    public String statename;
    
    public void initialise_moore(int count,String[] new_state,long[] output, String statename){  
        this.count=count;  
        this.new_state = new_state;  
        this.output=output;  
        this.statename = statename;  
    }  
    
    public long[] getoutput()
    {
        return this.output;
    }
    
    public String[] getstring()
    {
        return this.new_state;
    }
    
    public int getcount()
    {
        return this.count;
    }
    
    public String getname()
    {
        return this.statename;
    }
}
