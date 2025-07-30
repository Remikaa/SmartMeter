package com.Meter.IskraemcoInterns;

public class MeterConfig {
    public Server server;
    public Client client;

    public static class Server {
        public String port;
        public int baudRate;
        public int dataBits;
        public String stopBits;
        public String parity;
    }

    public static class Client {
        public int clientAddress;
        public int serverAddress;
        public String authentication;
        public String password;
        public String interfaceType;
    }
}
