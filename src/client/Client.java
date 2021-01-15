package client;

//I'm a stupid comment just to test out if I get commited and pushed or not
// Stupid comment number 2
// stupid comment number 3
//b7bkooo 5 mwaah
//stupid comment number  4
// edit no 5
import com.google.gson.Gson;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import javax.swing.ListModel;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kandil
 */
public class Client extends javax.swing.JFrame {

    /**
     * Creates new form Client
     */
    
    CardLayout cl;
    Socket servSock;
    DataInputStream dis;
    PrintStream ps;
    Thread th = null;
    boolean serverIsOff = false; // server status
    boolean wishListFire= false; // wishlist integraty flag
    boolean usrIsLogged=false;
    boolean alreadyFriends = false; 
    Vector <ProdInfo> friendProducts;
    Vector friendRequests;
    int availableItemIndex=-1;
    int listMyWishIndex=-1;
    //String reply;
    static UserInfo myInfo;
    UserInfo data;
    
    public Client() {
        initComponents();
        
        //UserInfo data = new UserInfo();
        cl = (CardLayout)basePane.getLayout();
        connClient(); // connect client to server
        // initialize the HashMap

    }
        public void connClient() {
        try {
            servSock = new Socket("127.0.0.1", 5005);
            serverIsOff = false;
            
            dis = new DataInputStream(servSock.getInputStream());
            ps = new PrintStream(servSock.getOutputStream());
            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String msg = dis.readLine();
                            //reply = msg;
                            if (msg != null) {
                                
                                System.out.println(msg);
                                handleRepMsg(msg);
                            }
                            else {
                                servSock.close();
                                dis.close();
                                ps.close();
                                serverIsOff = true;
                                break;
                            }
                        } catch (SocketException e) {
                            try {
                                dis.close();
                                ps.close();
                                servSock.close();
                                serverIsOff = true;
                                if (th != null) {
                                    th.stop();
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            th.start();
        } catch (ConnectException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            serverIsOff = true;
            System.out.println("Server is off");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


        void handleRepMsg(String msg) {
        data = new Gson().fromJson(msg, UserInfo.class);
        switch (data.getType()) {
            case "log":
                repLogMsg(data);
                break;
            case "reg":
                repRegMsg(data);
                break;
            case "fWish":
                repfWishMsg(data);
                break;
            case "rmFriend":
                reprmFriend(data);
                break;
            case "friendRequest":
                repFriendRequest(data);
                break;
            case "FDialogfriendRequest":
                dialFUNFriendRequest(data);
                break;
            case "contribute":
                repContribute(data);
                break;
            case "typeAvailableItem":
                FunAvailableItem(data);
                break;
            case "typeRemoveItem":
                FunRemoveItem(data);
                break;
            case "updateCredit":
                repUpdateCredit(data);
                break;
            default:
            // code block
        }
    }
    
    void repLogMsg(UserInfo data) {
        if (data.getResult().equals("success")) { // move to next panel
            System.out.println("success");
            usrIsLogged = true;
            
            myInfo = data.copy();
            //System.out.println("My info after copy "+ myInfo.getUsrName());
            //Filling profile details
            txtUsrNameProfile.setText(myInfo.getUsrName());
            txtFullNameProfile.setText(myInfo.getFname()+ " " + myInfo.getLname());
            txtEmailProfile.setText(myInfo.getEmail());
            txtCreditProfile.setText(Integer.toString(myInfo.getCredit()));
            System.out.println("My UsrName data  "+data.getUsrName());
            System.out.println("My UsrName myInfo  "+myInfo.getUsrName());
            DefaultListModel temp = new DefaultListModel<>();
            
            //filling my wishlist
            DefaultListModel myWishList = new DefaultListModel<>();
            for(ProdInfo prod : myInfo.getWishList()){
            myWishList.addElement(prod.getName());
            System.out.println(prod.getName());
            
            }
            //System.out.println(myWishList);
            listMyWish.setModel(myWishList);
            
            
            temp.clear();
            //filling available products
            DefaultListModel availableProdsList = new DefaultListModel<>();
            for(ProdInfo prod : myInfo.getAvailableProds()){
            availableProdsList.addElement(prod.getName());
            }
            listAvailableItems.setModel(availableProdsList);
            
            //Filling friend list
            DefaultListModel friendsList = new DefaultListModel<>();
            for(Object friend : myInfo.getAprvFriends() ){
            friendsList.addElement(friend);
            }
            listFriends.setModel(friendsList);
            //temp.clear();
            
            //filling user's friend requests

            DefaultListModel myFriendRequests = new DefaultListModel<>();
            friendRequests = myInfo.getPendFriends();
            System.out.print(friendRequests);
            for (Object user : friendRequests){
            myFriendRequests.addElement(user + " send you friend request");
            }
            listFriendRequests.setModel(myFriendRequests);
            // Fill notification list
            
            DefaultListModel notificationList = new DefaultListModel<>();
            for(ProdInfo prod : myInfo.getCompletedProds()){
            Vector friends = new Vector(prod.getContributedFriends());
        
                notificationList.addElement("CONGRATULATIONS!! " + friends + " Got you a " + prod.getName());
                //System.out.println("Product name " + prod.getName());
            
            }
            
            for(ContrDetails contr : myInfo.getCompletedContributions()){
            System.out.println("WELL DONE!!  You got "+ contr.getFriendName() + " a " +contr.getProdName());
            notificationList.addElement("WELL DONE!!  You got "+ contr.getFriendName() + " a " +contr.getProdName());
            
            
            }
            listNotification.setModel(notificationList);
            
            // add credit
            myInfo.setCredit(data.getCredit());
            labelCredit.setText(Integer.toString(myInfo.getCredit()));
            
            
            cl.show(basePane, "card2");
        
        } else {
            System.out.println("Wrong user name or password"); // give dialog box as wrong usr or pw
            JOptionPane.showMessageDialog(this, "Invalid UserName or Password", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    void repRegMsg(UserInfo data) {
        if (data.getResult().equals("success")) { // move to next panel
            //repLogMsg(data);
            JOptionPane.showMessageDialog(this, "User Created successfuly");
            System.out.println("success");
        loginRegsPane.setSelectedIndex(0);
        
        txtRegFname.setText("");
        txtRegLname.setText("");
        txtRegUsr.setText("");
        txtRegPw.setText("");
        txtRegEmail.setText("");
        
        } else if("1".equals(data.getResult())){
                JOptionPane.showMessageDialog(this, "User Name already taken");
            
        }else {
            
            JOptionPane.showMessageDialog(this, "Invalid Inputs", "Registeration Error",JOptionPane.ERROR_MESSAGE);
            System.out.println("Error occure, please try again"); // error
        }
    }
    
    void repfWishMsg(UserInfo data){
        DefaultListModel friendWishList = new DefaultListModel<>();
        friendProducts = data.getWishList();
        System.out.print(friendProducts);
        for(ProdInfo prod : friendProducts){
        friendWishList.addElement(prod.getName());
        
        }
        
        System.out.print(data.getWishList());
        wishListFire = false;
        listFriendWish.setModel(friendWishList);
        wishListFire = true;
        
    }
    
    void reprmFriend(UserInfo data){
        if(data.getResult().equals("success")){
            System.out.println(myInfo.getAprvFriends());
            
            myInfo.getAprvFriends().removeElement(data.getFriendName());
            System.out.println(myInfo.getAprvFriends());
            
            DefaultListModel temp = new DefaultListModel();
            
            for(Object friend : myInfo.getAprvFriends()){
                temp.addElement(friend);
            }
            //System.out.println(myInfo);

            listFriends.setModel(temp);
            
            DefaultListModel temp2 = new DefaultListModel();
            listFriendWish.setModel(temp2);
            labelFriendWishList.setText("");
        }
        else{
        JOptionPane.showMessageDialog(this, "Failed to remove friend");
        }
        
    }
    
    void repFriendRequest(UserInfo data){
            if("success".equals(data.getResult())){
                JOptionPane.showMessageDialog(this, "Request Sent Successfuly");
            }
            // Unique constratiant error code is "1"
            else if("1".equals(data.getResult())){
                JOptionPane.showMessageDialog(this, "Request Already Sent");
            }
            // Null constratiant error code is "1400"
            else if("1400".equals(data.getResult())){
                JOptionPane.showMessageDialog(this, "User Doesn't exist");
            }
             else {
                JOptionPane.showMessageDialog(this, "Process failed");
            }
    }
    void dialFUNFriendRequest(UserInfo data){
            if("success".equals(data.getResult())&&data.getFlagFriendReq()==true){
                
                DialogFriendRequest.setVisible(false);
            //updating friend list to add the new friend   
            myInfo.setAprvFriends(data.getAprvFriends());
            DefaultListModel temp = new DefaultListModel();
            for(Object friend : myInfo.getAprvFriends()){
                temp.addElement(friend);
            }
                listFriends.setModel(temp);
                
            //updating freind requests list to remove the request
            
            myInfo.setPendFriends(data.getPendFriends());
            DefaultListModel temp2 = new DefaultListModel();
            for(Object friendReq : myInfo.getPendFriends()){
                temp2.addElement(friendReq + " wants to be your friend");
            }
                listFriendRequests.setModel(temp2);
            
            JOptionPane.showMessageDialog(this, "Friend Added Succfully");
            
            }
            else if("success".equals(data.getResult())&&data.getFlagFriendReq()==false){
                
                //updating freind requests list to remove the request
            myInfo.getPendFriends().removeElement(data.getFriendName());
            DefaultListModel temp2 = new DefaultListModel();
            for(Object friendReq : myInfo.getPendFriends()){
                temp2.addElement(friendReq);
            }
                listFriendRequests.setModel(temp2);
                JOptionPane.showMessageDialog(this, "Friend's Request Decline");
            }
    }
    
    void repContribute(UserInfo data) {
        if ("success".equals(data.getResult())) {
            int contributamount = Integer.parseInt(txtContributionAmountFI.getText().trim());
            int actual = data.getContribution().getActualAmount();
            
            System.out.print(actual);
            
            System.out.print(contributamount);
            
            if (actual == contributamount) {
                JOptionPane.showMessageDialog(this, "you contributed successfully ");
                txtContributionAmountFI.setText("");
                labelCredit.setText(Integer.toString(data.getCredit()));
            } else {
                JOptionPane.showMessageDialog(this, "you actually contributed with " + actual + " and your gift completed");

                txtContributionAmountFI.setText("");
                labelCredit.setText(Integer.toString(data.getCredit()));
            }

        } else {
            JOptionPane.showMessageDialog(this, "your contribution failed");

        }
    }
    
    void FunRemoveItem(UserInfo data){
            if("success".equals(data.getResult())){
            myInfo.getWishList().removeElementAt(listMyWishIndex);

            System.out.println(myInfo.getWishList());
            
            DefaultListModel temp = new DefaultListModel();
            
            for(ProdInfo prod : myInfo.getWishList()){
                temp.addElement(prod.getName());
            }
            //System.out.println(myInfo);
            listMyWish.setModel(temp);
            JOptionPane.showMessageDialog(this, "Item Removed Succfully");
            }
            else{
                JOptionPane.showMessageDialog(this, "please try again");
            }       
    }
    void FunAvailableItem(UserInfo data) {
        if (data.getResult().equals("success")) { 
            
            if("success".equals(data.getResult())){
            myInfo.getWishList().addElement(data.getNewProd());
           //(listMyWishIndex);
            DefaultListModel temp = new DefaultListModel();
            
            for(ProdInfo prod : myInfo.getWishList()){
                temp.addElement(prod.getName());
            }
            //System.out.println(myInfo);
            listMyWish.setModel(temp);
            JOptionPane.showMessageDialog(this, "Item Added Succfully");
            }
        } else {
            JOptionPane.showMessageDialog(this,"Can't choose this item"); // error
        }
    }
    
    
            void repUpdateCredit (UserInfo data) {
     if ("success".equals(data.getResult())) {
          //txtCreditProfile.setText(Integer.toString(data.getCredit()));
           myInfo.setCredit(data.getCredit());
           JOptionPane.showMessageDialog(this, "your credit is updated");

     } else {
            JOptionPane.showMessageDialog(this, "your update failed");

        }
    }
    
        /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DialogFriendItem = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        labelProdNameFI = new javax.swing.JLabel();
        btnContributeFI = new javax.swing.JButton();
        txtContributionAmountFI = new javax.swing.JTextField();
        scrollPaneProdDescFI = new javax.swing.JScrollPane();
        textPaneProdDescFI = new javax.swing.JTextPane();
        labelPriceFI = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        labelCredit = new javax.swing.JLabel();
        labelProdImgFI = new javax.swing.JLabel();
        DialogAvailableItem = new javax.swing.JDialog();
        jPanel7 = new javax.swing.JPanel();
        labelProdNameAI = new javax.swing.JLabel();
        btnAddAI = new javax.swing.JButton();
        scrollPaneProdDescAI = new javax.swing.JScrollPane();
        textPaneProdDescAI = new javax.swing.JTextPane();
        labelPriceAI = new javax.swing.JLabel();
        labelProdImgAI = new javax.swing.JLabel();
        DialogMyItem = new javax.swing.JDialog();
        jPanel8 = new javax.swing.JPanel();
        labelProdNameMI = new javax.swing.JLabel();
        scrollPaneProdDescMI = new javax.swing.JScrollPane();
        textPaneProdDescMI = new javax.swing.JTextPane();
        labelPriceMI = new javax.swing.JLabel();
        progBarMoney = new javax.swing.JProgressBar();
        labelProdImgMI = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        DialogFriendRequest = new javax.swing.JDialog();
        labelRequest = new javax.swing.JLabel();
        btnAccept = new javax.swing.JButton();
        btnDecline = new javax.swing.JButton();
        basePane = new javax.swing.JPanel();
        loginRegsPane = new javax.swing.JTabbedPane();
        loginTab = new javax.swing.JPanel();
        txtLogUsr = new javax.swing.JTextField();
        loginBtn = new javax.swing.JButton();
        userLabel = new javax.swing.JLabel();
        passwdLabel = new javax.swing.JLabel();
        txtLogPw = new javax.swing.JPasswordField();
        regsPane1 = new javax.swing.JPanel();
        txtRegFname = new javax.swing.JTextField();
        txtRegLname = new javax.swing.JTextField();
        txtRegUsr = new javax.swing.JTextField();
        txtRegEmail = new javax.swing.JTextField();
        txtRegPw = new javax.swing.JPasswordField();
        createBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        mainPane = new javax.swing.JTabbedPane();
        panelFriends = new javax.swing.JPanel();
        scrollPanelFriends = new javax.swing.JScrollPane();
        listFriends = new javax.swing.JList<>();
        scrollPanelFriendWish = new javax.swing.JScrollPane();
        listFriendWish = new javax.swing.JList<>();
        labelFriendList = new javax.swing.JLabel();
        labelFriendWishList = new javax.swing.JLabel();
        btnRemoveFriend = new javax.swing.JButton();
        btnSendRequest = new javax.swing.JButton();
        txtFriendName = new javax.swing.JTextField();
        labelNewFriend = new javax.swing.JLabel();
        panelMyWishList = new javax.swing.JPanel();
        labelMyWish = new javax.swing.JLabel();
        scrollPanelAvailableItems = new javax.swing.JScrollPane();
        listAvailableItems = new javax.swing.JList<>();
        labelAvailableItems = new javax.swing.JLabel();
        btnRemoveItem = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listMyWish = new javax.swing.JList<>();
        panelFriendRequests = new javax.swing.JPanel();
        scrollPanelFriendRequests = new javax.swing.JScrollPane();
        listFriendRequests = new javax.swing.JList<>();
        panelNotifications = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listNotification = new javax.swing.JList<>();
        panelProfile = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtUsrNameProfile = new javax.swing.JTextField();
        txtFullNameProfile = new javax.swing.JTextField();
        txtEmailProfile = new javax.swing.JTextField();
        txtCreditProfile = new javax.swing.JTextField();
        btnUpdateCredit = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        labelProdNameFI.setText("<product name>");

        btnContributeFI.setText("contribute");
        btnContributeFI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContributeFIActionPerformed(evt);
            }
        });

        textPaneProdDescFI.setEditable(false);
        textPaneProdDescFI.setMaximumSize(new java.awt.Dimension(80, 40));
        scrollPaneProdDescFI.setViewportView(textPaneProdDescFI);

        labelPriceFI.setText("<price>");

        jLabel6.setText("Your Credit Limit is  ");

        labelProdImgFI.setMaximumSize(new java.awt.Dimension(500, 500));
        labelProdImgFI.setPreferredSize(new java.awt.Dimension(350, 350));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(txtContributionAmountFI)
                        .addGap(18, 18, 18)
                        .addComponent(btnContributeFI)
                        .addGap(115, 115, 115))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(labelProdNameFI)
                                .addGap(93, 93, 93)
                                .addComponent(labelPriceFI))
                            .addComponent(scrollPaneProdDescFI, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelCredit)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelProdImgFI, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(58, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelProdNameFI)
                            .addComponent(labelPriceFI))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollPaneProdDescFI, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(labelCredit))
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addComponent(labelProdImgFI, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnContributeFI)
                    .addComponent(txtContributionAmountFI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout DialogFriendItemLayout = new javax.swing.GroupLayout(DialogFriendItem.getContentPane());
        DialogFriendItem.getContentPane().setLayout(DialogFriendItemLayout);
        DialogFriendItemLayout.setHorizontalGroup(
            DialogFriendItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DialogFriendItemLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        DialogFriendItemLayout.setVerticalGroup(
            DialogFriendItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        labelProdNameAI.setText("<product name>");

        btnAddAI.setText("Add To My List");
        btnAddAI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAIActionPerformed(evt);
            }
        });

