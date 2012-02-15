#?/bin/bash
rm -rf bin
rm -rf build
mkdir build
cd build
cmake ../src
make
cd ../bin
./run_tld -p ../parameters.yml -tl
