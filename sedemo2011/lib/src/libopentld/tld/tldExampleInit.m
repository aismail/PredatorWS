function tldExampleInit(opt)

global tld;

opt.source = tldInitSource(opt.source); % select data source, camera/directory
opt.source.idx = 1:10000; 

while 1
    source = tldInitFirstFrame(tld,opt.source,opt.model.min_win); % get initial bounding box, return 'empty' if bounding box is too small
    if ~isempty(source), opt.source = source; break; end % check size
end

tld = tldInit(opt,[]); % train initial detector and initialize the 'tld' structure

end