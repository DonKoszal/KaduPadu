/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kadupadu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Sebastian
 */
public class KaduPadu extends JFrame {

    private final String idPath = "id.txt";
    private final String friendsPath = "friends.txt";
    private final String historyPath = "history.txt";
    
    private final String serverHostName = "localhost";
    private final int serverPortNumber = 1683;
    private Socket clientSocket;
    private BufferedReader clientReader;
    private PrintWriter clientWriter;
    
    private int userID;
    private ArrayList<String> friendsList = new ArrayList<String>();
    private HashMap<Integer, ArrayList<Message>> messageMap = new HashMap<Integer, ArrayList<Message>>();
    
    private JPanel container;
    private JPanel historyPane;
    private JPanel friendsPanel;
    
    private JList friendsSelect;
    private DefaultListModel friendsListModel;
    private JTextField newFriendText;
    private JButton newFriendBtn;
    private JButton sendMessageBtn;
    private final JLabel friendsLabel = new JLabel("Lista znajomych");
    private final JLabel kaduPaduLabel = new JLabel("KaduPadu version pre-alpha");
    private JLabel newFriendLabel = new JLabel("Wpisz ID nowego znajomego");
    private JTextArea messageArea;
    private JTextArea historyArea;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //this.friendsList = new ArrayList<String>();
        
        System.out.println("Start programu");
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    KaduPadu frame = null;
                    try {
                        frame = new KaduPadu();
                        frame.setVisible(true);
                    } catch (IOException ex) {
                        Logger.getLogger(KaduPadu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
    }
    
    public KaduPadu() throws IOException {
        System.out.println("Łączenie z serwerem");
        if(!startServerConnection(this.getServerHostName(), this.getServerPortNumber())) {
            System.out.println("Nie udało się nawiązać połączenia");

            return;
        } else {
            initializeId(this.getIdPath());
            initialFriendsList(this.getFriendsPath());
            initialMessageList(this.getHistoryPath());
        
            System.out.println("Tworzenie głównego okna");
            //ustaw właściwości okna
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);
            this.setBounds(100, 100, 600, 400);//pozycja na ekranie + wielkość

            container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            this.setContentPane(container);


            this.addMessagePanel();

            this.addFriendsPanel();
        }
    }
    
