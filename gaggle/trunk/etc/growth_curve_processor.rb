#!/usr/bin/ruby

# todo - add manipulatedVariable and manipulationType to emi template, and fill in...?
# todo - strip out NRC-1-delta from well names and populate vars above with "knockout" and gene name

require 'pp'

@emi_template = <<END
<?xml version="1.0"?>
<experiment name="__NAME__" date="__DATE__">

  __MANIPULATED__

  <predicate category='species' value='Halobacterium sp. NRC-1'/>
  <predicate category='strain'  value='__STRAIN__'/>
  <predicate category='perturbation' value='__TREEPATH__'/>

  <dataset status='primary' type='growth curve data'>
    <uri>__DATAFILE__</uri>
  </dataset>


  <constants>
__CONSTANTS__
  </constants>


__CONDITIONS__

</experiment>

END

def fix_deltas(input)
    if (input =~ /^NRC-1-delta-/)
        input.gsub!("NRC-1-delta-", "")
        @delta_map[input] = true
    else
        @delta_map[input] = false
    end
    input
end

def get_strain_names()
    @delta_map = {}
    @well_name_map = {}
    @well_order = []
    @strain_order = []
    well_line = nil
    strain_line = nil
    primary_pert_line = nil
    pert_value_line = nil
    secondary_pert_line = nil
    File.open(@metadata_file_name) do |file|
        file.each_line do |line|
            if line =~ /^Date/
                trash, @exp_date = line.split(@METADATA_DELIMITER)
                @exp_date.chomp!
            end
            next if line.chomp.strip == 'Unit'
            if line.downcase =~/^primary perturbation/
                primary_pert_line = line.chomp
            end
            if line.downcase =~ /^perturbation value/
                pert_value_line = line.chomp
            end
            if line.downcase =~ /^secondary perturbation/
                secondary_pert_line = line.chomp
            end
            if line =~ /^well/
                well_line = line.chomp
            end
            if line =~ /^strain/
                strain_line = line.chomp
            end
        end
    end
    well_segs = well_line.split(@METADATA_DELIMITER)
    well_segs.shift
    well_segs.shift
    strain_segs = strain_line.split(@METADATA_DELIMITER)
    strain_segs.shift
    strain_segs.shift

    ppl_segs = primary_pert_line.split(@METADATA_DELIMITER)
    2.times {ppl_segs.shift}

    pvl_segs = pert_value_line.split(@METADATA_DELIMITER)
    pvl_segs.shift
    pp_unit = pvl_segs.shift

    spl_segs = secondary_pert_line.split(@METADATA_DELIMITER)
    2.times {spl_segs.shift}

    

    well_segs.each_with_index do |well_seg, i|
        strain_seg = strain_segs[i]
        strain_seg = fix_deltas(strain_seg)
        if strain_seg[0] == 163 # shouldn't need this anymore
            tmp = strain_seg.split("")
            2.times do
                tmp.shift
            end
            strain_seg = tmp.join("")

        end



        unless (ppl_segs[i].nil? || ppl_segs[i].empty?)
            strain_seg += " - #{ppl_segs[i]}"
        end

        unless (pvl_segs[i].nil? || pvl_segs[i].empty?)
            strain_seg += " (#{pvl_segs[i]} #{pp_unit})"
            #strain_seg += " (#{pvl_segs[i]})"
        end

        unless (spl_segs[i].nil? || spl_segs[i].empty?)
            strain_seg += " - #{spl_segs[i]}"
        end

        strain_seg += "_#{@exp_date}"

        strain_seg.gsub!("/", "-")

        #puts strain_seg


        @strain_order << strain_seg
        @well_order << well_seg# unless strain_seg =~ /blank/
        @well_name_map[well_seg] = strain_seg# unless strain_seg =~ /blank/
    end
    #pp @well_order
end

def transpose_data()
    data = []
    count = 0
    File.open(@data_file_name) do |file|
        file.each_line do |line|
            line.chomp!
            row = line.split(@DATA_DELIMITER)
            if (count == 0)
                tmp = ''
                row.each_with_index do |cell, i|
                    if (i == 0)
                        newname = "Well"
                    else
                        if (@well_name_map[cell].nil? || @well_name_map[cell].empty?)
                            newname = cell
                        else
                            newname = "#{cell}-#{@well_name_map[cell]}"
                        end
                    end
                    tmp += "#{newname}#{@DATA_DELIMITER}"
                end
                row = tmp.split(@DATA_DELIMITER)
            end
            count += 1
            data << row
        end
    end
    data = data.transpose
    @timepoints =  data.shift
    @timepoints.shift
    pad_timepoints()
    #@outfilename = @output_dir + File::SEPARATOR + @exp_date + ".data"
    #File.open(@outfilename, "w") do |outfile|
    #    outfile.printf "Well\t"
    for row in data
        wellname = row.first.gsub(" ", "_")
        next if wellname =~ /blank/
        outname = @output_dir + File::SEPARATOR  + wellname + ".data"
        File.open(outname, "w") do |outfile|
            outfile.printf "Well\t"
            headerline = ""
            for timepoint in @timepoints
                headerline += "#{timepoint}_#{@exp_date}#{@DMV_DELIMITER}"
            end
            headerline.chop!
            outfile.puts headerline
            #outfile.puts @timepoints.join(@DMV_DELIMITER)
            outputline = ''
            for cell in row
                outputline +=  "#{cell}#{@DMV_DELIMITER}"
            end
            outputline.chop!
            outfile.puts outputline#unless outputline =~ /blank/
        end
    end

    #end
