CBC = ./cbc
ANT = jant

.SUFFIXES:
.SUFFIXES: .cb .
.cb:
	$(CBC) $<

test:
	./test.sh

recompile:
	cd .. && $(ANT) clean && $(ANT) compile

comptest: recompile
	./test.sh

clean:
	rm -f *.s *.o `svn pget svn:ignore .`
