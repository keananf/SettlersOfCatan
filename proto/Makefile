CC = protoc
DIR = ../core/src/main/java
PROTO_FILES = trade.proto      \
	      resources.proto  \
	      board.proto  \
	      build.proto  \
	      enums.proto  \
	      responses.proto  \
	      requests.proto   \
	      events.proto     \
	      messages.proto   \
	      

all:
	mkdir -p $(DIR)/java
	make java

java:
	$(CC) -I . $(PROTO_FILES) --java_out $(DIR)/

clean:
	rm -r build
