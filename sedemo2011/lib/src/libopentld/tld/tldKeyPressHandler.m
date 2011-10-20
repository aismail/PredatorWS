function tldKeyPressHandler()

global finish;

finish = 0; 
set(2,'KeyPressFcn', @handleKey);
function handleKey(~,~), finish = 1; end % by pressing any key, the process will exit

end