XPRES_JAVA = $(shell ls xpres/*.java)
XPRES2_JAVA = $(shell ls xpres2/*.java)

all: xpres.jar xpres2.jar

clean:
	rm -rf class xpres.jar xpres2.jar build

build/xpres/grammar/XpresParser.java: xpres/grammar/Xpres.g4
	java -jar /usr/local/lib/antlr-complete.jar -o build xpres/grammar/Xpres.g4 

xpres.jar: $(XPRES_JAVA) build/xpres/grammar/XpresParser.java
	javac -sourcepath . -cp .:/usr/local/lib/antlr-complete.jar -d class -Xlint:deprecation xpres/*.java build/xpres/grammar/*.java
	cd class; jar cf ../xpres.jar xpres/*.class xpres/grammar/*.class

build/xpres2/grammar/XpresParser.java: xpres2/grammar/Xpres.g4
	java -jar /usr/local/lib/antlr-complete.jar -o build xpres2/grammar/Xpres.g4 

xpres2.jar: $(XPRES2_JAVA) build/xpres2/grammar/XpresParser.java
	javac -sourcepath . -cp .:/usr/local/lib/antlr-complete.jar -d class -Xlint:deprecation xpres2/*.java build/xpres2/grammar/*.java
	cd class; jar cf ../xpres2.jar xpres2/*.class xpres2/grammar/*.class
