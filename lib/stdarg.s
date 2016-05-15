.file	"stdarg.cb"
	.text
.globl va_init
	.type	va_init,@function
va_init:
	pushl	%ebp
	movl	%esp, %ebp
	movl	$1, %eax
	sall	$2, %eax
	movl	%eax, %ecx
	movl	8(%ebp), %eax
	addl	%ecx, %eax
	movl	%ebp, %esp
	popl	%ebp
	ret
	.size	va_init,.-va_init
.globl va_next
	.type	va_next,@function
va_next:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$12, %esp
	movl	8(%ebp), %eax
	movl	(%eax), %eax
	movl	(%eax), %eax
	movl	%eax, -4(%ebp)
	movl	8(%ebp), %eax
	movl	%eax, -8(%ebp)
	movl	$1, %eax
	sall	$2, %eax
	movl	%eax, -12(%ebp)
	movl	-8(%ebp), %eax
	movl	(%eax), %eax
	movl	-12(%ebp), %ecx
	addl	%ecx, %eax
	movl	%eax, -12(%ebp)
	movl	-8(%ebp), %eax
	movl	%eax, %ecx
	movl	-12(%ebp), %eax
	movl	%eax, (%ecx)
	movl	-4(%ebp), %eax
	movl	%ebp, %esp
	popl	%ebp
	ret
	.size	va_next,.-va_next
	.section	.text.__i686.get_pc_thunk.bx,"axG",@progbits,__i686.get_pc_thunk.bx,comdat
.globl __i686.get_pc_thunk.bx
	.hidden	__i686.get_pc_thunk.bx
	.type	__i686.get_pc_thunk.bx,@function
__i686.get_pc_thunk.bx:
	movl	(%esp), %ebx
	ret
