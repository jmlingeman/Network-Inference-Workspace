#!/usr/bin/ruby


# checks to see if all mentioned data files exist and contain the conditions
# mentioned in the xml files

# todo - check to see that document meets EMI-ML "schema", such as it is
# at the very least, check that all required attributes are present, and that
# the 'name' attribute for variables is not blank - these are actual cases
# that this script did not catch, and hosed the repository until files were fixed..
# there are probably other such cases. 20071214


class TestRepository

def self.parse_file(path)
    require 'rexml/document'


    textconds = {}
    xml = REXML::Document.new(File.open(path))
    shortname = path.gsub(@repopath,"")
    shortname.gsub!(File::SEPARATOR,"")

    xml.elements.each "//uri" do |uri|
        u  = "#{uri}"
        u.gsub!("<uri>","")
        u.gsub!("</uri>","")
        u.strip!
        if (!u.downcase.index('httpindirect').nil?)
            segs = u.split("/")
            u = segs.last
        end
        filename = @repopath + File::SEPARATOR + u
        datashortname = filename.gsub(@repopath,"")
        datashortname.gsub!(File::SEPARATOR,"")
        if (!File.exist?(filename))
            puts "ERROR: xml file #{u} refers to non-existent file: #{filename}"
        end
        header =  `head -1 #{filename}`
        header.strip!
        header.chomp!
        segs = header.split(/\t/)
        segs.shift
        for seg in segs
            @dataconds[seg] = [] if (@dataconds[seg].nil?)
            @dataconds[seg] << datashortname
            textconds[seg] = ''
        end
    end


    conds = []
    xml.elements.each "//condition" do |c|
        if (!(@allconds[c.attributes['alias']].nil?))
            puts "ERROR: DUPLICATE CONDITION NAME: #{c.attributes['alias']}"
        end
        @allconds[c.attributes['alias']] = ''
        conds <<  c.attributes['alias']
    end


    for cond in conds
        if textconds[cond].nil?
            puts "ERROR: condition #{cond} in #{shortname} doesn't exist in data file(s)"
        end
    end


end


def self.check_permissions()
    require 'find'
    xml_file_names = {}
    permission_file_names = {}

    Find.find @repopath do |file|
        if file =~ /\.xml$/ and file !~ /\._/
            name = file.gsub(/\.xml$/,"")
            segs = name.split(File::SEPARATOR)
            name = segs.last
            xml_file_names[name.strip] = 1
        end
    end
    File.open("#{@repopath}#{File::SEPARATOR}.permissions") do |file|
        file.each_line do |line|
            next if line =~ /#/
            segs = line.split(":")
            permission_file_names[segs.first.strip] = 1
        end
    end

    puts "Checking permissions file...."

    #todo, only print this if there are some
    puts "!!!!!FILES NOT MENTIONED IN PERMISSIONS FILE:\n"

    fns = xml_file_names.keys.sort
    pfn = permission_file_names.keys.sort



    for f in fns
        if (pfn.detect {|i| i == f}.nil?)
            puts "#{f}"
        end
    end


    #todo, only print this if there are some
    puts "\nNonexistent files mentioned in permissions file:\n "


    for p in pfn
        if (fns.detect {|i| i == p}.nil?)
            puts "#{p}.xml"
        end
    end

    # todo, check passwd file and make sure each permissions entry points to valid user(s))

end


def self.main()
    if (ARGV.size == 0)
        puts "please supply a directory to parse"
        return
    end
    puts "parsing xml files...."
    #@repopath = '/Users/dtenenbaum/emi-sandbox/halobacterium/repos'
    @repopath = ARGV.first
    @allconds = {}
    @dataconds = {}




    require 'find'
    Find.find @repopath do |file|
        if file =~ /\.xml$/ and file !~ /\._/
            #puts "parsing #{file}..."
            parse_file file
        end
    end

    @dataconds.each_key do |k|
        if (@allconds[k].nil?)
            puts "WARNING: Orphan Condition #{k} in data file(s):"
            for item in @dataconds[k]
                puts "\t#{item}"
            end
        end
    end

    if (Kernel.test(?e, "#{@repopath}#{File::SEPARATOR}.permissions"))
        check_permissions()
    else
        puts "no permissions file, skipping permissions check"
    end
    


end

main()    

end


