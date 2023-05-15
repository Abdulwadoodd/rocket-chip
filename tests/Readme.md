# Assembly Tests

This directory contains simple assembly programs to test the functionality of code size reduction extension in RocketCore. 

:warning: These tests may not test the corner cases. More mature test suites will come from [arch-test](https://github.com/riscv-non-isa/riscv-arch-test) repository.

First, toolchain the supports code size reduction extension must be installed and the binaries must be added to `PATH` variable. Pre-built toolchain is available [here](https://www.embecosm.com/resources/tool-chain-downloads/#corev). After setting up the toolchain,run the following command in this directory.

```
make zcb.elf
```

This will generate `zcb.elf`. 

Now, build a verilator model by running the following command from [emulator](/emulator/) directory

```
make debug CONFIG_TYPE=BitManipCryptoConfig
```

This will include the Bit Manip extension to RocketCore and build the simulation executable named in the same directory. Type the following command to see the options to run the tests:
```
./emulator-freechips.rocketchip.system-freechips.rocketchip.system.BitManipCryptoConfig-debug
```