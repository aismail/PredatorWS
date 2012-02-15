#?/bin/bash
mkdir build
cd build
cmake ../src
make
cd ../bin
./run_tld -p ../parameters.yml -tl
