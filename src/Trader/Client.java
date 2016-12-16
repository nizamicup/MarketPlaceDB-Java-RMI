/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trader;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import marketplacedb.MarketPlaceDBIF;
import Bank.Bank;
import Bank.Account;

/**
 *
 * @author Nizam
 */
public class Client {

    private static String bankName="Nordea";
    private static int initialAcmoun=10000;
    private static MarketPlaceDBIF marketPlace;
    public static void main(String[] args) throws NotBoundException, MalformedURLException, RemoteException {

        try {
            marketPlace = (MarketPlaceDBIF) Naming.lookup("rmi://localhost/market");
            System.out.println("Enter your name");
            Scanner scanner = new Scanner(System.in);
            String userName;
            userName = scanner.next();
            System.out.println("Enter your password");
            Scanner scan = new Scanner(System.in);
            String password;
            password = scan.next();
            Thread clientThread = new Thread(new Trader(userName,password,marketPlace,bankName,initialAcmoun));
            
           clientThread.start();
            
            
          

        } catch (Exception e) {
            System.err.println("The runtime aaafailed: " + e.getMessage());
            System.exit(1);
        }

    }
}