        textPaneProdDescAI.setEditable(false);
        scrollPaneProdDescAI.setViewportView(textPaneProdDescAI);

        labelPriceAI.setText("<price>");

        labelProdImgAI.setMaximumSize(new java.awt.Dimension(500, 500));
        labelProdImgAI.setPreferredSize(new java.awt.Dimension(350, 350));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(labelProdNameAI)
                                .addGap(93, 93, 93)
                                .addComponent(labelPriceAI))
                            .addComponent(scrollPaneProdDescAI, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelProdImgAI, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(btnAddAI)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelProdNameAI)
                            .addComponent(labelPriceAI))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrollPaneProdDescAI, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelProdImgAI, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnAddAI)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout DialogAvailableItemLayout = new javax.swing.GroupLayout(DialogAvailableItem.getContentPane());
        DialogAvailableItem.getContentPane().setLayout(DialogAvailableItemLayout);
        DialogAvailableItemLayout.setHorizontalGroup(
            DialogAvailableItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        DialogAvailableItemLayout.setVerticalGroup(
            DialogAvailableItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DialogAvailableItemLayout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 35, Short.MAX_VALUE))
        );

        labelProdNameMI.setText("<product name>");

        textPaneProdDescMI.setEditable(false);
        scrollPaneProdDescMI.setViewportView(textPaneProdDescMI);

        labelPriceMI.setText("<price>");

        labelProdImgMI.setMaximumSize(new java.awt.Dimension(500, 500));
        labelProdImgMI.setPreferredSize(new java.awt.Dimension(350, 350));

        jLabel11.setText("Contributions so far");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(labelProdNameMI)
                                .addGap(93, 93, 93)
                                .addComponent(labelPriceMI))
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(progBarMoney, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                .addComponent(scrollPaneProdDescMI)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(labelProdImgMI, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelProdNameMI)
                            .addComponent(labelPriceMI))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrollPaneProdDescMI, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(labelProdImgMI, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progBarMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        javax.swing.GroupLayout DialogMyItemLayout = new javax.swing.GroupLayout(DialogMyItem.getContentPane());
        DialogMyItem.getContentPane().setLayout(DialogMyItemLayout);
        DialogMyItemLayout.setHorizontalGroup(
            DialogMyItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        DialogMyItemLayout.setVerticalGroup(
            DialogMyItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        labelRequest.setText("<Folan> Wants to be your friend");

        btnAccept.setText("Accept");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
            }
        });

        btnDecline.setText("Decline");
        btnDecline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeclineActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout DialogFriendRequestLayout = new javax.swing.GroupLayout(DialogFriendRequest.getContentPane());
        DialogFriendRequest.getContentPane().setLayout(DialogFriendRequestLayout);
        DialogFriendRequestLayout.setHorizontalGroup(
            DialogFriendRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DialogFriendRequestLayout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addComponent(btnAccept)
                .addGap(46, 46, 46)
                .addComponent(btnDecline)
                .addGap(123, 123, 123))
            .addGroup(DialogFriendRequestLayout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addComponent(labelRequest)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        DialogFriendRequestLayout.setVerticalGroup(
            DialogFriendRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DialogFriendRequestLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(labelRequest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(DialogFriendRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAccept)
                    .addComponent(btnDecline))
                .addContainerGap(222, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        basePane.setMinimumSize(new java.awt.Dimension(100, 100));
        basePane.setLayout(new java.awt.CardLayout());

        loginRegsPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                loginRegsPaneStateChanged(evt);
            }
        });

        txtLogUsr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLogUsrActionPerformed(evt);
            }
        });

        loginBtn.setText("Login");
        loginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginBtnActionPerformed(evt);
            }
        });

        userLabel.setText("UserName:");

        passwdLabel.setText("Password");

        txtLogPw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtLogPwKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout loginTabLayout = new javax.swing.GroupLayout(loginTab);
        loginTab.setLayout(loginTabLayout);
        loginTabLayout.setHorizontalGroup(
            loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginTabLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userLabel)
                    .addComponent(passwdLabel))
                .addGap(24, 24, 24)
                .addGroup(loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loginBtn)
                    .addComponent(txtLogUsr, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(txtLogPw))
                .addContainerGap(218, Short.MAX_VALUE))
        );
        loginTabLayout.setVerticalGroup(
            loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginTabLayout.createSequentialGroup()
                .addContainerGap(157, Short.MAX_VALUE)
                .addGroup(loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLogUsr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addGap(26, 26, 26)
                .addGroup(loginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwdLabel)
                    .addComponent(txtLogPw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addComponent(loginBtn)
                .addGap(70, 70, 70))
        );

        loginRegsPane.addTab("Login", loginTab);

        txtRegFname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRegFnameActionPerformed(evt);
            }
        });

        createBtn.setText("Create Account");
        createBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBtnActionPerformed(evt);
            }
        });

        clearBtn.setText("Clear All");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("First Name:");

        jLabel2.setText("Last Name:");

        jLabel3.setText("Username:");

        jLabel4.setText("Email");

        jLabel5.setText("Password");

        javax.swing.GroupLayout regsPane1Layout = new javax.swing.GroupLayout(regsPane1);
        regsPane1.setLayout(regsPane1Layout);
        regsPane1Layout.setHorizontalGroup(
            regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(regsPane1Layout.createSequentialGroup()
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(regsPane1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(regsPane1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                                .addComponent(txtRegFname, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(regsPane1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtRegLname, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(regsPane1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtRegUsr, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(regsPane1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(39, 39, 39)
                                .addComponent(txtRegPw))
                            .addGroup(regsPane1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtRegEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(createBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearBtn)
                .addGap(116, 116, 116))
        );
        regsPane1Layout.setVerticalGroup(
            regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(regsPane1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRegFname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRegLname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRegUsr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRegEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRegPw, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(regsPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createBtn)
                    .addComponent(clearBtn))
                .addContainerGap(129, Short.MAX_VALUE))
        );

        loginRegsPane.addTab("Registration", regsPane1);

        basePane.add(loginRegsPane, "card5");

        mainPane.setPreferredSize(new java.awt.Dimension(400, 400));
        mainPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainPaneStateChanged(evt);
            }
        });
        mainPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                mainPaneComponentShown(evt);
            }
        });

        listFriends.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listFriendsMouseClicked(evt);
            }
        });
        listFriends.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFriendsValueChanged(evt);
            }
        });
        scrollPanelFriends.setViewportView(listFriends);

        listFriendWish.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listFriendWishMouseClicked(evt);
            }
        });
        listFriendWish.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFriendWishValueChanged(evt);
            }
        });
        scrollPanelFriendWish.setViewportView(listFriendWish);

        labelFriendList.setText("Friend List");

        btnRemoveFriend.setText("Remove");
        btnRemoveFriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveFriendActionPerformed(evt);
            }
        });

        btnSendRequest.setText("Send Request");
        btnSendRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendRequestActionPerformed(evt);
            }
        });

        txtFriendName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFriendNameActionPerformed(evt);
            }
        });

        labelNewFriend.setText("Add new friend");

        javax.swing.GroupLayout panelFriendsLayout = new javax.swing.GroupLayout(panelFriends);
        panelFriends.setLayout(panelFriendsLayout);
        panelFriendsLayout.setHorizontalGroup(
            panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFriendsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnRemoveFriend)
                    .addComponent(labelNewFriend)
                    .addComponent(btnSendRequest)
                    .addGroup(panelFriendsLayout.createSequentialGroup()
                        .addGroup(panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtFriendName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollPanelFriends, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .addComponent(labelFriendList, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(107, 107, 107)
                        .addGroup(panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelFriendWishList)
                            .addComponent(scrollPanelFriendWish, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        panelFriendsLayout.setVerticalGroup(
            panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFriendsLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFriendList)
                    .addComponent(labelFriendWishList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFriendsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollPanelFriendWish, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                    .addComponent(scrollPanelFriends))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRemoveFriend)
                .addGap(18, 18, 18)
                .addComponent(labelNewFriend)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtFriendName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSendRequest)
                .addContainerGap())
        );

        mainPane.addTab("Friends", panelFriends);
        panelFriends.getAccessibleContext().setAccessibleName("friends");
        panelFriends.getAccessibleContext().setAccessibleDescription("");

        labelMyWish.setText("My Wish List");

        listAvailableItems.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listAvailableItemsMouseClicked(evt);
            }
        });
        listAvailableItems.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listAvailableItemsValueChanged(evt);
            }
        });
        scrollPanelAvailableItems.setViewportView(listAvailableItems);

        labelAvailableItems.setText("Avaiable Items");

        btnRemoveItem.setText("Remove ");
        btnRemoveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveItemActionPerformed(evt);
            }
        });

        listMyWish.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMyWishMouseClicked(evt);
            }
        });
        listMyWish.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listMyWishValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listMyWish);

        javax.swing.GroupLayout panelMyWishListLayout = new javax.swing.GroupLayout(panelMyWishList);
        panelMyWishList.setLayout(panelMyWishListLayout);
        panelMyWishListLayout.setHorizontalGroup(
            panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMyWishListLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelMyWish)
                    .addComponent(btnRemoveItem)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelAvailableItems)
                    .addComponent(scrollPanelAvailableItems, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        panelMyWishListLayout.setVerticalGroup(
            panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMyWishListLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMyWish)
                    .addComponent(labelAvailableItems))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMyWishListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMyWishListLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemoveItem))
                    .addComponent(scrollPanelAvailableItems, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );

        mainPane.addTab("My Wishlist", panelMyWishList);

        listFriendRequests.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listFriendRequestsMouseClicked(evt);
            }
        });
        listFriendRequests.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFriendRequestsValueChanged(evt);
            }
        });
        scrollPanelFriendRequests.setViewportView(listFriendRequests);

        javax.swing.GroupLayout panelFriendRequestsLayout = new javax.swing.GroupLayout(panelFriendRequests);
        panelFriendRequests.setLayout(panelFriendRequestsLayout);
        panelFriendRequestsLayout.setHorizontalGroup(
            panelFriendRequestsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPanelFriendRequests, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        panelFriendRequestsLayout.setVerticalGroup(
            panelFriendRequestsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPanelFriendRequests, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
        );

        mainPane.addTab("Friend Requests", panelFriendRequests);

        listNotification.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listNotificationValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listNotification);

        javax.swing.GroupLayout panelNotificationsLayout = new javax.swing.GroupLayout(panelNotifications);
        panelNotifications.setLayout(panelNotificationsLayout);
        panelNotificationsLayout.setHorizontalGroup(
            panelNotificationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        panelNotificationsLayout.setVerticalGroup(
            panelNotificationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
        );

        mainPane.addTab("Gifts Notifications", panelNotifications);

        jLabel7.setBackground(new java.awt.Color(153, 255, 255));
        jLabel7.setText("User Name: ");

        jLabel8.setText("Full Name:");

        jLabel9.setText("Email");

        jLabel10.setText("Credit:");

        txtUsrNameProfile.setEditable(false);
        txtUsrNameProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsrNameProfileActionPerformed(evt);
            }
        });

        txtFullNameProfile.setEditable(false);

        txtEmailProfile.setEditable(false);

        btnUpdateCredit.setText("Update credit");
        btnUpdateCredit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateCreditActionPerformed(evt);
            }
        });

        jButton1.setText("Log out");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelProfileLayout = new javax.swing.GroupLayout(panelProfile);
        panelProfile.setLayout(panelProfileLayout);
        panelProfileLayout.setHorizontalGroup(
            panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnUpdateCredit)
                    .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtUsrNameProfile)
                        .addComponent(txtFullNameProfile)
                        .addComponent(txtEmailProfile)
                        .addComponent(txtCreditProfile, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)))
                .addContainerGap(201, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelProfileLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(41, 41, 41))
        );
        panelProfileLayout.setVerticalGroup(
            panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProfileLayout.createSequentialGroup()
                .addGap(115, 115, 115)
                .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelProfileLayout.createSequentialGroup()
                        .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(txtUsrNameProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtFullNameProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addComponent(jLabel9))
                    .addComponent(txtEmailProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtCreditProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnUpdateCredit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        mainPane.addTab("Profile", panelProfile);

        basePane.add(mainPane, "card2");
        mainPane.getAccessibleContext().setAccessibleName("Friends");

        getContentPane().add(basePane, "card4");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRemoveFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveFriendActionPerformed
        // TODO add your handling code here:
        // prepare obj
        //data = new UserInfo();
        data.setUsrName(myInfo.getUsrName());
        data.setFriendName(listFriends.getSelectedValue());
        data.setType("rmFriend");
        // convert to Json
        String msg = new Gson().toJson(data);
        
        //Send to server
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
    }//GEN-LAST:event_btnRemoveFriendActionPerformed

    private void listFriendsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFriendsValueChanged
    
        
    }//GEN-LAST:event_listFriendsValueChanged

    private void listFriendWishValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFriendWishValueChanged
       
    }//GEN-LAST:event_listFriendWishValueChanged

    private void loginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginBtnActionPerformed
        // TODO add your handling code here:
        // code to connect to server and request login service
        // Authentication done at the server
        
        // prepare obj for log
        data = new UserInfo();
        data.setType("log");
        data.setUsrName(txtLogUsr.getText().trim());
        data.setPw(txtLogPw.getText().trim());
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }

    
        
    }//GEN-LAST:event_loginBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        // TODO add your handling code here:
        txtRegFname.setText("");
        txtRegLname.setText("");
        txtRegUsr.setText("");
        txtRegPw.setText("");
        txtRegEmail.setText("");

    }//GEN-LAST:event_clearBtnActionPerformed

    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed
        // TODO add your handling code here:
        // prepare obj for reg
        if(txtRegUsr.getText().isEmpty()==true||txtRegPw.getText().isEmpty()==true||txtRegEmail.getText().isEmpty()==true||
               txtRegFname.getText().isEmpty()==true|| txtRegLname.getText().isEmpty()==true)
        {
            JOptionPane.showMessageDialog(this, "Please Enter Correct Value");
        }
        else
        {
            data = new UserInfo();
            data.setUsrName(txtRegUsr.getText().trim());
            data.setPw(txtRegPw.getText().trim());
            data.setEmail(txtRegEmail.getText().trim());
            data.setFname(txtRegFname.getText().trim());
            data.setLname(txtRegLname.getText().trim());
            data.setType("reg");
            // obj to json
            String msg = new Gson().toJson(data);
            // send if server is on
            if (serverIsOff == true) {
                connClient();
                if (serverIsOff == false) {
                    ps.println(msg);
                    ps.flush();
                }
            } else {
                ps.println(msg);
                ps.flush();
            }
        }
    }//GEN-LAST:event_createBtnActionPerformed

    private void btnContributeFIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContributeFIActionPerformed
        // TODO add your handling code here:
        if(txtContributionAmountFI.getText().isEmpty() ){
             JOptionPane.showMessageDialog(this, "set your contribution first", "Error", JOptionPane.ERROR_MESSAGE);
        }else {
             int credit = Integer.parseInt(labelCredit.getText().trim());

             int Amountcontribute = Integer.parseInt(txtContributionAmountFI.getText().trim());
               if (Amountcontribute < credit) {
                       data.setType("contribute");
                       System.out.println(myInfo.getUsrName());
                        data.setUsrName(myInfo.getUsrName());

                        ContrDetails contribution = new ContrDetails();

                         contribution.setContrAmount(Amountcontribute);
                         contribution.setProdName(labelProdNameFI.getText());
                         contribution.setFriendName(listFriends.getSelectedValue());
                          data.setContribution(contribution);
                          System.out.println(data.getContribution().getFriendName());

                           // obj to json
                           String msg = new Gson().toJson(data);

                        // send if server is on
                           if (serverIsOff == true) {
                            connClient();
                               if (serverIsOff == false) {
                                ps.println(msg);
                                ps.flush();
                                }
                                 } else {
                                    System.out.println(msg);
                                    ps.println(msg);
                                    ps.flush();
                              }                 
               }
         else {
            JOptionPane.showMessageDialog(this, "you don't have enough credit", "Error", JOptionPane.ERROR_MESSAGE);
        
   
}
                
        }
    }//GEN-LAST:event_btnContributeFIActionPerformed

    private void txtFriendNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFriendNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFriendNameActionPerformed

    private void txtRegFnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRegFnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRegFnameActionPerformed

    private void btnSendRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendRequestActionPerformed
        // TODO add your handling code here:
        String requestedFriend = txtFriendName.getText().trim();
        // checking if friend already exists
        for(Object friend : myInfo.getAprvFriends()){
            if(friend.equals(requestedFriend)) {
                alreadyFriends = true;
            } 
        }
        // prepare obj for log
        if(alreadyFriends){
        JOptionPane.showMessageDialog(this, "You'r Already Friends");
        }
        else{
        data = new UserInfo();
        data.setUsrName(myInfo.getUsrName());
        data.setType("friendRequest");
        data.setFriendName(txtFriendName.getText().trim());
        
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
        
        }
    }//GEN-LAST:event_btnSendRequestActionPerformed

    private void listFriendRequestsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFriendRequestsValueChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_listFriendRequestsValueChanged

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed
        // TODO add your handling code here:
        data.setFlagFriendReq(true);
        data.setType("FDialogfriendRequest");
        data.setFriendName(myInfo.getPendFriends().elementAt(listFriendRequests.getSelectedIndex()) );
        
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
    }//GEN-LAST:event_btnAcceptActionPerformed

    private void btnDeclineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeclineActionPerformed
        // TODO add your handling code here:
        data.setFlagFriendReq(false);
        data.setType("FDialogfriendRequest");
        
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
    }//GEN-LAST:event_btnDeclineActionPerformed

    private void listNotificationValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listNotificationValueChanged
        // TODO add your handling code here:
       
    }//GEN-LAST:event_listNotificationValueChanged

    private void btnAddAIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAIActionPerformed
        // TODO add your handling code here:
        data.setNewProd(data.getAvailableProds().elementAt(availableItemIndex));
        
        data.setType("typeAvailableItem");
        
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
    }//GEN-LAST:event_btnAddAIActionPerformed

    private void listAvailableItemsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listAvailableItemsValueChanged
        // TODO add your handling code here:
        
        
        
    }//GEN-LAST:event_listAvailableItemsValueChanged

    private void listMyWishValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listMyWishValueChanged
        // TODO add your handling code here:
        
        
    }//GEN-LAST:event_listMyWishValueChanged

    private void btnRemoveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveItemActionPerformed
        // TODO add your handling code here:
        data.setRemoveItem((String) data.getWishList().elementAt(listMyWishIndex).getName());
        System.out.println("item will removed =  "+  data.getRemoveItem());
        data.setType("typeRemoveItem");
        
        // obj to json
        String msg = new Gson().toJson(data);
        
        // send if server is on
        if(serverIsOff == true) {
            connClient();
            if(serverIsOff == false) {
                ps.println(msg);
                ps.flush();
                }
        }
        else {
            System.out.println(msg);
            ps.println(msg);
            ps.flush();
        }
    }//GEN-LAST:event_btnRemoveItemActionPerformed

    private void txtLogUsrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLogUsrActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLogUsrActionPerformed

    private void txtUsrNameProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsrNameProfileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUsrNameProfileActionPerformed

    private void btnUpdateCreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateCreditActionPerformed
        // TODO add your handling code here:
      int  updateCredit = Integer.parseInt(txtCreditProfile.getText().trim());
      
      data = new UserInfo();
      data.setCredit(updateCredit);
      data.setUsrName(myInfo.getUsrName());
      data.setType("updateCredit");
      System.out.print(data.getCredit());
    //System.out.print(data.getupdateCredit());
      String msg = new Gson().toJson(data);

            // send if server is on
            if (serverIsOff == true) {
                connClient();
                if (serverIsOff == false) {
                    ps.println(msg);
                    ps.flush();
                }
            } else {
                System.out.println(msg);
                ps.println(msg);
                ps.flush();
            }
    }//GEN-LAST:event_btnUpdateCreditActionPerformed

    private void mainPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainPaneComponentShown
        // TODO add your handling code here:
       
    }//GEN-LAST:event_mainPaneComponentShown

    private void mainPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainPaneStateChanged
        // TODO add your handling code here:
        //if (mainPane.getSelectedIndex() >= 0 && mainPane.getSelectedIndex() <= 4) {
            if (usrIsLogged == true) {
                // prepare object
                data = new UserInfo();
                data.setType("log");
                data.setUsrName(myInfo.getUsrName());
                data.setPw(myInfo.getPw());
                // obj to json
                String msg = new Gson().toJson(data);

                // send if server is on
                if (serverIsOff == true) {
                    connClient();
                    if (serverIsOff == false) {
                        ps.println(msg);
                        ps.flush();
                    }
                } else {
                    //System.out.println(msg);
                    ps.println(msg);
                    ps.flush();
                }
            }
       // }
    }//GEN-LAST:event_mainPaneStateChanged

    private void txtLogPwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLogPwKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            loginBtn.doClick();
        }
    }//GEN-LAST:event_txtLogPwKeyPressed

    private void loginRegsPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_loginRegsPaneStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_loginRegsPaneStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            dis.close();
            ps.close();
            servSock.close();
            usrIsLogged = false;
            System.out.println("you are logged out");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        cl.show(basePane, "card5");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void listFriendsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFriendsMouseClicked
        // TODO add your handling code here:
        String friendName = listFriends.getSelectedValue();
        //System.out.println("Friend name outside if---------------- " + friendName);
        if (friendName != null) {
            System.out.println("Friend name inside if---------------- " + friendName);
            data = new UserInfo();
            data.setUsrName(friendName);
            data.setType("fWish");
            
            //edit labels
            labelFriendWishList.setText(friendName + " wants");

            // obj to json
            String msg = new Gson().toJson(data);

            // send if server is on
            if (serverIsOff == true) {
                connClient();
                if (serverIsOff == false) {
                    ps.println(msg);
                    ps.flush();
                }
            } else {
                //System.out.println(msg);
                ps.println(msg);
                ps.flush();
            }
        }
        
    }//GEN-LAST:event_listFriendsMouseClicked

    private void listFriendWishMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFriendWishMouseClicked
        // TODO add your handling code here:
        if(wishListFire){
        // TODO add your handling code here:
        String prodName = listFriends.getSelectedValue();
         // fill the comonents of dialogbox with the details of (prodName)
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int XPOSITION = (screenSize.width - DialogFriendItem.getWidth())/4 ;
        final int YPOSITION  = (screenSize.height - DialogFriendItem.getHeight())/4 ;
        
        // fill the components of dialogbox with the details of (prodName)
        
        int itemIndex = listFriendWish.getSelectedIndex();
        System.out.println("listFriendWish"+listFriendWish.getModel());
        System.out.println("friendProducts" + friendProducts);
        
        labelProdNameFI.setText(friendProducts.elementAt(itemIndex).getName()); 
        labelPriceFI.setText(Integer.toString(friendProducts.elementAt(itemIndex).getPrice()));
        textPaneProdDescFI.setText(friendProducts.elementAt(itemIndex).getDesc());
        labelProdImgFI.setSize(150, 150);
        ImageIcon imageIcon = new ImageIcon(new ImageIcon(data.getWishList().elementAt(listFriendWish.getSelectedIndex()).getImg()).getImage().getScaledInstance(labelProdImgFI.getWidth(), labelProdImgFI.getHeight(), Image.SCALE_DEFAULT));
        labelProdImgFI.setIcon(imageIcon);
        System.out.println(friendProducts.elementAt(itemIndex).getName());
        
        DialogFriendItem.setLocation(XPOSITION, YPOSITION);
        DialogFriendItem.setSize(500, 300);
        DialogFriendItem.show();
       }
    }//GEN-LAST:event_listFriendWishMouseClicked

    private void listMyWishMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMyWishMouseClicked
        // TODO add your handling code here:
        listMyWishIndex=listMyWish.getSelectedIndex();
        if(listMyWish.getSelectedValue() != null){ 
        int percentage = myInfo.getWishList().elementAt(listMyWishIndex).getPaid()*100/myInfo.getWishList().elementAt(listMyWishIndex).getPrice();
           
        labelProdNameMI.setText((String) myInfo.getWishList().elementAt(listMyWishIndex).getName()); 
            labelPriceMI.setText(Integer.toString(myInfo.getWishList().elementAt(listMyWishIndex).getPrice()) );
            textPaneProdDescAI.setText((String) myInfo.getWishList().elementAt(listMyWishIndex).getDesc()); 
            System.out.println(percentage);
            progBarMoney.setStringPainted(true);
            progBarMoney.setValue(percentage);
            labelProdImgMI.setSize(150, 150);
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(myInfo.getWishList().elementAt(listMyWish.getSelectedIndex()).getImg()).getImage().getScaledInstance(labelProdImgMI.getWidth(), labelProdImgMI.getHeight(), Image.SCALE_DEFAULT));
            labelProdImgMI.setIcon(imageIcon);
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Dimension screenSize = toolkit.getScreenSize();
            final int XPOSITION = (screenSize.width - DialogMyItem.getWidth())/4 ;
            final int YPOSITION  = (screenSize.height - DialogMyItem.getHeight())/4 ;
            DialogMyItem.setLocation(XPOSITION, YPOSITION);
            DialogMyItem.setSize(500, 300);
            DialogMyItem.show();
        }
        

    }//GEN-LAST:event_listMyWishMouseClicked

    private void listAvailableItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAvailableItemsMouseClicked
        // TODO add your handling code here:
        availableItemIndex = listAvailableItems.getSelectedIndex();
         
        if(myInfo.getAvailableProds().elementAt(availableItemIndex).getQty()==0)
        {
            JOptionPane.showMessageDialog(this, "This Item is Out Of Stock");
        }else
        {
            labelProdNameAI.setText((String) myInfo.getAvailableProds().elementAt(availableItemIndex).getName()); 
            labelPriceAI.setText(Integer.toString(myInfo.getAvailableProds().elementAt(availableItemIndex).getPrice()) );
            textPaneProdDescAI.setText((String) myInfo.getAvailableProds().elementAt(availableItemIndex).getDesc()); 
            labelProdImgAI.setSize(150, 150);
            
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(myInfo.getAvailableProds().elementAt(listAvailableItems.getSelectedIndex()).getImg()).getImage().getScaledInstance(labelProdImgAI.getWidth(), labelProdImgAI.getHeight(), Image.SCALE_DEFAULT));
            labelProdImgAI.setIcon(imageIcon);
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Dimension screenSize = toolkit.getScreenSize();
            final int XPOSITION = (screenSize.width - DialogAvailableItem.getWidth())/4 ;
            final int YPOSITION  = (screenSize.height - DialogAvailableItem.getHeight())/4 ;
            DialogAvailableItem.setLocation(XPOSITION, YPOSITION);
            DialogAvailableItem.setSize(500, 300);
            DialogAvailableItem.show();
            
        }
    }//GEN-LAST:event_listAvailableItemsMouseClicked

    private void listFriendRequestsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFriendRequestsMouseClicked
        // TODO add your handling code here:
        if(listFriendRequests.getSelectedValue() !=null){
        String freqName = listFriendRequests.getSelectedValue();
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int XPOSITION = (screenSize.width - DialogFriendRequest.getWidth())/4 ;
        final int YPOSITION  = (screenSize.height - DialogFriendRequest.getHeight())/4 ;
        
        
       int itemIndex = listFriendRequests.getSelectedIndex();
        data.setFriendName((String) friendRequests.elementAt(itemIndex));
        //System.out.println(data.getfriendreq());
        labelRequest.setText((String) friendRequests.elementAt(itemIndex)+"  Wants to be your friend"); 
        
        DialogFriendRequest.setLocation(XPOSITION, YPOSITION);
        DialogFriendRequest.setSize(300, 300);
        DialogFriendRequest.show();
        }
    }//GEN-LAST:event_listFriendRequestsMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog DialogAvailableItem;
    private javax.swing.JDialog DialogFriendItem;
    private javax.swing.JDialog DialogFriendRequest;
    private javax.swing.JDialog DialogMyItem;
    private javax.swing.JPanel basePane;
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnAddAI;
    private javax.swing.JButton btnContributeFI;
    private javax.swing.JButton btnDecline;
    private javax.swing.JButton btnRemoveFriend;
    private javax.swing.JButton btnRemoveItem;
    private javax.swing.JButton btnSendRequest;
    private javax.swing.JButton btnUpdateCredit;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton createBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelAvailableItems;
    private javax.swing.JLabel labelCredit;
    private javax.swing.JLabel labelFriendList;
    private javax.swing.JLabel labelFriendWishList;
    private javax.swing.JLabel labelMyWish;
    private javax.swing.JLabel labelNewFriend;
    private javax.swing.JLabel labelPriceAI;
    private javax.swing.JLabel labelPriceFI;
    private javax.swing.JLabel labelPriceMI;
    private javax.swing.JLabel labelProdImgAI;
    private javax.swing.JLabel labelProdImgFI;
    private javax.swing.JLabel labelProdImgMI;
    private javax.swing.JLabel labelProdNameAI;
    private javax.swing.JLabel labelProdNameFI;
    private javax.swing.JLabel labelProdNameMI;
    private javax.swing.JLabel labelRequest;
    private javax.swing.JList<String> listAvailableItems;
    private javax.swing.JList<String> listFriendRequests;
    private javax.swing.JList<String> listFriendWish;
    private javax.swing.JList<String> listFriends;
    private javax.swing.JList<String> listMyWish;
    private javax.swing.JList<String> listNotification;
    private javax.swing.JButton loginBtn;
    private javax.swing.JTabbedPane loginRegsPane;
    private javax.swing.JPanel loginTab;
    private javax.swing.JTabbedPane mainPane;
    private javax.swing.JPanel panelFriendRequests;
    private javax.swing.JPanel panelFriends;
    private javax.swing.JPanel panelMyWishList;
    private javax.swing.JPanel panelNotifications;
    private javax.swing.JPanel panelProfile;
    private javax.swing.JLabel passwdLabel;
    private javax.swing.JProgressBar progBarMoney;
    private javax.swing.JPanel regsPane1;
    private javax.swing.JScrollPane scrollPaneProdDescAI;
    private javax.swing.JScrollPane scrollPaneProdDescFI;
    private javax.swing.JScrollPane scrollPaneProdDescMI;
    private javax.swing.JScrollPane scrollPanelAvailableItems;
    private javax.swing.JScrollPane scrollPanelFriendRequests;
    private javax.swing.JScrollPane scrollPanelFriendWish;
    private javax.swing.JScrollPane scrollPanelFriends;
    private javax.swing.JTextPane textPaneProdDescAI;
    private javax.swing.JTextPane textPaneProdDescFI;
    private javax.swing.JTextPane textPaneProdDescMI;
    private javax.swing.JTextField txtContributionAmountFI;
    private javax.swing.JTextField txtCreditProfile;
    private javax.swing.JTextField txtEmailProfile;
    private javax.swing.JTextField txtFriendName;
    private javax.swing.JTextField txtFullNameProfile;
    private javax.swing.JPasswordField txtLogPw;
    private javax.swing.JTextField txtLogUsr;
    private javax.swing.JTextField txtRegEmail;
    private javax.swing.JTextField txtRegFname;
    private javax.swing.JTextField txtRegLname;
    private javax.swing.JPasswordField txtRegPw;
    private javax.swing.JTextField txtRegUsr;
    private javax.swing.JTextField txtUsrNameProfile;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
}