    private boolean startServerConnection(String hostName, int port) {
        try {
            clientSocket = new Socket(hostName, port);
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            Thread handler = new Thread(){
                @Override
                public void run() {
                    System.out.println("Rozpoczęto nasłuchiwanie na serwer");
                    //String serverMessage;
                    while(true){
                        //try {
                        //    serverMessage = clientReader.readLine();
                        //    System.out.println("Odczytano wiadomość z serwera: " + serverMessage);
                        //} catch (IOException ex) {
                        //    Logger.getLogger(KaduPadu.class.getName()).log(Level.SEVERE, null, ex);
                        //}
                        readMessageFromServer();
                    }
                }
            };
            handler.start();
            System.out.println("Połączono z serwerem");
            return true;
           
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void sendMessageToServer(MessageCode code,String msg){
        clientWriter.println(code.toString()+';'+msg);
        System.out.println("Wysłano wiadomość: " + code+';'+msg);
    }
	
    public void readMessageFromServer(){
        try {		
            String serverMessage = clientReader.readLine();
            String code = serverMessage.substring(0, 1);
            serverMessage = serverMessage.substring(2);
            System.out.println("Odczytano wiadomość z serwera: " + serverMessage);
            switch(code){
                case "N":
                    this.setUserID(Integer.parseInt(serverMessage));
                    this.createTxtFile(serverMessage, this.getIdPath());
                    break;
                case "F":
                    if(!"-1".equals(serverMessage)) {
                        friendsList.add(serverMessage);
                        addToTxt(serverMessage, this.getFriendsPath());
                        addFriendTOCombobox(serverMessage);
                        newFriendLabel.setForeground(Color.green);
                        newFriendLabel.setText("Prawidłowo dodano nowego znajomego!");
                        newFriendText.setText("");
                        System.out.println("Dodano nowego znajomego - "+serverMessage);
                    } else {
                        newFriendLabel.setText("Nie odnaleziono znajomego o podanym ID");
                    }
                    break;
                case "M":
                    Message msg = new Message(serverMessage.split(";"));
                    addMessage(msg);
                    addToTxt(msg.toString(), this.getHistoryPath());
                    break;
                default:
                    System.out.println("Nieznana wiadomość");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeId(String fileName) throws IOException {
        System.out.println("Poszukiwanie ID użytkownika");
        if (new File(fileName).exists())
        {
            System.out.println("Znaleziono plik z ID");
            try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
            {
                this.setUserID(Integer.parseInt(br.readLine()));
                System.out.println("Odczytano ID z pliku - " + this.getUserID());
                sendMessageToServer(MessageCode.MY_ID, Integer.toString(this.getUserID()));
            } catch (IOException e) {
                e.printStackTrace();
            } 
        } else {
            System.out.println("Nie znaleziono pliku z ID");
            sendMessageToServer(MessageCode.NEW_ID, "");
        }
    }
    
    private void createTxtFile(String content, String fileName) {
        try{
            try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {
                writer.println(content);
            }
        } catch (IOException e) {
           // do something
        }
    }
    
    private void addMessagePanel() {
        System.out.println("Tworzenie okna wiadomości");
        historyPane = new JPanel();
        historyPane.setLayout(new BoxLayout(historyPane, BoxLayout.Y_AXIS));
	historyPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        historyPane.setMinimumSize( new Dimension( this.getWidth()*2/3, this.getHeight() ) );
        historyPane.setPreferredSize( new Dimension( this.getWidth()*2/3, this.getHeight() ) );
        historyPane.setMaximumSize( new Dimension( this.getWidth()*2/3, this.getHeight() ) );
        
	kaduPaduLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
	historyPane.add(kaduPaduLabel);
        
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        
        historyArea = new JTextArea(7,1);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setEditable(false);
        historyArea.setBorder(BorderFactory.createMatteBorder(3,3,3,3,Color.blue));
        historyPane.add(historyArea);//JTextArea
        
        messageArea = new JTextArea(1, 1);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25), border));
        historyPane.add(messageArea);//JTextArea
        
        //przycisk
        sendMessageBtn = new JButton("Wyślij wiadomość");
	historyPane.add(sendMessageBtn);
	sendMessageBtn.addActionListener((ActionEvent e) -> {
            sendMessage();
        });
        
        container.add(historyPane);
    }
    
    private void addFriendsPanel() {
        System.out.println("Tworzenie okna znajomych");
        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
	friendsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        //set size
        friendsPanel.setMinimumSize( new Dimension( this.getWidth()*1/3, this.getHeight() ) );
        friendsPanel.setPreferredSize( new Dimension( this.getWidth()*1/3, this.getHeight() ) );
        friendsPanel.setMaximumSize( new Dimension( this.getWidth()*1/3, this.getHeight() ) );
        
        //add labels
	//friendsLabel.setBounds(5, 5, 135, 15);
        friendsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
	friendsPanel.add(friendsLabel);
        
        //add friends listfriendsSelect2
        friendsListModel = new DefaultListModel();
        this.friendsList.forEach((friend) -> {
            friendsListModel.addElement(friend);
        }); 
        friendsSelect = new JList(friendsListModel);//this.friendsList.toArray()
        friendsSelect.setMaximumSize(new Dimension( this.getWidth()/2, this.getHeight() ));
        friendsSelect.addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting()) {
                System.out.println("Wybrano znajomego - "+friendsSelect.getSelectedValue().toString());
                loadHistory(Integer.parseInt(friendsSelect.getSelectedValue().toString()));
            }
        });
	friendsPanel.add(friendsSelect);
        
        //add new friends panel
        newFriendText = new JTextField(1);
        newFriendText.setMaximumSize(new Dimension( this.getWidth()/4, 30 ));
	friendsPanel.add(newFriendText);
        
        friendsPanel.add(newFriendLabel);
        
        //przycisk
        newFriendBtn = new JButton("Dodaj znajomego");
	friendsPanel.add(newFriendBtn);
	newFriendBtn.addActionListener((ActionEvent e) -> {
            addFriend();
        });
        
