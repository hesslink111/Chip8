class Machine(
    val memory: UByteArray = UByteArray(4096),
    val v: UByteArray = UByteArray(16),
    var i: UShort = 0u,
    var dt: UByte = 0u,
    var st: UByte = 0u,
    var pc: UShort = 0u,
    var sp: UShort = 0u,
    val stack: UShortArray = UShortArray(16),
    val keys: BooleanArray = BooleanArray(16),
    val spriteDigits: UShortArray = UShortArray(16),
    val display: BooleanArray = BooleanArray(64 * 32)
) {
    init {
        // Set sprite digits.
        spriteDigits[0x0] = 0.toUShort()
        memory[0] = 0xF0.toUByte()
        memory[1] = 0x90.toUByte()
        memory[2] = 0x90.toUByte()
        memory[3] = 0x90.toUByte()
        memory[4] = 0xF0.toUByte()

        spriteDigits[0x1] = 5.toUShort()
        memory[5] = 0x20.toUByte()
        memory[6] = 0x60.toUByte()
        memory[7] = 0x20.toUByte()
        memory[8] = 0x20.toUByte()
        memory[9] = 0x70.toUByte()

        spriteDigits[0x2] = 10.toUShort()
        memory[10] = 0xF0.toUByte()
        memory[11] = 0x10.toUByte()
        memory[12] = 0xF0.toUByte()
        memory[13] = 0x80.toUByte()
        memory[14] = 0xF0.toUByte()

        spriteDigits[0x3] = 15.toUShort()
        memory[15] = 0xF0.toUByte()
        memory[16] = 0x10.toUByte()
        memory[17] = 0xF0.toUByte()
        memory[18] = 0x10.toUByte()
        memory[19] = 0xF0.toUByte()

        spriteDigits[0x4] = 20.toUShort()
        memory[20] = 0x90.toUByte()
        memory[21] = 0x90.toUByte()
        memory[22] = 0xF0.toUByte()
        memory[23] = 0x10.toUByte()
        memory[24] = 0x10.toUByte()

        spriteDigits[0x5] = 25.toUShort()
        memory[25] = 0xF0.toUByte()
        memory[26] = 0x80.toUByte()
        memory[27] = 0xF0.toUByte()
        memory[28] = 0x10.toUByte()
        memory[29] = 0xF0.toUByte()

        spriteDigits[0x6] = 30.toUShort()
        memory[30] = 0xF0.toUByte()
        memory[31] = 0x80.toUByte()
        memory[32] = 0xF0.toUByte()
        memory[33] = 0x90.toUByte()
        memory[34] = 0xF0.toUByte()

        spriteDigits[0x7] = 35.toUShort()
        memory[35] = 0xF0.toUByte()
        memory[36] = 0x10.toUByte()
        memory[37] = 0x20.toUByte()
        memory[38] = 0x40.toUByte()
        memory[39] = 0x40.toUByte()

        spriteDigits[0x8] = 40.toUShort()
        memory[40] = 0xF0.toUByte()
        memory[41] = 0x90.toUByte()
        memory[42] = 0xF0.toUByte()
        memory[43] = 0x90.toUByte()
        memory[44] = 0xF0.toUByte()

        spriteDigits[0x9] = 45.toUShort()
        memory[45] = 0xF0.toUByte()
        memory[46] = 0x90.toUByte()
        memory[47] = 0xF0.toUByte()
        memory[48] = 0x10.toUByte()
        memory[49] = 0xF0.toUByte()

        spriteDigits[0xA] = 50.toUShort()
        memory[50] = 0xF0.toUByte()
        memory[51] = 0x90.toUByte()
        memory[52] = 0xF0.toUByte()
        memory[53] = 0x90.toUByte()
        memory[54] = 0x90.toUByte()

        spriteDigits[0xB] = 55.toUShort()
        memory[55] = 0xE0.toUByte()
        memory[56] = 0x90.toUByte()
        memory[57] = 0xE0.toUByte()
        memory[58] = 0x90.toUByte()
        memory[59] = 0xE0.toUByte()

        spriteDigits[0xC] = 60.toUShort()
        memory[60] = 0xF0.toUByte()
        memory[61] = 0x80.toUByte()
        memory[62] = 0x80.toUByte()
        memory[63] = 0x80.toUByte()
        memory[64] = 0xF0.toUByte()

        spriteDigits[0xD] = 65.toUShort()
        memory[65] = 0xF0.toUByte()
        memory[66] = 0x90.toUByte()
        memory[67] = 0x90.toUByte()
        memory[68] = 0x90.toUByte()
        memory[69] = 0xF0.toUByte()

        spriteDigits[0xE] = 70.toUShort()
        memory[70] = 0xF0.toUByte()
        memory[71] = 0x80.toUByte()
        memory[72] = 0xF0.toUByte()
        memory[73] = 0x80.toUByte()
        memory[74] = 0xF0.toUByte()

        spriteDigits[0xF] = 75.toUShort()
        memory[75] = 0xF0.toUByte()
        memory[76] = 0x80.toUByte()
        memory[77] = 0xF0.toUByte()
        memory[78] = 0x80.toUByte()
        memory[79] = 0x80.toUByte()
    }
}
