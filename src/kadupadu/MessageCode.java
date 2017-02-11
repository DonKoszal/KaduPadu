/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kadupadu;

/**
 *
 * @author Sebastian
 */
public enum MessageCode {
    
    MY_ID("I"),
    NEW_FRIEND("F"),
    NEW_ID("N"),
    SEND_MESSAGE("M");
        
    String msg;
    
    @Override
    public String toString() {
        return msg;
    }
    
    private MessageCode(String msg){
        this.msg = msg;
    }
}
