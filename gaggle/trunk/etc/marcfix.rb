#!/opt/local/bin/ruby

require 'rexml/document'
require 'pp'

class ConditionMapper

    def initialize(newlines, short_source_file_name)
        #puts "repopath = #{$repopath}, ssfn = #{short_source_file_name}"
        sourcefilename = "" + $repopath + File::SEPARATOR + short_source_file_name
        sourcelines = []
        File.open(sourcefilename) do |file|
            while line = file.gets
                sourcelines << line.chomp
            end
        end


        new = get_condition_hash(newlines)
        old = get_condition_hash(sourcelines)

        @new_to_old = {}
        new.each_pair do |key, value|
            @new_to_old[value] = old[key]
        end

        @old_to_new = {}
        old.each_pair do |key, value|
            @old_to_new[value] = new[key] unless new[key].nil?
        end
    end

    def get_condition_hash(lines)
        xml_str = "<root>\n"
        result = {}
        for line in lines
            if (line.strip =~ /^</)
                xml_str += line
                xml_str += "\n"
            end

        end

        xml_str += "\n</root>"


        xml = REXML::Document.new(xml_str)
        xml.elements.each "//condition" do |c|
            cond_name = c.attributes['alias']
            h = {}
            c.elements.each "variable" do |v|
                h[v.attributes['name']] = v.attributes['value']
            end
            key = h.sort.to_s
            result[key] = cond_name
        end
        result
    end

    def new_to_old
        @new_to_old
    end

    def old_to_new
        @old_to_new
    end

end

class Segment
    def initialize(lines)
        @conditions = ''
        @condition_names = {}
        @condition_names_arr = []
        @outputfile = lines[0].gsub("\t", " ")
        @outputfile = @outputfile.split()[1].strip()
        lines.shift
        cond_end = -1

        lines.each_with_index do |line, i|
            line = "</condition>" if line == "\t</condition>"
            if line.strip == "#### END ####"
                cond_end = i
                break
            end
            @conditions += line + "\n"
        end

        xml = REXML::Document.new("<conditions>\n" + @conditions + "\n</conditions>")
        xml.elements.each "//condition" do |cond|
            @condition_names[cond.attributes['alias']] = ''
            @condition_names_arr << cond.attributes['alias']
        end

        treesegs = ['genetic']
        treearray = lines.slice(cond_end+2, lines.size)
        for line in treearray
            treesegs << line.strip unless line == ""
        end
        @treelocation = treesegs.join(":")
        #puts "treelocation = #{@treelocation}"
        
        
    end

    def is_control?
        !@treelocation.index(":Controls:").nil?
    end

    def outputfile
        @outputfile

    end

    def conditions
        @conditions.chomp!
    end

    def condition_names
        @condition_names
    end

    def condition_names_arr
        @condition_names_arr
    end

    def treelocation
        @treelocation
    end

    def to_s
        s = ''
        s+= "Outputfile: #{outputfile()}\n"
        s+= "Is a control? #{is_control?()}\n"
        s+= "Tree location: #{treelocation()}\n"
        s+= "Conditions:\n"
        s+= conditions()
        s
    end
end

class ParsedNoteFile
    def initialize(lines)
        @segments = []
        @lines = lines
        @sec_indexes = []
        count = 0
        for line in @lines
            if (line.strip == "Make File:")
                @sec_indexes << count
            end
            count += 1
        end
        @sec_indexes.each_with_index do |sec_index, i|
            if (i == (@sec_indexes.size() -1))
                nextstart = @lines.size() +1
            else
                nextstart = @sec_indexes[i+1]
            end
            @segments << Segment.new(@lines.slice(sec_index+1..nextstart-1))
        end

    end

    def source_file
        sf = @lines[0]
        sf.gsub!(/^From File /, "")
        sf
    end

    def segments
        @segments
    end

    def to_s
        s = ''
        s += "Source file: #{source_file()}\n"
        s += "Segments:\n"
        for seg in segments()
            s+= seg.to_s
        end
        s
    end

end



