import java.awt.BorderLayout; 
import java.awt.Color; 
import java.awt.Dimension;
import java.awt.FlowLayout; 
import java.awt.GridLayout;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton; 
import javax.swing.JFrame; 
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class BattleshipUI extends JFrame implements ActionListener 
{

    // Chat 
    private Socket socket;
    private int port = 7777;
    private ObjectOutputStream outstream;
    
    //Set Display Size
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 700;

    int intBoardSize;
    String[] rowLabels = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"};
    
    //Initialize Panels
    JPanel buttonPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel playerBoardPanel = new JPanel();
    JPanel opponentBoardPanel = new JPanel();
    JPanel chatPanel = new JPanel();
    JPanel sendChatPanel = new JPanel();
    JTextField sendTextField = new JTextField();
    JButton sendButton = new JButton("Send");
    JTextField receiveTextField = new JTextField();
    JPanel receiveChatPanel = new JPanel();

    JButton boardSizeDialogButton;
    JButton startButton;
    JButton hitButton;
    JButton missButton;
    
    //Initialize Button Double Array
    private JButton[][] playerButtonArray;
    private JButton[][] opponentButtonArray;
    
    private Color opponentButtonColor = Color.lightGray;
    
    private boolean started = false;
    private boolean[][] boatCoordinates;
    
    public BattleshipUI() 
    {
        super("Battleship");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Layout Formatting
        setLayout(new BorderLayout());
        buttonPanel.setBackground(Color.lightGray);
        buttonPanel.setLayout(new FlowLayout());
        
        //Add Board Size Selection Button
        boardSizeDialogButton = new JButton("Board Size");
        boardSizeDialogButton.addActionListener(this);
        buttonPanel.add(boardSizeDialogButton);

        //Add Start Button
        startButton = new JButton("Start");
        startButton.setVisible(false);
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        
        //Add Hit Button
        hitButton = new JButton("Opponent Hit");
        hitButton.setVisible(false);
        hitButton.addActionListener(this);
        buttonPanel.add(hitButton);
        
        //Add Miss Button
        missButton = new JButton("Opponent Miss");
        missButton.setVisible(false);
        missButton.addActionListener(this);
        buttonPanel.add(missButton);
        
        
        chatPanel.setLayout(new GridLayout(1,2));
        chatPanel.setBackground(Color.blue);
        sendChatPanel.setLayout(new BorderLayout());
        receiveChatPanel.setLayout(new BorderLayout());
        
        sendChatPanel.add(new JLabel("Send Message:                          (Player)"), BorderLayout.NORTH);
        sendChatPanel.add(sendTextField, BorderLayout.CENTER);
        
        sendButton.addActionListener(this);
        sendChatPanel.add(sendButton, BorderLayout.SOUTH);
        
        receiveChatPanel.add(new JLabel("Message Received:                    (Opponent)"), BorderLayout.NORTH);
        receiveChatPanel.add(receiveTextField, BorderLayout.CENTER);

        chatPanel.add(sendChatPanel);
        chatPanel.add(receiveChatPanel);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.SOUTH);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        String buttonString = e.getActionCommand();
        String boardSize;

        if(buttonString.equals("Board Size"))
        {
            // Open Dialog Display to Enter Size
            boardSize = JOptionPane.showInputDialog(this, "Board Size: Max. 10", "10");
            System.out.println("boardSize = " + boardSize);
            
            // Set Board to Inputed Size
            if(boardSize != null)
            {
                //If Size Is Too Big, Return and Do Not Continue Code
                intBoardSize = Integer.parseInt(boardSize);
                if(intBoardSize > 10)
                {
                    return;
                }
                
                startButton.setVisible(true);
                
                // Add extra row and column for position label
                intBoardSize++;
                
                //Hide Size Setter Button - No Longer Needed
                boardSizeDialogButton.setVisible(false);
                
                // Set Layout of Button Board Grid
                playerBoardPanel.setLayout(new GridLayout(intBoardSize, intBoardSize));
                opponentBoardPanel.setLayout(new GridLayout(intBoardSize, intBoardSize));

                boardPanel.add(playerBoardPanel);
                boardPanel.add(opponentBoardPanel);

                initializeBoatCoordinates();
                
                //Set Button Double Array Size to Inputed Size and Add New Life Cell Button to Each Index
                playerButtonArray = new JButton[intBoardSize][intBoardSize];
                for(int i = 0; i < intBoardSize; i++) 
                {
                    for(int j = 0; j < intBoardSize; j++) 
                    {
                        JButton b = new JButton();
                        b.setPreferredSize(new Dimension(50, 50));
                        if(j == 0)
                        {
                            if(i != 0)
                                b.setText(Integer.toString(i));
                        }
                        if(i == 0)
                        {
                            if(j != 0)
                                b.setText(rowLabels[j-1]);
                        }
                        playerButtonArray[i][j] = b;
                        b.setBackground(Color.LIGHT_GRAY);
                        if(i != 0 && j != 0)
                        {
                            b.setActionCommand("p," + i + "," + j); // Indicate Which Button
                            b.addActionListener(this);                        
                        }
                        playerBoardPanel.add(b);
                    }            
                }
                
                opponentButtonArray = new JButton[intBoardSize][intBoardSize];
                for(int i = 0; i < intBoardSize; i++) 
                {
                    for(int j = 0; j < intBoardSize; j++) 
                    {
                        JButton b = new JButton();
                        b.setPreferredSize(new Dimension(50, 50));
                        if(j == 0)
                        {
                            if(i != 0)
                                b.setText(Integer.toString(i));
                        }
                        if(i == 0)
                        {
                            if(j != 0)
                                b.setText(rowLabels[j-1]);
                        }
                        opponentButtonArray[i][j] = b;
                        b.setBackground(Color.LIGHT_GRAY);
                        if(i != 0 && j != 0)
                        {
                            b.setActionCommand("o," + i + "," + j); // Indicate Which Button
                            b.addActionListener(this);                        
                        }
                        opponentBoardPanel.add(b);
                    }            
                }
            }
        }
        // When Start Button is Clicked
        else if(buttonString.equals("Start")) 
        {
            started = true;
            startButton.setVisible(false);
            hitButton.setVisible(true);
            missButton.setVisible(true);
            
            System.out.println("Start... ");
        }
        // When Hit Button is Clicked
        else if(buttonString.equals("Opponent Hit")) 
        {
            opponentButtonColor = Color.red;
            System.out.println("Opponent Hit... ");
        }
        // When Miss Button is Clicked
        else if(buttonString.equals("Opponent Miss")) 
        {
            opponentButtonColor = Color.white;
            System.out.println("Opponent Miss... ");
        }
        else if(buttonString.equals("Send")) 
        {
            try {
                outstream.writeObject(sendTextField.getText());
                sendTextField.setText("");
            } catch (IOException ex) {
                Logger.getLogger(BattleshipUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Send... ");
        }
        else {
            //Get Button Position Value From Action Command
            String[] parts = buttonString.split(",");
            String board = parts[0];
            int i = Integer.parseInt(parts[1]);
            int j = Integer.parseInt(parts[2]);
            
            if(board.equals("p"))
            {
                Color c = ((JButton)e.getSource()).getBackground();
                if(started == false)
                {
                    if(c != Color.cyan) 
                    {
                        playerButtonArray[i][j].setBackground(Color.cyan);
                        boatCoordinates[i][j] = true;
                    }
                    else 
                    {
                        playerButtonArray[i][j].setBackground(Color.LIGHT_GRAY);
                        boatCoordinates[i][j] = false;
                    }                            
                }
                else
                {
                    if(boatCoordinates[i][j] == true) 
                    {
                        if(c != Color.red)
                            playerButtonArray[i][j].setBackground(Color.red);
                        else
                            playerButtonArray[i][j].setBackground(Color.cyan);
                    }
                }
            }
            else if (board.equals("o"))
            {
                Color c = ((JButton)e.getSource()).getBackground();
                
                if(c == Color.red)
                    opponentButtonArray[i][j].setBackground(Color.LIGHT_GRAY);
                else if(c == Color.white)
                    opponentButtonArray[i][j].setBackground(Color.LIGHT_GRAY);
                else
                    opponentButtonArray[i][j].setBackground(opponentButtonColor);
            }

            System.out.println("Life Selection (i,j): " + buttonString);
        }
    }
    
    // Prepare Battleship Game Board
    private void initializeBoatCoordinates()
    {
            // Set Game Board Double Array to Inputed Size
            boolean[][] newBoatCoordinates = new boolean[intBoardSize][intBoardSize];
            for(int row = 0; row < newBoatCoordinates.length; row++ )
            {
                for(int col = 0; col < newBoatCoordinates[row].length; col++ )
                {
                    newBoatCoordinates[row][col] = false; //Initially Set no boats presence
                }
            }
            boatCoordinates = newBoatCoordinates;
    }
    
    // Chat Program begins here
    /**
     * First try as a client, if it fails then start as a client; Then create
     * two separate threads one for input and another for output
     */
    private void start() throws IOException {

        startClient();
        if (socket == null) {
            System.out.println("Client Failed -- Starting Server \n");
            startServer();
        }

        Thread instreamHandler = new Thread(
                new ChatInputStreamHandler(socket, receiveTextField));
        
        instreamHandler.start();
        
        outstream = new ObjectOutputStream(socket.getOutputStream());
    
    }

    /**
     * Start the cline on local host and port 7777
     */
    private void startClient() {

        try {
            // Create a connection to the server socket 
            InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host.getHostName(), 7777);
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host");
            System.out.println(e);
        } catch (IOException e) {
            System.out.println("No Server Found");
            System.out.println(e);
        }
    }

    /**
     * Start the server on port 7777
     */
    void startServer() {

        try {
            //Starting servr and waiting for client
            ServerSocket server = new ServerSocket(port);
            socket = server.accept();
        } catch (IOException e) {
            System.out.println("Error starting server");
            System.out.println(e);
        }
    }    
    
    
    
     public static void main(String[] args) throws InterruptedException, IOException {
        
        BattleshipUI g = new BattleshipUI();
        g.setVisible(true);
        g.start();
        
        
        
    }

    
}


/**
 * A class that creates a thread to handle input on the socket
 *
 * @author Thananjeyan
 */
class ChatInputStreamHandler implements Runnable {

    ObjectInputStream instream;
    Socket socket;
    JTextField receiveTextArea;

    public ChatInputStreamHandler(Socket socket, JTextField receiveTextField) {

        System.out.println("Creating InputStreamHandler");
        this.socket = socket;
        this.receiveTextArea = receiveTextField;
    }

    
    /*
     * Thread run method
     */    
    public void run() {

        try {
            instream = new ObjectInputStream(socket.getInputStream());

            while (true) {
                String message = (String) instream.readObject();
                receiveTextArea.setText(message + "\n");
            }
        } catch (IOException e) {
            System.out.println("ChatInputStreamHandler run - IOException");
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println("ChatInputStreamHandler run - ClassNotFoundException");
            System.out.println(e);
        }        
    }
}
