.SUFFIXES:

libcbc.a: libcbc.o
	ar cr libcbc.a libcbc.o

libcbc.o: libcbc.cb
	../bin/cbc -O -c $< -o $@

clean:
	rm -f libcbc.a *.o *.s
