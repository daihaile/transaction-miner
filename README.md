
# transaction-miner

This project is based on the Blockchain Logging Framework. https://github.com/TU-ADSP/Blockchain-Logging-Framework

To build this project run
```bash
 cd transaction-miner
 mvn clean compile assembly:single 

```

To run this project use the compiled jar file
```bash
cd target
java -jar transaction-miner-1.0-SNAPSHOT-jar-with-dependencies.jar <mode> <node> <contractAddress> <startblock> <endblock>
```
with\
mode: 	websocket | ipc | http\
node: 		url of ipc file or url to websocket/http\

examples:
```bash
	java -jar transaction-miner-1.0-SNAPSHOT-jar-with-dependencies.jar Gamble.bcql
```

```bash
	java -jar transaction-miner-1.0-SNAPSHOT-jar-with-dependencies.jar websocket ws://localhost:8546/ 0xBC0A50d62A07B8392F89216d58f958d1D119D94A 200 694
```
