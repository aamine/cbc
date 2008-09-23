CBC = ../bin/cbc
CBFLAGS = -O
TARGET = libcbc.a
OBJS = libcbc.o

.SUFFIXES:
.SUFFIXES: .cb .o

.cb.o:
	$(CBC) $(CBFLAGS) -c $< -o $@

$(TARGET): $(OBJS)
	ar cr $(TARGET) $(OBJS)

clean:
	rm -f $(TARGET) *.o *.s
