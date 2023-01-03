package main;

import main.Ethereum.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        String filepath, node, contractAddress, method;
        BigInteger startBlock, endBlock;
        Client client = null;
        for (String arg : args) {
            System.out.println(arg);
        }
        if (args.length == 0) {
            LOGGER.log(Level.SEVERE, "No arguments given");
            return;
        } else if (args.length == 1) {
            final String path = args[0];
            ManifestReader mr = new ManifestReader(path);
            node = mr.getNode();
            contractAddress = mr.getContractAddress();
            startBlock = mr.getStartBlock();
            endBlock = mr.getEndBlock();
            client = Client.connectWebsocket(node);
        } else if (args.length == 5 ) {
            method = args[0];
            node = args[1];
            contractAddress = args[2];
            startBlock = new BigInteger(args[3]);
            endBlock = new BigInteger(args[4]);
            if(method.equalsIgnoreCase("websocket")) {
                System.out.println("connect to webscoket");
                client = Client.connectWebsocket(node);
            } else if (method.equalsIgnoreCase("http")) {
                client = Client.connectHttp(node);
            } else if( method.equalsIgnoreCase("ipc")){
                client = Client.connectIpc(node);
            }
        } else {
            System.out.println("bad arguments");
            return;
        }
        System.out.println(client);
        ArrayList<EthereumTransaction> txList = getAllTransactions(client, startBlock, endBlock, contractAddress);
        try {

            ArrayList<TransactionTrace> traceList = client.getAllTransactionTraces(txList);
            System.out.println("saving to csv: " + traceList.size());
            boolean writeToFile = client.writeCSV(traceList, "output");
            System.out.println(writeToFile);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static ArrayList<EthereumTransaction> getAllTransactions(Client client, BigInteger blockStart, BigInteger blockEnd, String contractAddress) throws IOException {
        BigInteger blockNumberEnd = blockEnd;
        BigInteger currentBlockNumber = blockStart;
        EthereumBlock block;
        ArrayList<EthereumTransaction> txList = new ArrayList<>();
        FileWriter writer = new FileWriter("transactions.csv",false);
        while(currentBlockNumber.compareTo(blockNumberEnd) < 1) {
            block = client.queryBlockData(currentBlockNumber);
            if(block.transactionCount() > 0) {
                LOGGER.info("BLOCK: " + block.getNumber());
                Stream<EthereumTransaction> stream = block.transactionStream();
                stream.forEach(transaction -> {
                    String txString = transaction.getHash();
                    try {
                        if(transaction.getTo() == null) {
                            LOGGER.info("return: " + transaction.getHash());
                            return;
                        }
                        String to = transaction.getTo();
                        String from = transaction.getFrom();
                        BigInteger value = transaction.getValue();
                        if(transaction.getTo().equalsIgnoreCase(contractAddress)) {
                            writer.write(txString+ "," + from + "," + to + "," + value.toString() + "\n");
                            txList.add(transaction);
                            LOGGER.info("writing to file " + txString);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (NullPointerException e) {
                        String error = transaction.getHash() + " " + e.getMessage();
                        LOGGER.info(error);
                    }
                });
            }
            currentBlockNumber = currentBlockNumber.add(BigInteger.valueOf(1));
        }
        LOGGER.info("Fetched " + txList.toArray().length + " transactions in " + blockStart + " to " + blockNumberEnd);
        writer.flush();
        writer.close();
        return txList;
    }
}