end
def pad_timepoints
    @timepoints.each_with_index do |timepoint, i|
        (hours, minutes, seconds)  = timepoint.split(":")
        hours = pad(hours, (4 - hours.length()))
        minutes = pad(minutes, (2 - minutes.length()))
        seconds = pad(seconds, (2 - seconds.length()))
        @timepoints[i] = "#{hours}:#{minutes}:#{seconds}"
    end
end

def pad(str, numchars)
    numchars.times {str = "0" + str}
    str
end


def create_emi_files()
    wells = []
    strains = []
    c_data = []
    v_data = []
    constant = nil
    columns = -1
    name_unit_hash = {}
    File.open(@metadata_file_name) do |file|
        file.each_line do |line|
            line.chomp!

            next if line =~ /^Date/
            next if line =~ /#{@METADATA_DELIMITER}Unit/
            if line =~ /^well#{@METADATA_DELIMITER}/
                wells = line.split("#{@METADATA_DELIMITER}")
                columns = (wells.size() -1)
                #puts "columns = #{columns}"
                wells.shift
                wells.shift
            elsif line =~ /^strain#{@METADATA_DELIMITER}/
                strains = line.split("#{@METADATA_DELIMITER}")
                strains.shift
                strains.shift
            elsif line =~ /^constant/
                constant = true
            elsif line =~ /^variables/
                constant = false
            elsif line =~ /Uracil/ and constant # shouldn't be necessary anymore
                next
            else
                tmp = line.split("#{@METADATA_DELIMITER}")
                header = tmp.shift
                units = tmp.shift
                name_unit_hash[header]=units
                tmp.reverse!
                tmp << header
                tmp.reverse!

                if tmp.size < columns
                    diff = columns - tmp.size
                    diff.times do
                        tmp << ""
                    end
                end
                if constant
                    c_data << tmp
                else
                    #pp tmp
                    v_data << tmp
                end
            end
        end
    end
    #puts "c_data first size = #{c_data.first.size}"
    c_data = c_data.transpose
    c_headers = c_data.shift
    #puts "v_data first size = #{v_data.first.size}"
    #pp v_data.first
    v_data = v_data.transpose
    v_headers = v_data.shift

    
    @strain_order.each_with_index do |strain, i|
        next if strain =~ /blank/
        #puts strain
        #puts @well_order[i]
        emi = "#{@emi_template}"
        #name = "#{@exp_date}_#{@well_order[i]}-#{strain}"
        emi.gsub!("__NAME__",  "#{@well_order[i]}-#{strain}")
        emi.gsub!("__DATE__", @exp_date)
        emi.gsub!("__STRAIN__", strain)
        
        
                # temporarily
                #_treepath = @well_order[i] 
                _strain_copy = "#{strain}"
                _gene_rep, _straininfo, _media = _strain_copy.split " - "
                _segs = _media.split('_')
                _segs.pop
                _media = _segs.join("_")
                _treepath = "#{@well_order[i]} - #{_straininfo} - #{_media}"
                puts _treepath
                emi.gsub!("__TREEPATH__", "Growth Curve:#{@exp_date}:#{_treepath}")

                # end temporarily - uncomment following line

                emi.gsub!("__TREEPATH__", "Growth Curve:#{@exp_date}:#{@well_order[i]}-#{strain}")
        
        

        if (@delta_map[strain] == true)
            str = "#{strain}"
            str.gsub!(/-replicate[0-9]*$/, "")
            man = %Q(<predicate category="manipulationType" value="knockout"/>\n)
            man += %Q(  <predicate category="manipulatedVariable" value="#{str}"/>\n\n)
            emi.gsub!("__MANIPULATED__", man)
        else
            emi.gsub!("__MANIPULATED__", "")
        end




        # 20070816_Well_272-tbpC-replicate2.data

        emi.gsub!("__DATAFILE__", "#{@well_order[i]}-#{strain}.data".gsub(" ", "_"))
        cons = ""
        c_data[i].each_with_index do |item, j|
            header = c_headers[j]
            units = name_unit_hash[header]
            cons += %Q(     <variable name="#{header}" value="#{item}")
            unless (units.nil? || units.empty?)
                cons += %Q( units="#{units}")
            end
            cons += "/>\n"
        end

        emi.gsub!("__CONSTANTS__", cons)

        vars = ""
        @timepoints.each do |timepoint|
            vars += %Q(     <condition alias="#{timepoint}_#{@exp_date}">\n)
            #vars += %Q(     <variable name="foo" value="#{@well_order[i]}"/>\n)
            v_data[i].each_with_index do |var, k|
                unless var.nil?
                    header = v_headers[k]
                    units = name_unit_hash[header]
                    next if var.nil? or var.strip.empty?
                    next if (var == "FALSE") and (header =~ /^peptone$|^FYL$|^DM$|^BD$/)
                    vars += %Q(         <variable name="#{header}" value="#{var}")
                    unless units.empty?
                        vars += %Q( units="#{units}")
                    end
                    vars += "/>\n"
                end
            end
            vars += %Q(     </condition>\n)
        end
        emi.gsub!("__CONDITIONS__", vars)
        #puts emi
        #puts "\n\n\n --------- \n\n\n"
        emifile = "#{@output_dir}#{File::SEPARATOR}#{@exp_date}_#{@well_order[i]}.xml".gsub(" ", "_")
        puts "processing #{emifile}..."
        File.open(emifile, "w") do |file|
            file.puts emi
        end
    end 


end

def main()
    if ARGV.size != 3
        puts "usage: growth_curve_processor.rb [data_file_name] [metadata_file_name] [output_dir]"
        exit
    end
    @data_file_name, @metadata_file_name, @output_dir = ARGV
    get_strain_names()
    transpose_data()
    create_emi_files()
end

@DATA_DELIMITER = ","
@METADATA_DELIMITER = "\t"
@DMV_DELIMITER = "\t"
main()
