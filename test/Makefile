CBC = ./cbc
ANT = jant

test:
	./test.sh

recompile:
	cd .. && $(ANT) recompile

comptest: recompile
	./test.sh

clean:
	rm -f *.s *.o `svn pget svn:ignore .`
