TOOLCHAIN_PREFIX=riscv32-corev-elf-

%.elf: %.S
	$(TOOLCHAIN_PREFIX)gcc -march=rv64im_zicsr_zcb_zba_zbb -mabi=lp64  -nostdlib -nostartfiles -T link.ld -o $@ $<

clean:
	rm -rf *.elf