        //center elements
        DefaultListCellRenderer renderer =  (DefaultListCellRenderer)friendsSelect.getCellRenderer();  
        renderer.setHorizontalAlignment(JLabel.CENTER); 
        
        //add to main window
        container.add(friendsPanel);
    }
    
    private void loadHistory(int friendId) {
        historyArea.setText("");
        if(!messageMap.containsKey(friendId)) return;
        ArrayList<Message> temp = messageMap.get(friendId);
        temp.forEach((msg) -> {
            historyArea.setText(historyArea.getText()+msg.toLabel()+'\n');
        });
    }
    
    private void addFriend() {
        String newFriendFieldContent = newFriendText.getText();
        newFriendLabel.setForeground(Color.red);
        if(!newFriendFieldContent.chars().allMatch( Character::isDigit )) { 
            newFriendLabel.setText("Podane ID jest w złym nieprawidłowym formacie");
        } else {
            String friendId = newFriendFieldContent;
            if(Integer.parseInt(friendId) == this.getUserID()) newFriendLabel.setText("Podano własne ID");
            else if(friendsList.contains(friendId)) newFriendLabel.setText("Podany znajomy istnieje już w Twojej liscie");
            else this.sendMessageToServer(MessageCode.SEND_MESSAGE, friendId);
        }
    }
    
    private void addToTxt(String messagge, String fileName) {
        try {
            Files.write(Paths.get(fileName), ('\n'+messagge).getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
    
    private void addFriendTOCombobox(String friend) {
        friendsListModel.addElement(friend);
    }
    
    private void sendMessage() {
        String strMessage = messageArea.getText();
        String friendId = friendsSelect.getSelectedValue().toString();
        if("".equals(strMessage) || "".equals(friendId)) return;
        strMessage = strMessage.replace(';', ',');
        System.out.println("Wysyłana wiadomość - "+strMessage);
        
        Message message = new Message(friendId, MessageType.SENDED, new Date(), strMessage);
        sendMessageToServer(MessageCode.SEND_MESSAGE, message.toString());
    }
    
    private void addMessage(Message message) {
        int id = Integer.parseInt(message.getFriendId());
        if(messageMap.containsKey(id)) {
            messageMap.get(id).add(message);
        } else {
            ArrayList<Message> tempList = new ArrayList<>();
            tempList.add(message);
            messageMap.put(id, tempList);
        }
    }
    
    private void initialFriendsList(String fileName) {
        System.out.println("Pobieranie znajomych");
        if (new File(fileName).exists())
        {
            System.out.println("Znaleziono plik ze znajomymi");
            try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
            {
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    friendsList.add(currentLine);
                }
                System.out.println("Odczytano znajomych z pliku - " + friendsList);
            } catch (IOException e) {
                e.printStackTrace();
            } 
        } else {
            System.out.println("Nie znaleziono pliku ze znajomymi");
            this.createTxtFile("", this.getFriendsPath());
        }
    }
    
    private void initialMessageList(String fileName) {
        System.out.println("Pobieranie wiadomości");
        if (new File(fileName).exists())
        {
            System.out.println("Znaleziono plik z wiadomościami");
            try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
            {
                String currentLine;
                Message message;
                while ((currentLine = br.readLine()) != null) {
                    if("".equals(currentLine)) continue;
                    System.out.println(currentLine);
                    String[] strMessage = currentLine.split(";");
                    addMessage(new Message(strMessage));
                }
                System.out.println("Odczytano znajomych z pliku - " + friendsList);
            } catch (IOException e) {
                e.printStackTrace();
            } 
        } else {
            System.out.println("Nie znaleziono pliku z wiadomościami");
            this.createTxtFile("", this.getHistoryPath());
        }
    }

    /**
     * @return the idPath
     */
    public String getIdPath() {
        return idPath;
    }

    /**
     * @return the userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * @return the serverHostName
     */
    public String getServerHostName() {
        return serverHostName;
    }

    /**
     * @return the serverPortNumber
     */
    public int getServerPortNumber() {
        return serverPortNumber;
    }

    /**
     * @return the friendsPath
     */
    public String getFriendsPath() {
        return friendsPath;
    }

    /**
     * @return the historyPath
     */
    public String getHistoryPath() {
        return historyPath;
    }
    
}
