#?/bin/bash
cmake ../src
cd ../build
make
cd ../bin
./run_tld -p ../parameters.yml -tl
