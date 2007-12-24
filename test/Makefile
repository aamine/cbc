CC = gcc
CFLAGS = -Wall -O -fno-omit-frame-pointer
CBC = ./cbc
CBPROGS = zero one hello hello2 hello3 hello4 integer string \
          funcall0 funcall1 funcall2 funcall3 funcall4 funcall5 \
          param lvar1 lvar2 comm scomm gvar sgvar slvar slcomm \
          add sub mul div mod bitand bitor bitxor bitnot lshift rshift \
          logicalnot unaryplus unaryminus \
          eq neq gt lt gteq lteq \
          assign opassign inc dec \
          if1 if2 condexpr logicaland logicalor \
          while1 while2 while3 while-break while-continue \
          dowhile1 dowhile2 dowhile3 dowhile-break dowhile-continue \
          for1 for-break for-continue \
          array struct struct-semcheck union union-semcheck \
          pointer ptrmemb funcptr \
          charops shortops intops longops \
          ucharops ushortops uintops ulongops \
          block cast defvar mbc \

# immscope cast

.SUFFIXES:
.SUFFIXES: .cb .
.cb:
	$(CBC) $<

default: cbprograms

test: cbprograms
	./test.sh

cbprograms: $(CBPROGS)

errors:
	-$(CBC) struct-semcheck2.cb
	-$(CBC) struct-semcheck3.cb
	-$(CBC) struct-semcheck4.cb
	-$(CBC) union-semcheck2.cb
	-$(CBC) union-semcheck3.cb
	-$(CBC) union-semcheck4.cb
	-$(CBC) break-semcheck.cb
	-$(CBC) continue-semcheck.cb
	-$(CBC) funcall-semcheck.cb

clean:
	rm -f $(CBPROGS) *.s *.o
