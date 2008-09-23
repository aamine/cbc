ANT = jant
CBC = ../bin/cbc
CBFLAGS =

.SUFFIXES:
.SUFFIXES: .cb .

.cb:
	$(CBC) $(CBFLAGS) $<

default: compile

test:
	./run.sh

compile:
	cd .. && $(ANT) compile

recompile:
	cd .. && $(ANT) recompile

comptest: recompile test

clean:
	rm -f *.s *.o `svn pget svn:ignore .`
