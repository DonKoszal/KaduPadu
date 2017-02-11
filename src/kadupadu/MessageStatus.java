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
public enum MessageStatus {
    
    
        NEW(0),
        OLD(1);
        
        int status;
        
        @Override
        public String toString() {
            return Integer.toString(status);
        }
        
        private MessageStatus(int status){
            if(status != 0 && status != 1) status = 0;
            this.status = status;
        }
}
