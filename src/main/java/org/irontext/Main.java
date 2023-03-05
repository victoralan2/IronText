package org.irontext;


import org.irontext.encryption.Hasher;

import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class Main {
    public static Server server;
    public static void main(String[] args) {
        try {
            server = new Server("192.168.1.101", 52216);
            server.startServer();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}