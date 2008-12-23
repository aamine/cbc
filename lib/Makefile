CBC = ../bin/cbc
CBFLAGS = -O -fPIC
TARGET = libcbc.a
OBJS = stdarg.o alloca.o

CC = gcc
CFLAGS = -Wall

.SUFFIXES:
.SUFFIXES: .cb .s .o

.cb.o:
	$(CBC) $(CBFLAGS) -c $< -o $@
.s.o:
	$(CBC) -c $<

$(TARGET): $(OBJS)
	ar cr $(TARGET) $(OBJS)

stdarg.o: stdarg.cb

sizeof_jmpbuf: sizeof_jmpbuf.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -f $(TARGET) *.o stdarg.s