class Marcfix

    def self.get_raw_uri(element)
        u = element.text
        u.strip!
        if (!u.downcase.index('httpindirect').nil?)
            segs = u.split("/")
            u = segs.last
        end
        u
    end

    def self.generate_xml(pnf, seg)
        source_file = $repopath + File::SEPARATOR + pnf.source_file
        xml = REXML::Document.new(File.open(source_file))
        root = xml.root
        # todo add a control indicator here!
        xml.elements.each "//predicate" do |pred|
            if (pred.attributes['category'] == 'perturbation')
                pred.attributes['value'] = seg.treelocation()
            end
        end

        xml.elements.each "//uri" do |uri|
            old_uri = get_raw_uri(uri)
            prefix = seg.outputfile().slice(0,seg.outputfile().rindex('.'))
            expname = prefix
            ext = old_uri.slice(old_uri.rindex('.'),old_uri.length)
            uri.text="#{prefix}#{ext}"
        end

        expname = seg.outputfile().gsub(/\.xml$/,"")
        #puts "expname = #{expname}"
        #puts "oldexpname = " + root.attributes["name"]
        root.attributes['name'] = expname


        xml.elements.each "//condition" do |cond|
            root.delete(cond)
        end

        xml_str = xml.to_s
        xml_str.gsub!("</experiment>", "")
        cond_lines = seg.conditions().split("\n")
        for line in cond_lines
            xml_str += ("\t" + line + "\n") if !(line =~ %r{<condition|/condition}).nil?
            xml_str += (line + "\n") if !(line =~ %r{<variable|/variable}).nil?

        end
        xml_str += "\n</experiment>"

        outputfilename = $outputpath + File::SEPARATOR + seg.outputfile()
        puts "WHOOPS! #{outputfilename} exists already!" if File.exists?(outputfilename)
        outputfile = File.open(outputfilename, "a") do |file|
            file << xml_str
        end
    end

    def self.generate_data(pnf, seg, mapper)
        source_file = $repopath + File::SEPARATOR + pnf.source_file
        xml = REXML::Document.new(File.open(source_file))
        xml.elements.each "//uri" do |uri|
            raw_uri = get_raw_uri(uri)
            olddatafilename = $repopath + File::SEPARATOR + raw_uri
            outputfilename = $outputpath + File::SEPARATOR + seg.outputfile()
            outputfilename.gsub!(/\.xml$/, "")
            segs = olddatafilename.split(".")
            ext = segs.pop
            outputfilename += "." + ext
            old_headers_str = `head -1 #{olddatafilename}`.chomp
            old_headers = old_headers_str.split()
            new_headers = []
            row_titles_title = old_headers.shift
            new_headers << row_titles_title
            columns = []
            #columns << "$1"
            columns << 0
            #puts "###"

            #seg.condition_names().each_with_index do |newcond, i|
            old_headers.each_with_index do |header, i|
                new_equiv = mapper.old_to_new[header]
                if seg.condition_names().include?(new_equiv)
                    #columns << "$#{(i + 2)}"
                    columns << (i + 1)
                end
            end
            #pp columns
            puts "WHOOPS! #{outputfilename} exists already!" if File.exists?(outputfilename)
            File.open(outputfilename, "a") do |file|
                file.write row_titles_title
                file.write "\t"
                file.write seg.condition_names_arr().join("\t")
                #file.write(new_headers.join("\t"))
                file.write("\n")
                #cmd = %Q!awk 'OFS = "\t"; {print #{columns.join(",")}}' #{olddatafilename}|grep -v '#{row_titles_title}'!
                #puts cmd
                #file.write(`#{cmd}`)
                #puts seg.outputfile
                #pp columns
                awk_replacer(columns, olddatafilename, file)
            end

        end

    end

    def self.awk_replacer(columns, olddatafilename, file)
        firstline = true
        File.open(olddatafilename) do |oldfile|
            while line = oldfile.gets
                if (firstline)
                    firstline = false
                    next
                end
                line.chomp!
                cols = line.split(/\t/)
                out_line = []
                cols.each_with_index do |col, i|
                    if (columns.include?(i))
                        out_line << col
                    end
                end
                file.write(out_line.join("\t"))
                file.write("\n")
            end
        end
    end

    def self.handle(filename)
        lines = []
        File.open(filename) do |file|
            while line = file.gets
                lines << line.chomp
            end
        end
        pnf = ParsedNoteFile.new(lines)
        mapper = ConditionMapper.new(lines, pnf.source_file)
        for seg in pnf.segments
            if File.exists?($outputpath + File::Separator + seg.outputfile)
                puts "#{seg.outputfile} exists, skipping..."
                next
            end
            generate_xml(pnf, seg)
            generate_data(pnf, seg, mapper)
            # todo generate list of superseded xml files to be deleted
            # (and/or actually do the deletion?)
            # todo generate lines to be added to / removed from .permissions file
            # (and/or actually change the .permissions file)
        end

    end

    def self.main()

        if (ARGV.size == 1)
            puts "handling #{ARGV.first}..."
            handle(ARGV.first)
            return
        end


        require 'find'
        count = 0;
        Find.find $notespath do |file|
            if (file =~ /\.txt$/)
                #break if count > 0
                puts "handling #{file}..."
                handle(file)
                count += 1
            end
        end
    end

    $repopath = '/Users/dtenenbaum/emi-sandbox/halobacterium/repos'
    $notespath = '/Users/dtenenbaum/marc'
    $outputpath = '/Users/dtenenbaum/marc_out'
    main()
    puts "done."

end