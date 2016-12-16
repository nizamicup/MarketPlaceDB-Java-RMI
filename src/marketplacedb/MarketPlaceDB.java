/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketplacedb;

import Bank.Bank;
import Bank.BankDBException;
import Bank.RejectedException;
import Trader.TraderIF;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nizam
 */
public class MarketPlaceDB extends UnicastRemoteObject implements MarketPlaceDBIF {

    String dbms = "derby";
    String datasource = "Banks";
    static final String NAME = "rmi://localhost/market";
    Map<String, TraderIF> clients;
    List<Item> items;
    List<Item> wishItems;
    TraderIF client;
    Bank bank;
    Statement statement;
    PreparedStatement createCustomerStmt;
    PreparedStatement findCustomerStmt;
    PreparedStatement deleteCustomerStmt;
    PreparedStatement createItemStmt;
    PreparedStatement findItemStmt;
    PreparedStatement deleteItemStmt;
    Connection conn;

    MarketPlaceDB(String bankName) throws RemoteException, MalformedURLException, SQLException, ClassNotFoundException, BankDBException {
        clients = new HashMap<String, TraderIF>();
        items = new ArrayList<Item>();
        wishItems = new ArrayList<Item>();
        try {
            bank = (Bank) Naming.lookup(bankName);
        } catch (Exception ex) {
            System.err.println("Error looking for the bank given the URL: "
                    + bankName);
            System.exit(1);
        }

        Naming.rebind(NAME, this);
        System.out.println("Server ready.");
        conn = createDatabase(dbms, datasource);
        //prepareStatements(conn, "customer");
    }

