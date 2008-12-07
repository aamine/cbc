CBC = ../bin/cbc
CBFLAGS = -O -fPIC
TARGET = libcbc.a
OBJS = stdarg.o alloca.o

.SUFFIXES:
.SUFFIXES: .cb .s .o

.cb.o:
	$(CBC) $(CBFLAGS) -c $< -o $@
.s.o:
	$(CBC) -c $<

$(TARGET): $(OBJS)
	ar cr $(TARGET) $(OBJS)

stdarg.o: stdarg.cb

clean:
	rm -f $(TARGET) *.o *.s
