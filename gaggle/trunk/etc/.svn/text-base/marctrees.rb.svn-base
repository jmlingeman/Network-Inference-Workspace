require 'rexml/document'
require 'pp'

$inputpath = "/Users/dtenenbaum/marc_out"
$outputpath = "/Users/dtenenbaum/marc_treefixed"
$filename = "/Users/dtenenbaum/dl/NewTrees.txt"
h = {}
tp = ''


def self.remove_preds(xmlfile)
    next unless xmlfile =~ /\.xml$/
    xml = REXML::Document.new(File.open("#{$outputpath}/#{xmlfile}"))
    root = xml.root
    xml.elements.each "//predicate" do |pred|
        if (pred.attributes['category'] =~ /^perturbation/)
            root.delete(pred)
        end
    end
    File.open("#{$outputpath}/#{xmlfile}", "w") do |outfile|
        outfile << xml
    end
end



def self.add_preds(file, treepaths)
    xml = REXML::Document.new(File.open("#{$outputpath}/#{file}"))
    root = xml.root
    count = 1
    puts "file is #{file}"
    for item in treepaths[file]
        if count == 1
            name = 'perturbation'
        else
            name = "perturbation#{count}"
        end
        count += 1
        elem = REXML::Element.new("predicate")
        elem.add_attribute("category", name)
        elem.add_attribute("value", item)

        root.insert_before("//predicate", elem)
    end
    File.delete("#{$outputpath}/#{file}") if File.exists?("#{$outputpath}/#{file}")
    File.open("#{$outputpath}/#{file}", "w") do |outfile|
        outfile.print root
    end
end


def self.get_filenames
    h = {}
    File.open($filename) do |file|
        while line = file.gets do
            if (line =~ /^#/)
                xmlfile = line.chomp.strip.gsub("#","").strip
                h[xmlfile] = nil
            end
        end
    end
    h.keys.sort
end



def get_treepaths
    tp = 'genetic:'
    treepaths = {}
    xmlfile = ''
    start = true
    count = 0
    stored = '  dummy'
    File.open($filename) do |file|
        while line = file.gets do
            count += 1

            next if line =~ /^BEGIN|^END|^From|^With curr/

            if (line =~ /^#/)
                xmlfile = line.chomp.strip.gsub("#","").strip
                next
            end

            next if line.chomp.strip.empty?

            if (line =~ /^\+\+\+/)
                tp.chop!
                unless (tp =~ /^genetic:Any |^genetic:All /)
                    treepaths[xmlfile] = [] if treepaths[xmlfile].nil?
                    treepaths[xmlfile] << tp
                end
                tp = 'genetic:'
                next
            end
            tp += line.chomp.strip + ":"
        end
    end
    treepaths
end

system("rm #{$outputpath}/*")
for file in get_filenames
    cmd = "cp #{$inputpath}/#{file} #{$outputpath}"
    system cmd
    remove_preds(file)
end

treepaths = get_treepaths()

for file in get_filenames
    add_preds(file, treepaths)
end
