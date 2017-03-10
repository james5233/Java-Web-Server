default: java_handler.java MyClient.java MyServer.java file_handler.java session_manager.java sess_transactor.java
	javac java_handler.java -Xlint:unchecked
	javac file_handler.java -Xlint:unchecked
	javac MyClient.java -Xlint:unchecked
	javac MyServer.java -Xlint:unchecked
	javac session_manager.java -Xlint:unchecked
	javac sess_transactor.java -Xlint:unchecked

all: java_handler.java MyClient.java MyServer.java file_handler.java session_manager.java sess_transactor.java Stock_src/Stock.java Stock_src/mainfest.mf
	javac java_handler.java -Xlint:unchecked
	javac file_handler.java -Xlint:unchecked
	javac MyClient.java -Xlint:unchecked
	javac MyServer.java -Xlint:unchecked
	javac session_manager.java -Xlint:unchecked
	javac sess_transactor.java -Xlint:unchecked
	(cd Stock_src && javac Stock.java -Xlint:unchecked)
	(cd Stock_src && jar cmf mainfest.mf ../Stock.jar Stock.class)

clean:
	rm -f java_handler.class file_handler.class MyServer.class MyClient.class session_manager.class sess_transactor.class handler_error_log *~

clean_all:
	rm -f java_handler.class file_handler.class MyServer.class MyClient.class session_manager.class sess_transactor.class handler_error_log Stock.jar *~ Stock_src/Stock.class Stock_src/*~
