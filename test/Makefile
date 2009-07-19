ANT = ant
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
	rm -f a.out *.s *.o tc.* `cat TARGETS`