    @Override
    public synchronized void registerClient(TraderIF obj) throws RemoteException {
        
        try {            
            ResultSet rs = null;
            prepareStatements(conn,"customer");
            findCustomerStmt.setString(1, obj.getID());
             //System.out.println(obj.getPassword());
             System.out.println(obj.getID());
            rs=findCustomerStmt.executeQuery();
            
            //Client already exist
            if (rs.next()) {
                System.out.println("User already exist");
                rs.close();
                findCustomerStmt.close();
            } else {
                rs.close();
                createCustomerStmt.setString(1, obj.getID());
            createCustomerStmt.setString(2, obj.getPassword());
            createCustomerStmt.setInt(3, 0);
            createCustomerStmt.setInt(4, 0);
            int rows = createCustomerStmt.executeUpdate();
                if (rows == 1) {
                    bank.newAccount(obj.getID());
                    clients.put(obj.getID(), obj);
                    System.out.println("Client: " + obj + "is now registerd in the MarketPlace");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketPlaceDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RejectedException ex) {
            Logger.getLogger(MarketPlaceDB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public synchronized void sell(String name, float price, String clientId) throws RemoteException {
        try {
            
            prepareStatements(conn, "ITEM");
           System.out.println("items added or not");
            boolean added = addItemDatabase(name, price, clientId);
             
            if (added) {
                Item item = new Item(name, price, clientId);
                items.add(item);
                System.out.println("Adding new item from " + clientId + " for sale: "
                        + name + " - " + price + " SEK.");
                TraderIF owner = clients.get(clientId);
                //owner.retrieveMsg("You have successfully added an item");
            }
        } catch (SQLException ex) {
            System.out.println("Item can not add to database");
        }

        /**
         * try { manageWishes(); } catch (RejectedException ex) {
         * System.out.println("Error in managing wishes"); }
         */
    }

    @Override
    public synchronized boolean buy(String name, float price, String clientId) throws RemoteException {
        boolean result;
        try {
            prepareStatements(conn, "ITEM");
        } catch (SQLException ex) {
            Logger.getLogger(MarketPlaceDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        Item item = findItem(name, price, clientId);
        if (item != null) {
            try {
                String sellerName = item.getClientId();
                System.out.println(sellerName);
                result = makePurchase(item, sellerName, clientId);
                if (!result) {
                    TraderIF client = clients.get(clientId);
                    client.retrieveMsg("purchase failed for this item");
                }

            } catch (RejectedException ex) {
                Logger.getLogger(MarketPlaceDB.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(MarketPlaceDB.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
    public synchronized void wish(String item, float price, String clientId) throws RemoteException {
    }

    @Override
    public synchronized Map<String, TraderIF> getClients() throws RemoteException {
        return clients;
    }

    @Override
    public synchronized List<Item> getItems() throws RemoteException {
        return items;
    }

    @Override
    public synchronized void login(TraderIF client) throws RemoteException {
        clients.put(client.getID(), client);
        System.out.println("Client " + client.getID() + "  is logged in to the market place");
    }

    @Override
    public synchronized void logout(TraderIF client) throws RemoteException {
        clients.remove(client);
                System.out.println("Client " + client.getID() + "  is logged out from market place");

    }

    private Connection createDatabase(String dbms, String datasource) throws SQLException, ClassNotFoundException, BankDBException {
        Connection connection = getConnection(dbms, datasource);
        boolean existCustomer = false;
        boolean existItem = false;
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        for (ResultSet rs = dbm.getTables(null, null, null, null); rs.next();) {
            if (rs.getString(tableNameColumn).equalsIgnoreCase("customer")) {
                existCustomer = true;
                if (existItem) {
                    rs.close();
                    break;
                }
            } else if (rs.getString(tableNameColumn).equalsIgnoreCase("item")) {
                existItem = true;
                if (existCustomer) {
                    rs.close();
                    break;
                }
            }
        }
        if (!existCustomer) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE customer ("
                    + "name VARCHAR(32) PRIMARY KEY,"
                    + "password VARCHAR(32),"
                    + "sold INTEGER,"
                    + "bought INTEGER"
                    + ")");
        }
        if (!existItem) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE item ("
                    + "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + "itemName VARCHAR(32),"
                    + "price FLOAT,"
                    + "name VARCHAR(32)"
                    + //"wish INTEGER" +
                    ")");
        }
        return connection;
    }

    private Connection getConnection(String dbms, String datasource) throws ClassNotFoundException, SQLException, BankDBException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "Hassan!@7");
        } else {
            throw new BankDBException("Unable to create datasource, unknown dbms.");
        }
    }
    
    private void prepareStatements(Connection connection, String tableName) throws SQLException {
        if (tableName.equalsIgnoreCase("customer")) {
            createCustomerStmt = connection.prepareStatement("INSERT INTO "
                    + tableName + " VALUES (?, ?, ?, ?)");
            findCustomerStmt = connection.prepareStatement("SELECT * from "
                    + tableName + " WHERE NAME = ?");
            deleteCustomerStmt = connection.prepareStatement("DELETE FROM "
                    + tableName
                    + " WHERE name = ?");
            /**
             * updateCustomerStmt = connection.prepareStatement("UPDATE " +
             * tableName + " SET balance = ? WHERE name= ? ");
             */
        }
        if (tableName.equalsIgnoreCase("item")) {
           
            createItemStmt = connection.prepareStatement("INSERT INTO "
                    + tableName + "(itemName,price,name) VALUES (?, ?, ?)");
               findItemStmt = connection.prepareStatement("SELECT * from "
                    + tableName + " WHERE itemName = ?");
            
            deleteItemStmt = connection.prepareStatement("DELETE FROM "
                    + tableName
                    + " WHERE itemName = ?");
            
            /**
             * updateCustomerStmt = connection.prepareStatement("UPDATE " +
             * tableName + " SET balance = ? WHERE name= ? ");
             */
        }
    }

    private boolean addItemDatabase(String item, float price, String clientId) throws SQLException {
        
        
        createItemStmt.setString(1,item);
        createItemStmt.setDouble(2, price);
            createItemStmt.setString(3, clientId);
            
            int rows = createItemStmt.executeUpdate();
                if (rows == 1) {
                    System.out.println("Item: " + item + "is now added to the database for "+ clientId);
                    return true;
                }
        return false;
    }

    private Item findItem(String name, float price, String clientId){
        String failureMsg = "Could not search for specified account.";
        
        ResultSet result = null;
        try {
            findItemStmt.setString(1, name);
            System.out.println(failureMsg);
            result = findItemStmt.executeQuery();
            if (result.next()) {
                return new Item(name,result.getInt("price"),result.getString("name"));//(holderName, result.getInt(BALANCE_COLUMN_NAME), this);
            }
        }
        catch(SQLException ex)
        {
            System.out.println("Can not find the item" + ex.getStackTrace());
        }
        return null;
        
    }

    private boolean makePurchase(Item item, String sellerName, String clientId) throws RemoteException, RejectedException, SQLException {
        TraderIF seller = (TraderIF) clients.get(sellerName);
        TraderIF customer = (TraderIF) clients.get(clientId);

        if (customer == null || seller == null) {
            return false;
        } else if (customer.equals(seller)) {
            return false;
        } else {
            float price = item.getPrice();
            
            //bank.withdraw(bank.getAccount(clientId), (int) price);
            bank.deposit(bank.getAccount(sellerName), (int) price);
            seller.notitySeller(item, price);
            customer.notifyCustomer(item, price);
            removeItem(item);
            items.remove(item);
        }
        System.out.println("Purchase made for" + clientId);
        return true;
    }

    private void removeItem(Item item) throws SQLException {
        ResultSet rs = statement.executeQuery("Select * from item where itemName = '" + item.getName() + "'"
                + "and price='" + item.getPrice() + "' and name = '" + item.getClientId() + "'");
        if (rs.next()) {
            int id = rs.getInt("id");
            statement.executeUpdate("Delete from item where id =" + id);
        }
    }

}
