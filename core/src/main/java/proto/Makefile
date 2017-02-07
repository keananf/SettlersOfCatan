CC = protoc
PROTO_FILES = trade/trade.proto      \
	      resource/resource.proto  \
	      terrain/terrain.proto  \
	      board/board.proto  \
	      empty.proto  \
	      lobby/lobby.proto  \
	      requests.proto   \
	      events.proto     \
	      messages.proto   \
	      

all:
	make java

java:
	$(CC) -I . $(PROTO_FILES) --java_out=.. 

clean:
	rm -r build
