/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kadupadu;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Sebastian
 */
public class Message {
    private String friendId;
    private MessageType type;
    private Date data;
    private String tresc;
    
    Message(String friendId, MessageType type, Date data, String tresc) {
        this.friendId = friendId;
        this.type = type;
        this.data = data;
        this.tresc = tresc;
    }
    
    Message(String[] strMessage) {
        this.friendId   = strMessage[0];
        //System.out.println(strMessage[1]);
        this.type       = (Integer.parseInt(strMessage[1]) == 0 ? MessageType.SENDED : MessageType.RECIVED);
        this.data       = new Date(strMessage[2]);
        this.tresc      = strMessage[3];
    }
    
    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return getFriendId()+";"+getType()+";"+dateFormat.format(data)+";"+tresc;
    }
    
    public String toLabel() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(data)+": "+tresc;
    }

    /**
     * @return the friendId
     */
    public String getFriendId() {
        return friendId;
    }

    /**
     * @return the type
     */
    public MessageType getType() {
        return type;
    }
}
