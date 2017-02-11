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
    private MessageStatus status;
    private Date data;
    private String tresc;
    
    Message(String friendId, MessageType type, MessageStatus status, Date data, String tresc) {
        this.friendId = friendId;
        this.type = type;
        this.status = status;
        this.data = data;
        this.tresc = tresc;
    }
    
    Message(String[] strMessage) {
        this.friendId   = strMessage[0];
        //System.out.println(strMessage[1]);
        this.type       = (Integer.parseInt(strMessage[1]) == 0 ? MessageType.SENDED : MessageType.RECIVED);
        this.status     = (Integer.parseInt(strMessage[2]) == 0 ? MessageStatus.OLD : MessageStatus.NEW);
        this.data       = new Date(strMessage[3]);
        this.tresc      = strMessage[4];
    }
    
    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return friendId+";"+type+";"+status+";"+dateFormat.format(data)+";"+tresc;
    }
    
    public String toLabel() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(data)+": "+tresc;
    }
}
