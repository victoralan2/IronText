package org.irontext;

public class Main {
    public static void main(String[] args) throws Exception {
        BadWordFilter wordFilter = new BadWordFilter();
        System.out.println("HELLO WORD!");
        Thread.sleep(10000);

        try {
            System.out.println(wordFilter.filter("hello word! BITCH FUCK YOU"));
        } catch (Exception e){
            e.printStackTrace();
        }
        Thread.sleep(1000);
//
//        try {
//            Server server = new Server("localhost", 2918);
//            new Thread(() ->{
//                try {
//                    server.startServer();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//
//            }).start();
//
//
//            Client client2 = new Client("localhost", 2918);
//            client2.startConnection();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
    }
}
