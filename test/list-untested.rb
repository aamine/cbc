def print_list(list)
  list.sort.each do |name|
    puts name
  end
end

test_sh = File.read('test.sh')
tested = test_sh.scan(/[\$\w\-]+\.cb/).reject {|n| /\$/ =~ n } +
         test_sh.scan(%r<\./[\w\-]+>).map {|n| File.basename(n) + '.cb' }
tested = tested.uniq

print_list Dir.glob('*.cb') - tested
