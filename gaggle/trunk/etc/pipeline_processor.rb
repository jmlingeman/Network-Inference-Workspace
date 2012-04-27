#!/usr/bin/ruby

require 'pp'

@ARRAYS_PATH = "/Volumes/Arrays-2/"
@PIPELINE_PATH = @ARRAYS_PATH + "Pipeline/output/project_id/"


def parse()
  ratiofile = File.new(ARGV[1] + ".ratio", "w")
  lambdafile = File.new(ARGV[1] + ".lambda", "w")
  numconds = -1
  ratiofile.print "GENE\t"
  lambdafile.print "GENE\t"
  count = -1
  fn = @PIPELINE_PATH + ARGV.first + "/matrix_output"
  #puts "hey:" + ARGV.first
  File.open(fn) do |file|
    while line = file.gets
      count += 1
      next if count == 0
      next if line =~ /^NumSigGenes/
      segs = line.split()
      if (count == 1)
        2.times {segs.shift}
        segs.pop
        numconds = segs.length / 2
        puts "nc = #{numconds}"
        if (numconds == 1)
          header = segs.first
        else
          header_arr = segs[0..(numconds-1)]
          header = header_arr.join("\t")
        end
        
        ratiofile.puts header
        lambdafile.puts header
      else
        #puts pad(segs[0])
        #rline =  pad(segs.shift) + "\t"
        rline = segs.shift() + "\t"
        lline = "#{rline}"
        segs.shift
        segs.pop
        if (numconds == 1) 
          rline += segs[0]
          lline += segs[1]
        else
          rdata = segs[0..(numconds-1)]
          rline += rdata.join("\t")
          ldata = segs[numconds..(segs.length-1)]
          lline += ldata.join("\t")
        end
        ratiofile.puts rline
        lambdafile.puts lline
      end
    end
  end
  ratiofile.close
  lambdafile.close
end

def pad(input)
  prefix, num = input.split("_")
  pad = ""
  (4- num.length() ).times {pad += "0"}
  return prefix + "_" + pad + num
end

def main()
  if ARGV.size != 2
    puts "specify project and date dir (e.g. 625/20051130_162243), and output dir and prefix (e.g. somedir/mydata)"
    exit
  end
 
  parse  
end


main()