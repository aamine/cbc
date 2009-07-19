ANT = ant
VERSION = 1.0.1
BINVERSION = 1.0

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

test: check
check:
	cd test; $(MAKE) test

unittest:
	cd unit; $(MAKE) test

dist:
	rm -rf cbc-$(VERSION) cbc-$(BINVERSION)
	svn export http://i.loveruby.net/svn/public/cbc/tags/$(VERSION) cbc-$(VERSION)
	cd cbc-$(VERSION); $(MAKE)
	cd cbc-$(VERSION); $(ANT) clean-build
	tar c cbc-$(VERSION) | gzip -n > cbc-$(VERSION).tar.gz
	mv cbc-$(VERSION) cbc-$(BINVERSION)
	tar c cbc-$(BINVERSION) | gzip -n > cbc-$(BINVERSION).tar.gz
	rm -rf cbc-$(BINVERSION)
