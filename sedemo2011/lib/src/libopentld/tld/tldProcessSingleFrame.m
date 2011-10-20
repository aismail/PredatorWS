function tldProcessSingleFrame()
    global tld;
    persistent i;

    if isempty(i)
        i = 1;
    end;
    i = i + 1;
    
    file_name = 'temp/frame.png';
    frame = img_get_from_file(file_name);
    tld = tldProcessFrame(tld,i,frame); % process frame i    
    bb = tld.bb(:,i);

    % Delete bb.txt file, if it exists
    if exist('bb.txt', 'file')
	    delete('bb.txt');
    end;

    dlmwrite('bb.txt', bb);
end
