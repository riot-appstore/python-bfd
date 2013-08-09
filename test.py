#!/usr/bin/python
import bfd
import sys
import os
import binascii

# Called each time there is an address referenced in an instruction,
#   such as move r0, 0x12345678
#
# We get a chance to format that value however we want.  In the example below,
#
def funcFmtAddr(bfd, addr):
    addrStr = '0x%08x' % addr
    if bfd.syms_by_addr.has_key(addr):
        addrStr += ' <%s>' % bfd.syms_by_addr[addr].name
    return addrStr

def funcFmtLine(addr, rawData, instr, abfd):
    ret = ''
    if abfd.syms_by_addr.has_key(addr):
        ret += '\n0%08x <%s>:\n' % (addr, abfd.syms_by_addr[addr].name)
    if abfd.bpc > 1 and abfd.endian is bfd.ENDIAN_LITTLE:
        bytes = ''.join(['%02x ' % i for i in reversed(rawData)])
    else:
        bytes = ''.join(['%02x ' % i for i in rawData])
        
    ret += ' %08x %-32s %s\n' % (addr, bytes, instr)
    return ret

def dump_sec(abfd, sec_name, offset, size):
    
    text = abfd.sections[sec_name]
    dump = abfd.raw_data(text, text.vma + offset, size)
    print binascii.hexlify(dump)

def dump(path, target, numLines):

    b=bfd.Bfd(path, target)

    print '\nFile: %s' % path
    print 'Arch ID: %d' % b.archId
    print 'Architecture: %s' % b.arch
    print 'Machine: %s' % b.mach
    print 'Target: %s' % b.target

    print '\nSections:\n'
    print 'Index %-32s %-10s %-10s %-10s %s' % ('Name', 'Size', 'VMA', 'Flags', 'Alignment')
    i = 0
    for sec in b.sections.values():
        print '   %2d %-32s 0x%08x 0x%08x %s 2**%d' % (i, sec.name, sec.size, sec.vma, ', '.join([f.abbrev for f in sec.flags]), sec.alignment)
        #print sec.SEC_ALLOC
        i += 1

    print '\nSymbols:\n'
    for sym in b.syms_by_name.values()[0:10]:
        print '  0x%08x %s %s' % (sym.value, sym.type, sym.name)

    if '.text' in b.sections:
        sec_name = '.text'
    elif '.app_text' in b.sections:
        sec_name = '.app_text'
    elif '.data' in b.sections:
        sec_name = '.data'
    print '\nDisassembly of %s:\n' % sec_name
    sec = b.sections[sec_name]
    start = sec.vma
    (dis,nextAddr, lineCnt) = b.disassemble(sec, start, None, numLines, funcFmtAddr, funcFmtLine, {}, endian=bfd.ENDIAN_LITTLE)
    print 'disassembly is %s' % dis
    print 'Next address to disassemble is: 0x%08x' % nextAddr
    #print b.disassemble(sec, start, stop, funcFmtAddr, funcFmtLine, endian=bfd.ENDIAN_BIG)

    print '\nSupported Architectures:\n'
    print ' '.join(bfd.list_architectures())

    print '\nSupported Targets:\n'
    print ' '.join(bfd.list_targets())


def main():

    exe = '/bin/ls'

    if len(sys.argv) > 1:
        exe = sys.argv[1]

    numLines = 10
    
    if len(sys.argv) > 2:
        numLines = int(sys.argv[2])

    target_archs = bfd.guess_target_arch(exe)
    print 'guessed targets: %s' % target_archs

    for target,arch in target_archs:
        dump(exe, target, numLines)

if __name__ == '__main__':
    main()

