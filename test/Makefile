ANT = jant

test:
	./test.sh

compile:
	cd .. && $(ANT) compile

recompile:
	cd .. && $(ANT) recompile

comptest: recompile test

clean:
	rm -f *.s *.o `svn pget svn:ignore .`
