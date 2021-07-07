import kotlin.random.Random

fun execute(machine: Machine, waitForKeyPress: () -> UByte, notifyDisplayUpdated: () -> Unit) {
    val op = (machine.memory[machine.pc.toUInt()].toUInt() shl 8)
        .or(machine.memory[machine.pc.toUInt() + 1u].toUInt())
    machine.pc = (machine.pc + 2u).toUShort()

    val success: Unit = when(op and 0xF000u) {
        0x0000u -> {
            when(op and 0x00FFu) {
                // 00E0 - CLS
                0x00E0u -> {
                    var updated = false
                    for (i in machine.display.indices) {
                        updated = updated || machine.display[i]
                        machine.display[i] = false
                    }
                    if(updated) {
                        notifyDisplayUpdated()
                    }
                    return
                }

                // 00EE - RET
                0x00EEu -> {
                    machine.pc = machine.stack[machine.sp]
                    machine.sp--
                    return
                }

                else -> unknownOp(op)
            }
        }

        // 1nnn - JP addr
        0x1000u -> {
            // Jump to nnn.
            machine.pc = (op and 0x0FFFu).toUShort()
        }

        // 2nnn - CALL addr
        0x2000u -> {
            // Call nnn.
            machine.sp++
            machine.stack[machine.sp] = machine.pc
            machine.pc = (op and 0x0FFFu).toUShort()
        }

        // 3xkk - SE Vx, byte
        0x3000u -> {
            // Skip next instruction if Vx = kk.
            val x = (op and 0x0F00u) shr 8
            val kk = (op and 0x00FFu).toUByte()
            if(machine.v[x] == kk) {
                machine.pc = (machine.pc + 2u).toUShort()
            }
            return
        }

        // 4xkk - SNE Vx, byte
        0x4000u -> {
            // Skip next instruction if Vx != kk.
            val x = (op and 0x0F00u) shr 8
            val kk = (op and 0x00FFu).toUByte()
            if(machine.v[x] != kk) {
                machine.pc = (machine.pc + 2u).toUShort()
            }
            return
        }

        0x5000u -> {
            when(op and 0x000Fu) {
                // 5xy0 - SE Vx, Vy
                0x0000u -> {
                    // Skip next instruction if Vx = Vy.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    if(machine.v[x] == machine.v[y]) {
                        machine.pc = (machine.pc + 2u).toUShort()
                    }
                    return
                }

                else -> unknownOp(op)
            }
        }

        // 6xkk - LD Vx, byte
        0x6000u -> {
            // Set Vx = kk.
            val x = (op and 0x0F00u) shr 8
            val kk = (op and 0x00FFu).toUByte()
            machine.v[x] = kk
        }

        // 7xkk - ADD Vx, byte
        0x7000u -> {
            // Set Vx = Vx + kk.
            val x = (op and 0x0F00u) shr 8
            val kk = (op and 0x00FFu).toUByte()
            machine.v[x] = (machine.v[x] + kk).toUByte()
        }

        0x8000u -> {
            when(op and 0x000Fu) {
                // 8xy0 - LD Vx, Vy
                0x0000u -> {
                    // Set Vx = Vy.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[x] = machine.v[y]
                }

                // 8xy1 - OR Vx, Vy
                0x0001u -> {
                    // Set Vx = Vx OR Vy.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[x] = machine.v[x] or machine.v[y]
                }

                // 8xy2 - AND Vx, Vy
                0x0002u -> {
                    // Set Vx = Vx AND Vy.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[x] = machine.v[x] and machine.v[y]
                }

                // 8xy3 - XOR Vx, Vy
                0x0003u -> {
                    // Set Vx = Vx XOR Vy.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[x] = machine.v[x] xor machine.v[y]
                }

                // 8xy4 - ADD Vx, Vy
                0x0004u -> {
                    // Set Vx = Vx + Vy, set VF = carry.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    val result = machine.v[x] + machine.v[y]
                    machine.v[x] = result.toUByte()
                    machine.v[0xF] = (result shr 8).toUByte()
                }

                // 8xy5 - SUB Vx, Vy
                0x0005u -> {
                    // Set Vx = Vx - Vy, set VF = NOT borrow.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[0xF] = if(machine.v[x] > machine.v[y]) 1u else 0u
                    machine.v[x] = (machine.v[x] - machine.v[y]).toUByte()
                }

                // 8xy6 - SHR Vx {, Vy}
                0x0006u -> {
                    // Set Vx = Vx SHR 1.
                    val x = (op and 0x0F00u) shr 8
                    // val y = (op and 0x00F0u) shr 4 // ?
                    machine.v[0xF] = if((machine.v[x].toUInt() and 0x0001u) == 1u) 1u else 0u
                    machine.v[x] = (machine.v[x].toUInt() shr 1).toUByte()
                }

                // 8xy7 - SUBN Vx, Vy
                0x0007u -> {
                    // Set Vx = Vy - Vx, set VF = NOT borrow.
                    val x = (op and 0x0F00u) shr 8
                    val y = (op and 0x00F0u) shr 4
                    machine.v[0xF] = if(machine.v[y] > machine.v[x]) 1u else 0u
                    machine.v[x] = (machine.v[y] - machine.v[x]).toUByte()
                }

                // 8xyE - SHL Vx {, Vy}
                0x000Eu -> {
                    // Set Vx = Vx SHL 1.
                    val x = (op and 0x0F00u) shr 8
                    // val y = (op and 0x00F0u) shr 4 // ?
                    machine.v[0xF] = if((machine.v[x].toUInt() and 0x1000u) == 0x1000u) 1u else 0u
                    machine.v[x] = (machine.v[x].toUInt() shl 1).toUByte()
                }

                else -> unknownOp(op)
            }
        }

        // 9xy0 - SNE Vx, Vy
        0x9000u -> {
            // Skip next instruction if Vx != Vy.
            val x = (op and 0x0F00u) shr 8
            val y = (op and 0x00F0u) shr 4
            if(machine.v[x] != machine.v[y]) {
                machine.pc = (machine.pc + 2u).toUShort()
            }
            return
        }

        // Annn - LD I, addr
        0xA000u -> {
            // Set I = nnn.
            machine.i = (op and 0x0FFFu).toUShort()
        }

        // Bnnn - JP V0, addr
        0xB000u -> {
            // Jump to location nnn + V0.
            machine.pc = ((op and 0x0FFFu) + machine.v[0]).toUShort()
        }

        // Cxkk - RND Vx, byte
        0xC000u -> {
            // Set Vx = random byte AND kk.
            val x = (op and 0x0F00u) shr 8
            val kk = (op and 0x00FFu).toUByte()
            val rb = Random.nextInt(0, 256).toUByte()
            machine.v[x] = rb and kk
        }

        // Dxyn - DRW Vx, Vy, nibble
        0xD000u -> {
            // Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
            val x = (op and 0x0F00u) shr 8
            val y = (op and 0x00F0u) shr 4
            val n = (op and 0x000Fu)
            val vx = machine.v[x]
            val vy = machine.v[y]
            var displayUpdated = false
            var collision = false
            for(yOffset in 0u until n) {
                val spriteByte = machine.memory[machine.i + yOffset]
                for(xOffset in 0U until 8u) {
                    val update = ((spriteByte.toUInt() shr (7 - xOffset.toInt())) and 0b1u) == 1u
                    val displayPosition = (((vx + xOffset) % 64u) * 32u) + ((vy + yOffset) % 32u)
                    val existingPixel = machine.display[displayPosition]
                    val displayPixel = existingPixel xor update
                    machine.display[displayPosition] = displayPixel
                    displayUpdated = displayUpdated || (existingPixel != displayPixel)
                    collision = collision || existingPixel && !displayPixel
                }
            }
            if(displayUpdated) {
                notifyDisplayUpdated()
            }
            machine.v[0xF] = if(collision) 1u else 0u
        }

        0xE000u -> {
            when(op and 0x00FFu) {
                // Ex9E - SKP Vx
                0x009Eu -> {
                    // Skip next instruction if key with the value of Vx is pressed.
                    val x = (op and 0x0F00u) shr 8
                    if(machine.keys[machine.v[x]]) {
                        machine.pc = (machine.pc + 2u).toUShort()
                    }
                    return
                }

                // ExA1 - SKNP Vx
                0x00A1u -> {
                    // Skip next instruction if key with the value of Vx is not pressed.
                    val x = (op and 0x0F00u) shr 8
                    if(!machine.keys[machine.v[x]]) {
                        machine.pc = (machine.pc + 2u).toUShort()
                    }
                    return
                }

                else -> unknownOp(op)
            }
        }

        0xF000u -> {
            when(op and 0x00FFu) {
                // Fx07 - LD Vx, DT
                0x0007u -> {
                    // Set Vx = delay timer value.
                    val x = (op and 0x0F00u) shr 8
                    machine.v[x] = machine.dt
                }

                // Fx0A - LD Vx, K
                0x000Au -> {
                    // Wait for a key press, store the value of the key in Vx.
                    val x = (op and 0x0F00u) shr 8
                    machine.v[x] = waitForKeyPress()
                }

                // Fx15 - LD DT, Vx
                0x0015u -> {
                    // Set delay timer = Vx.
                    val x = (op and 0x0F00u) shr 8
                    machine.dt = machine.v[x]
                }

                // Fx18 - LD ST, Vx
                0x0018u -> {
                    // Set sound timer = Vx.
                    val x = (op and 0x0F00u) shr 8
                    machine.st = machine.v[x]
                }

                // Fx1E - ADD I, Vx
                0x001Eu -> {
                    // Set I = I + Vx.
                    val x = (op and 0x0F00u) shr 8
                    machine.i = (machine.i + machine.v[x]).toUShort()
                }

                // Fx29 - LD F, Vx
                0x0029u -> {
                    // Set I = location of sprite for digit Vx.
                    val x = (op and 0x0F00u) shr 8
                    machine.i = machine.spriteDigits[machine.v[x]]
                }

                // Fx33 - LD B, Vx
                0x0033u -> {
                    // Store BCD representation of Vx in memory locations I, I+1, and I+2.
                    val x = (op and 0x0F00u) shr 8
                    val hundreds = ((machine.v[x] / 100u) % 10u).toUByte()
                    val tens = ((machine.v[x] / 10u) % 10u).toUByte()
                    val ones = ((machine.v[x]) % 10u).toUByte()
                    machine.memory[machine.i] = hundreds
                    machine.memory[machine.i + 1u] = tens
                    machine.memory[machine.i + 2u] = ones
                }

                // Fx55 - LD [I], Vx
                0x0055u -> {
                    // Store registers V0 through Vx in memory starting at location I.
                    val x = (op and 0x0F00u) shr 8
                    for(i in 0u .. x) {
                        machine.memory[machine.i + i] = machine.v[i]
                    }
                }

                // Fx65 - LD Vx, [I]
                0x0065u -> {
                    // Read registers V0 through Vx from memory starting at location I.
                    val x = (op and 0x0F00u) shr 8
                    for(i in 0u .. x) {
                        machine.v[i] = machine.memory[machine.i + i]
                    }
                }

                else -> unknownOp(op)
            }
        }

        else -> unknownOp(op)
    }
}

fun unknownOp(op: UInt) {
    throw Exception("Unknown op: $op")
}
