#!/usr/bin/env ruby

require 'fileutils'
require 'pathname'
require 'optparse'

TMPDIR = '/tmp'

def main
  libs = []
  expr = stmt = nil
  auto_print = false
  dump_asm = false
  verbose = false

  parser = OptionParser.new
  parser.banner = "Usage: #{File.basename($0)} [-i LIB] [-v] ([-p] -e expr | file)"
  parser.on('-i', '--import=LIB', 'Imports library LIB.') {|lib|
    libs.push lib
  }
  parser.on('-e', '--expr=EXPR', 'Evaluate Cflat expression EXPR.') {|str|
    expr = str
  }
  parser.on('-p', '--print', 'Auto-print mode.') {
    auto_print = true
  }
  parser.on('--dump-asm', 'Dump assembly source.') {
    dump_asm = true
  }
  parser.on('-s', '--statement=STMT', 'Evaluate Cflat statement. STMT') {|str|
    stmt = str
  }
  parser.on('-v', '--verbose', 'Verbose mode.') {
    verbose = true
  }
  parser.on('--help', 'Prints this message and quit.') {
    puts parser.help
    exit 0
  }
  begin
    parser.parse!
  rescue OptionParser::ParseError => err
    $stderr.puts err.message
    $stderr.puts parser.help
    exit 1
  end
  if auto_print and (not expr or stmt or not ARGV.empty?)
    error_exit "auto print mode works only with -e/--expr."
  end
  error_exit "too many arguments" if ARGV.size > 1
  num_src = [ARGV[0], expr, stmt].compact.size
  error_exit "file argument, --expr and --stmt is exclusive" if num_src > 1
  error_exit "one of file argument, --expr, --stmt is required" if num_src == 0

  if expr or stmt
    if auto_print and not libs.include?('stdio')
      libs.unshift 'stdio'
    end
    using_tmpfile("#{TMPDIR}/cflatexp#{Process.pid}.cb") {|path|
      generate_source path, libs, stmt || build_stmt(expr, auto_print)
      print_source path if verbose
      interprit path, verbose, dump_asm
    }
  else
    interprit File.expand_path(ARGV[0]), verbose, dump_asm
  end
end

def error_exit(msg)
  error msg
  exit 1
end

def error(msg)
  $stderr.puts "#{File.basename($0, '.*')}: error: #{msg}"
end

SEPARATOR_WIDTH = 60

def print_source(path)
  puts " #{path} ".center(SEPARATOR_WIDTH, '-')
  puts File.read(path)
  puts '-' * SEPARATOR_WIDTH
end

def using_tmpfile(path)
  yield path
ensure
  FileUtils.rm_f path
end

def build_stmt(expr, auto_print)
  stmt = auto_print ? %Q[printf("%d\\n", #{expr})] : expr
  stmt + (stmt.strip[-1,1] == ';' ? '' : ';')
end

def generate_source(path, libs, stmt)
  File.open(path, 'w') {|f|
    unless libs.empty?
      libs.each do |lib|
        f.puts "import #{lib};"
      end
      f.puts
    end
    f.print(<<-End)
int
main(int argc, char** argv)
{
    #{stmt}
    return 0;
}
    End
  }
end

def interprit(path, verbose, dump_asm)
  cbc = Pathname.new($PROGRAM_NAME).realpath.dirname + 'cbc'
  Dir.chdir TMPDIR
  opt = dump_asm ? ' --dump-asm' : ''
  devnull = (verbose || dump_asm) ? '' : ' >/dev/null'
  invoke "#{cbc}#{opt} #{path} #{devnull}", verbose
  exit 0 if dump_asm
  begin
    exe = File.basename(path, '.cb')
    invoke "./#{exe}", verbose
    exit 0
  rescue SystemCallError => err
    error_exit err.message
  ensure
    FileUtils.rm_f "#{exe}.s"
    FileUtils.rm_f "#{exe}.o"
    FileUtils.rm_f exe
  end
end

def invoke(cmd, verbose = false)
  $stderr.puts cmd if verbose
  system cmd
  st = $?
  show_status st if st.exitstatus != 0 or verbose
  st
end

module Signal   # reopen
  NAMES = Signal.list.invert

  def Signal.name(num)
    NAMES[num] or raise ArgumentError, "undefined signal number"
  end
end

def show_status(st)
  case
  when st.coredump?
    $stderr.puts "core dumped (SIG#{Signal.name(st.termsig)})"
  when st.signaled?
    $stderr.puts "terminated by signal (SIG#{Signal.name(st.termsig)})"
  else
    $stderr.puts "status: #{st.exitstatus}"
  end
end

main
