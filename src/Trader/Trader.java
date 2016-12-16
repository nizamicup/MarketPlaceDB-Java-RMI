/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trader;

import Bank.Bank;
import Bank.RejectedException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import marketplacedb.Item;
import marketplacedb.MarketPlaceDBIF;

/**
 *
 * @author Nizam
 */
public class Trader extends UnicastRemoteObject implements Runnable , TraderIF{

    private String traderName;
    private MarketPlaceDBIF marketPlace;
    private Map<String, TraderIF> traders;
    boolean register = true;
    private Bank myBank;
    private int initialAmount;
    private List<Item> items = new ArrayList<Item>();
    private String password;

    public Trader(String traderName, String password, MarketPlaceDBIF marketPlace, String bank, int initialAmount) throws RemoteException, RejectedException {
        //public Trader(MarketPlaceIF marketPlace,String bank, int initialAmount) throws RemoteException {

        
        this.traderName = traderName;
        this.marketPlace = marketPlace;
        this.initialAmount = initialAmount;
        this.password = password;
        try {

            myBank = (Bank) Naming.lookup(bank);
           marketPlace.registerClient(this);
           marketPlace.login(this);
        } catch (NotBoundException ex) {
            System.out.println(ex.toString());
        } catch (MalformedURLException ex) {
            System.out.println(ex.toString());
        }

       /**catch (Exception re) {
            //marketPlace.unregisterClient(this);
            //myBank.deleteAccount(myBank.getAccount(this.getClient()));
            //myBank.deleteAccount(this.getClient());
            System.err.println("Error creating account.");
            System.exit(0);
        }*/

        try {
           // myBank.deposit(myBank.getAccount(this.getClient()), initialAmount);
           // myBank.getAccount(traderName).deposit(initialAmount);
        } catch (Exception re) {
            //marketPlace.unregisterClient(this);
            System.err.println("Error loading the money.");
            System.exit(0);
        }

    }

    

    public String getClient() {
        return traderName;
    }

    @Override
    public synchronized void retrieveMsg(String message) {
        System.out.println(message);
    }

    @Override
    public synchronized String getID() throws RemoteException {
        return traderName;
    }

    void sellItem(String itemName, float itemPrice) {
        try {
            marketPlace.sell(itemName, itemPrice, traderName);
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        }
    }

    void wish(String itemName, float itemPrice) {
        try {
            marketPlace.wish(itemName, itemPrice, traderName);
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }

    }

    void buy(String itemName, float itemPrice) {
        try {
            marketPlace.buy(itemName, itemPrice, traderName);
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }

    }

    void getItems() throws RemoteException {

        items = marketPlace.getItems();
        if (!items.isEmpty()) {
            System.out.println("\nRegistered Clients ***********");
            for (Item item : items) {
                System.out.println(item.getName()+ "********"+"and Price is: "+item.getPrice());
            }
            System.out.println("******************************\n");
        }
    }

    void getClients() throws RemoteException {

        traders = marketPlace.getClients();
        if (!traders.isEmpty()) {
            System.out.println("\nRegistered Clients ***********");
            for (TraderIF client : traders.values()) {
                System.out.println(client.getID());
            }
            System.out.println("******************************\n");
        }
    }

    private void printMessage() throws RemoteException, RejectedException {
        System.out.println("Enter your request...");
        Scanner scanner = new Scanner(System.in);
        String message = scanner.nextLine();

        switch (message) {
            case "close":
                register = false;
                //marketPlace.unregisterClient(this);
               //myBank.deleteAccount(myBank.getAccount(this.getClient()));
                marketPlace.logout(this);
                System.out.println("You are Unregisterd from the MarketPlace");
                break;
            case "sell":
                System.out.println("Enter your Item name");
                String itemName = scanner.nextLine();
                System.out.println("Enter Item price");
                float itemPrice = Float.valueOf(scanner.nextLine());
                sellItem(itemName, itemPrice);
                break;
            case "wish":
                System.out.println("Enter your Item name");
                String wishItem = scanner.nextLine();
                System.out.println("Enter Item price");
                float wishItemPrice = Float.valueOf(scanner.nextLine());
                wish(wishItem, wishItemPrice);
                break;
            case "buy":
                System.out.println("Available items in the Market:");
                getItems();
                System.out.println("Enter your Item name");
                String buyItem = scanner.nextLine();
                System.out.println("Enter Item price");
                float buyItemPrice = Float.valueOf(scanner.nextLine());
                buy(buyItem, buyItemPrice);
                break;
            case "list":
                getClients();
                break;
            case "items":
                getItems();
                break;

        }
    }

    @Override
    public synchronized void notifyCustomer(Item item, float price) {
        System.out.println("You bought an item " + item.getName() + " on price: " + price);
    }

    @Override
    public synchronized void notitySeller(Item item, float price) {

        System.out.println("You sold an item " + item.getName() + " on price: " + price);
    }

    @Override
    public String getPassword() throws RemoteException {
               return password;
    }

    @Override
    public void run() {
        while (register) {

            try {
                printMessage();
            } catch (RemoteException ex) {
                System.out.println("Error: " + ex.toString());
            } catch (RejectedException ex) {
                Logger.getLogger(Trader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Good Bye!");
        System.exit(0);
    }

}
