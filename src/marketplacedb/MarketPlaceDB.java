/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketplacedb;

import Bank.Bank;
import Bank.BankDBException;
import Trader.TraderIF;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nizam
 */
public class MarketPlaceDB extends UnicastRemoteObject implements MarketPlaceDBIF{

    String dbms="derby";
    String datasource="Banks";
    static final String NAME = "rmi://localhost/market";
    Map<String, TraderIF> clients;
    List<Item> items;
    List<Item> wishItems;
    TraderIF client;
    Bank bank;
    
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
        createDatabase(dbms,datasource);
    }

    @Override
    public void registerClient(TraderIF obj) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unregisterClient(TraderIF obj) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sell(String name, float price, String clientId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean buy(String name, float price, String clientId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void wish(String item, float price, String clientId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, TraderIF> getClients() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Item> getItems() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void login(TraderIF client) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logout(TraderIF client) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void createDatabase(String dbms, String datasource) throws SQLException, ClassNotFoundException, BankDBException {
        Connection connection = getConnection(dbms, datasource);
        boolean existCustomer = false;
        boolean existItem = false;
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        for (ResultSet rs = dbm.getTables(null, null, null, null); rs.next();) {
            if (rs.getString(tableNameColumn).equals("Customer")) {
                existCustomer = true;
                rs.close();
                break;
            }
            if(rs.getString(tableNameColumn).equals("Item"))
            {
                existCustomer = true;
                rs.close();
                break;
            }
        }
        if (!existCustomer) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE customer (" + 
                                          "name VARCHAR(32) PRIMARY KEY," + 
                                          "password VARCHAR(32)," +
                                          "sold INTEGER," +
                                          "bought INTEGER" +
                                       ")" );
        }
        if (!existItem) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE item (" + 
                                          "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                                          "itemName VARCHAR(32)," + 
                                          "price FLOAT," +
                                          "name VARCHAR(32)," +
                                          "wish INTEGER" +
                                       ")");
        }
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
    
}
