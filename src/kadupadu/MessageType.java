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
public enum MessageType {
    
        SENDED(0),
        RECIVED(1);
        
        int type;
        
        @Override
        public String toString() {
            return Integer.toString(type);
        }
        
        private MessageType(int type){
            if(type != 0 && type != 1) type = 0;
            this.type = type;
        }
}
