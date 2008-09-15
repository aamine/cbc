ANT = ant

default: all

all: lib/cbc.jar lib/libcbc.a

lib/cbc.jar:
	$(ANT) compile

lib/libcbc.a:
	cd lib; $(MAKE) libcbc.a

clean:
	$(ANT) clean 
	cd lib; $(MAKE) clean
	cd test; $(MAKE) clean

test:
	cd test; $(MAKE) test
